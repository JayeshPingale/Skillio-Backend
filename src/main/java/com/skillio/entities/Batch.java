package com.skillio.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "batches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Batch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long batchId;
    
    private String batchCode; // Daffodil25A, Java_2025_Jan, etc.
    private String batchName;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course; // Which course this batch is for
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    private String timing; // 10:00 AM - 12:00 PM, 2:00 PM - 4:00 PM
    private String modeOfClass; // ONLINE, OFFLINE, HYBRID
    
    private Integer capacity; // Total seats available
    private Integer enrolledCount = 0; // Currently enrolled
    
    private String instructor; // Instructor name
    private String status; // UPCOMING, ONGOING, COMPLETED, CANCELLED
    
    @Column(columnDefinition = "TEXT")
    private String description;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "UPCOMING";
        }
        if (this.enrolledCount == null) {
            this.enrolledCount = 0;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
