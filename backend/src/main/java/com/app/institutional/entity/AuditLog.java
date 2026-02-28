package com.app.institutional.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "audit_logs")
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "password" })
    private User actor; // Who performed the action

    @Column(nullable = false)
    private String actionType; // UPLOAD, DOWNLOAD, REVIEW, DELETE

    @Column(nullable = false)
    private String entityName; // "Document", "User"

    private Long entityId; // ID of the document/user affected

    @Column(columnDefinition = "TEXT")
    private String details;

    @Builder.Default
    private LocalDateTime actionDate = LocalDateTime.now();
}
