package com.skillio.services;

import com.skillio.dto.AuditLogResponse;
import com.skillio.entities.User;

import java.time.LocalDateTime;
import java.util.List;

public interface AuditLogService {
    
    // ✅ NEW: Create Audit Log (for all services to use)
    void createAuditLog(String entityType, Long entityId, String action, 
                       Object oldValue, Object newValue, User performedBy);
    
    // Get All Audit Logs
    List<AuditLogResponse> getAllAuditLogs();
    
    // Get Audit Logs by Entity Type (e.g., "Lead", "User", "Batch")
    List<AuditLogResponse> getAuditLogsByEntityType(String entityType);
    
    // Get Audit Logs for specific Entity (e.g., Lead ID 5, User ID 10)
    List<AuditLogResponse> getAuditLogsByEntityTypeAndId(String entityType, Long entityId);
    
    // Get Audit Logs by User (who performed actions)
    List<AuditLogResponse> getAuditLogsByUser(Long userId);
    
    // Get Audit Logs by Action (CREATE, UPDATE, DELETE)
    List<AuditLogResponse> getAuditLogsByAction(String action);
    
    // Get Audit Logs within Date Range
    List<AuditLogResponse> getAuditLogsByDateRange(LocalDateTime startDate, LocalDateTime endDate);
    
    // Get Single Audit Log by ID
    AuditLogResponse getAuditLogById(Long auditId);
}
