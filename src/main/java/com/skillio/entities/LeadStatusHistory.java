package com.skillio.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "lead_status_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lead_id", nullable = false)
    private Lead lead;
    
    private String oldStatus; // Previous status (NEW, CONTACTED, INTERESTED, etc.)
    private String newStatus; // Current status
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by")
    private User changedBy; // User who changed the status
    
    @Column(columnDefinition = "TEXT")
    private String remarks; // Why status changed
    
    private LocalDateTime changedAt;
    
    @PrePersist
    protected void onCreate() {
        this.changedAt = LocalDateTime.now();
    }
}
