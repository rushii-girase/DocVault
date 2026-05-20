package com.app.institutional.service;

import com.app.institutional.entity.AuditLog;
import com.app.institutional.entity.Document;
import com.app.institutional.entity.DocumentReview;
import com.app.institutional.entity.User;
import com.app.institutional.entity.enums.DocumentStatus;
import com.app.institutional.repository.AuditLogRepository;
import com.app.institutional.repository.DocumentRepository;
import com.app.institutional.repository.DocumentReviewRepository;
import com.app.institutional.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private DocumentReviewRepository documentReviewRepository;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public Document uploadDocument(MultipartFile file, String title, String userEmail, Long parentDocumentId) {
        User student = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String storedFileName = fileStorageService.storeFile(file);

        Document document = Document.builder()
                .title(title)
                .originalFileName(file.getOriginalFilename())
                .storedFileName(storedFileName)
                .contentType(file.getContentType())
                .sizeBytes(file.getSize())
                .student(student)
                .version(1)
                .status(DocumentStatus.PENDING)
                .uploadDate(LocalDateTime.now())
                .build();

        if (parentDocumentId != null) {
            Document parentDoc = documentRepository.findById(parentDocumentId)
                    .orElseThrow(() -> new RuntimeException("Parent document not found"));
            document.setParentDocument(parentDoc);
            // Increment version based on parent
            document.setVersion(parentDoc.getVersion() + 1);
        }

        Document savedDoc = documentRepository.save(document);
        logAction(student, "UPLOAD", "Document", savedDoc.getId(),
                "Uploaded document: " + title + " (v" + savedDoc.getVersion() + ")");

        // Notify all staff of the same college that a new document was uploaded or changed
        java.util.List<User> staffMembers = userRepository.findByRole(com.app.institutional.entity.enums.Role.STAFF);
        for (User staff : staffMembers) {
            if (staff.getCollegeName() == student.getCollegeName()) {
                notificationService.createNotification(staff,
                        "Student " + student.getName() + " uploaded a new document/change: " + title, savedDoc.getId());
            }
        }

        return savedDoc;
    }

    @Transactional
    public Resource downloadDocument(Long documentId, String requestorEmail) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        User requestor = userRepository.findByEmail(requestorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Basic RBAC: If Student, must be the owner. Staff/Admin can download anything.
        if (requestor.getRole().name().equals("STUDENT") && !document.getStudent().getId().equals(requestor.getId())) {
            throw new RuntimeException("Unauthorized to download this document");
        }

        try {
            Path filePath = fileStorageService.loadFileAsResource(document.getStoredFileName());
            Resource resource = new UrlResource(filePath.toUri());

            if (resource.exists() || resource.isReadable()) {
                logAction(requestor, "DOWNLOAD", "Document", document.getId(),
                        "Downloaded document: " + document.getOriginalFileName());
                return resource;
            } else {
                throw new RuntimeException("Could not read file object.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Error reading file", e);
        }
    }

    @Transactional
    public DocumentReview reviewDocument(Long documentId, String remark, DocumentStatus status, String reviewerEmail) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        User reviewer = userRepository.findByEmail(reviewerEmail)
                .orElseThrow(() -> new RuntimeException("Reviewer not found"));

        document.setStatus(status);
        documentRepository.save(document);

        DocumentReview review = DocumentReview.builder()
                .document(document)
                .reviewer(reviewer)
                .remarkText(remark)
                .statusDecision(status)
                .reviewDate(LocalDateTime.now())
                .build();

        DocumentReview savedReview = documentReviewRepository.save(review);
        logAction(reviewer, "REVIEW", "Document", document.getId(),
                "Reviewed document. Status: " + status.name() + ". Remark: " + remark);

        // Notify the Student
        String notificationMessage = "Your document '" + document.getTitle() + "' was reviewed ("
                + status.name() + "). Staff Remark: " + remark;
        notificationService.createNotification(document.getStudent(), notificationMessage, document.getId());

        return savedReview;
    }

    public List<Document> getStudentDocuments(Long studentId) {
        return documentRepository.findByStudentId(studentId);
    }

    public List<Document> getAllDocuments(String requestorEmail) {
        User requestor = userRepository.findByEmail(requestorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        List<Document> documents = documentRepository.findAll();
        
        if (requestor.getRole() == com.app.institutional.entity.enums.Role.STAFF && requestor.getCollegeName() != null) {
            documents = documents.stream()
                .filter(doc -> doc.getStudent().getCollegeName() == requestor.getCollegeName())
                .collect(java.util.stream.Collectors.toList());
        }
        return documents;
    }

    public Document getDocumentById(Long id) {
        return documentRepository.findById(id).orElseThrow(() -> new RuntimeException("Document not found"));
    }

    @Transactional
    public void deleteDocument(Long documentId, String requestorEmail) {
        Document document = documentRepository.findById(documentId)
                .orElseThrow(() -> new RuntimeException("Document not found"));

        User requestor = userRepository.findByEmail(requestorEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only STUDENT owner or ADMIN can delete
        if (requestor.getRole().name().equals("STUDENT") && !document.getStudent().getId().equals(requestor.getId())) {
            throw new RuntimeException("Unauthorized to delete this document");
        }

        // Clean up dependencies before deleting the document entity
        documentReviewRepository.deleteByDocumentId(document.getId());
        notificationService.deleteNotificationsByDocumentId(document.getId());

        documentRepository.delete(document);

        logAction(requestor, "DELETE", "Document", document.getId(),
                "Deleted document: " + document.getOriginalFileName());
    }

    private void logAction(User actor, String actionType, String entityName, Long entityId, String details) {
        AuditLog log = AuditLog.builder()
                .actor(actor)
                .actionType(actionType)
                .entityName(entityName)
                .entityId(entityId)
                .details(details)
                .build();
        auditLogRepository.save(log);
    }

    @Transactional
    public void requestCustomDocumentAll(String documentName, String note, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new RuntimeException("Requester not found"));

        List<User> students = userRepository.findByRole(com.app.institutional.entity.enums.Role.STUDENT);
        
        // If staff, only request from their own college students
        if (requester.getRole() == com.app.institutional.entity.enums.Role.STAFF && requester.getCollegeName() != null) {
            students = students.stream()
                .filter(s -> s.getCollegeName() == requester.getCollegeName())
                .collect(java.util.stream.Collectors.toList());
        }
        
        String notificationMessage = "Action Required: Please upload a custom document (" + documentName + ").\nNote from Staff/Admin: " + note;
        
        for (User student : students) {
            notificationService.createNotification(student, notificationMessage, null);
        }

        logAction(requester, "REQUEST_DOCUMENT", "Notification", null,
                "Requested custom document '" + documentName + "' from all students");
    }
}
