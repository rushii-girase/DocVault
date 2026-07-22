package com.app.institutional.repository;

import com.app.institutional.entity.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    List<AuditLog> findByEntityNameAndEntityIdOrderByActionDateDesc(String entityName, Long entityId);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM AuditLog a JOIN FETCH a.actor ORDER BY a.actionDate DESC")
    List<AuditLog> findAllWithActor();

    void deleteByActorId(Long actorId);
}
