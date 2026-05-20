package com.app.institutional.controller;

import com.app.institutional.entity.Document;
import com.app.institutional.entity.DocumentReview;
import com.app.institutional.entity.enums.DocumentStatus;
import com.app.institutional.payload.response.MessageResponse;
import com.app.institutional.service.DocumentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200", maxAge = 3600)
@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping("/upload")
    @PreAuthorize("hasRole('STUDENT')")
    public ResponseEntity<?> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "parentDocumentId", required = false) Long parentDocumentId,
            Authentication authentication) {

        try {
            Document doc = documentService.uploadDocument(file, title, authentication.getName(), parentDocumentId);
            return ResponseEntity.ok(new MessageResponse(
                    "Document uploaded successfully. ID: " + doc.getId() + " v" + doc.getVersion()));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("Could not upload the file: " + e.getMessage()));
        }
    }

    @GetMapping("/download/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<Resource> downloadDocument(@PathVariable Long id, Authentication authentication) {
        try {
            Resource resource = documentService.downloadDocument(id, authentication.getName());

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType("application/octet-stream"))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/preview/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<Resource> previewDocument(@PathVariable Long id, Authentication authentication) {
        try {
            Document doc = documentService.getDocumentById(id);
            Resource resource = documentService.downloadDocument(id, authentication.getName());

            String contentType = doc.getContentType() != null ? doc.getContentType() : "application/pdf";

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/{id}/review")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<?> reviewDocument(
            @PathVariable Long id,
            @RequestParam("remark") String remark,
            @RequestParam("status") String statusStr,
            Authentication authentication) {

        try {
            DocumentStatus status = DocumentStatus.valueOf(statusStr.toUpperCase());
            DocumentReview review = documentService.reviewDocument(id, remark, status, authentication.getName());
            return ResponseEntity
                    .ok(new MessageResponse("Document reviewed successfully. Review ID: " + review.getId()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error reviewing document: " + e.getMessage()));
        }
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<List<Document>> getStudentDocuments(@PathVariable Long studentId) {
        return ResponseEntity.ok(documentService.getStudentDocuments(studentId));
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<List<Document>> getAllDocuments(Authentication authentication) {
        return ResponseEntity.ok(documentService.getAllDocuments(authentication.getName()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STUDENT') or hasRole('ADMIN')")
    public ResponseEntity<?> deleteDocument(@PathVariable Long id, Authentication authentication) {
        try {
            documentService.deleteDocument(id, authentication.getName());
            return ResponseEntity.ok(new MessageResponse("Document deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error deleting document: " + e.getMessage()));
        }
    }

    @PostMapping("/request-all")
    @PreAuthorize("hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<?> requestCustomDocumentAll(
            @RequestBody java.util.Map<String, String> payload,
            Authentication authentication) {
        try {
            String documentName = payload.get("documentName");
            String note = payload.get("note");
            if (note == null) note = "";
            documentService.requestCustomDocumentAll(documentName, note, authentication.getName());
            return ResponseEntity.ok(new MessageResponse("Document requested from all students successfully."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new MessageResponse("Error requesting document: " + e.getMessage()));
        }
    }
}
