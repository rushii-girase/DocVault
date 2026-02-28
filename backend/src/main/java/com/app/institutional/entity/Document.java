package com.app.institutional.entity;

import com.app.institutional.entity.enums.DocumentStatus;
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
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String originalFileName;

    @Column(nullable = false)
    private String storedFileName;

    private String contentType;
    private Long sizeBytes;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "password" })
    private User student;

    private int version;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DocumentStatus status = DocumentStatus.PENDING;

    @Builder.Default
    private LocalDateTime uploadDate = LocalDateTime.now();

    // Self-reference to track version history
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_document_id")
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "parentDocument", "hibernateLazyInitializer", "handler" })
    private Document parentDocument;
}
