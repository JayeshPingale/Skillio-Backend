package com.skillio.repositories;

import com.skillio.entities.AuditLog;
import com.skillio.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
	List<AuditLog> findByEntityTypeAndEntityId(String entityType, Long entityId);

	List<AuditLog> findByPerformedBy(User performedBy);

	List<AuditLog> findByEntityTypeOrderByPerformedAtDesc(String entityType);

	List<AuditLog> findByAction(String action);

	List<AuditLog> findByPerformedAtBetween(LocalDateTime startDate, LocalDateTime endDate);
}
