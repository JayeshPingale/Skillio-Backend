package com.skillio.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long auditId;
    
    private String entityType; // Lead, Student, Payment, Commission, etc.
    private Long entityId; // ID of the entity that was changed
    
    private String action; // CREATE, UPDATE, DELETE
    
    @Column(columnDefinition = "TEXT")
    private String oldValue; // JSON format of old data
    
    @Column(columnDefinition = "TEXT")
    private String newValue; // JSON format of new data
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "performed_by")
    private User performedBy; // User who performed the action
    
    private String ipAddress; // Request IP
    private String userAgent; // Browser/Client info
    
    private LocalDateTime performedAt;
    
    @PrePersist
    protected void onCreate() {
        this.performedAt = LocalDateTime.now();
    }
}
