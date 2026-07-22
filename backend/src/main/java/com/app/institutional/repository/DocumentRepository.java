package com.app.institutional.repository;

import com.app.institutional.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByStudentId(Long studentId);

    List<Document> findByParentDocumentId(Long parentDocumentId);
}
