package com.app.institutional.service;

import com.app.institutional.entity.Notification;
import com.app.institutional.entity.User;
import com.app.institutional.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    public void createNotification(User recipient, String message, Long relatedDocumentId) {
        Notification notification = Notification.builder()
                .recipient(recipient)
                .message(message)
                .relatedDocumentId(relatedDocumentId)
                .isRead(false)
                .createdAt(LocalDateTime.now())
                .build();
        notificationRepository.save(notification);
    }

    public List<Notification> getUnreadNotificationsForUser(Long userId) {
        return notificationRepository.findByRecipientIdAndIsReadFalseOrderByCreatedAtDesc(userId);
    }

    public List<Notification> getAllNotificationsForUser(Long userId) {
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(userId);
    }

    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    @org.springframework.transaction.annotation.Transactional
    public void deleteNotificationsByDocumentId(Long documentId) {
        notificationRepository.deleteByRelatedDocumentId(documentId);
    }
}
