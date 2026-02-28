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
@Table(name = "document_reviews")
public class DocumentReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "document_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "student", "parentDocument", "hibernateLazyInitializer",
            "handler" })
    private Document document;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler", "password" })
    private User reviewer; // The STAFF member

    @Column(columnDefinition = "TEXT")
    private String remarkText;

    @Enumerated(EnumType.STRING)
    private DocumentStatus statusDecision;

    @Builder.Default
    private LocalDateTime reviewDate = LocalDateTime.now();
}
