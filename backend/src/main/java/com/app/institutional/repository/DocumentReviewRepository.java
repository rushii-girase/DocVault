package com.app.institutional.repository;

import com.app.institutional.entity.DocumentReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentReviewRepository extends JpaRepository<DocumentReview, Long> {
    List<DocumentReview> findByDocumentId(Long documentId);

    void deleteByDocumentId(Long documentId);
}
