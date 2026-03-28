package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    
    private Long auditId;
    
    private String entityType; // Lead, Student, Payment, Commission, etc.
    private Long entityId;
    private String action; // CREATE, UPDATE, DELETE
    
    private String oldValue; // JSON format of old data
    private String newValue; // JSON format of new data
    
    // User Info
    private Long performedByUserId;
    private String performedByUserName;
    private String performedByUserEmail;
    
    private String ipAddress;
    private String userAgent;
    
    private LocalDateTime performedAt;
}
