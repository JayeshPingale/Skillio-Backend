package com.skillio.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "enrollments", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"student_id", "batch_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long enrollmentId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", nullable = false)
    private Batch batch;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;
    
    private LocalDate enrollmentDate;
    
    @Column(precision = 10, scale = 2)
    private BigDecimal totalCourseFees; // Amount to be paid
    
    private String status; // ACTIVE, COMPLETED, CANCELLED, ON_HOLD
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admitted_by", nullable = false)
    private User admittedBy; // Admin who admitted the student
    
 // for discount on total fees 
    private Double discountPercentage;  // 0-50% tak
    private Double discountAmount;      // max 25000
    private String discountReason;

    
    @Column(columnDefinition = "TEXT")
    private String remarks;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) {
            this.status = "ACTIVE";
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
