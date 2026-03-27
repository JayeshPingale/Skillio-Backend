package com.skillio.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "targets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Target {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long targetId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Sales executive
    
    private String targetPeriod; // MONTHLY, QUARTERLY
    
    private Integer targetLeads; // Target number of leads
    private Integer targetEnrollments; // Target number of conversions to students
    
    @Column(precision = 12, scale = 2)
    private BigDecimal targetRevenue; // Target revenue in rupees
    
    private LocalDate startDate;
    private LocalDate endDate;
    
    // Achieved values
    private Integer achievedLeads = 0;
    private Integer achievedEnrollments = 0;
    
    @Column(precision = 12, scale = 2)
    private BigDecimal achievedRevenue = BigDecimal.ZERO;
    
    private String status; // ACTIVE, COMPLETED, ON_TRACK, AT_RISK
    
    @Column(columnDefinition = "TEXT")
    private String remarks;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.achievedLeads == null) {
            this.achievedLeads = 0;
        }
        if (this.achievedEnrollments == null) {
            this.achievedEnrollments = 0;
        }
        if (this.achievedRevenue == null) {
            this.achievedRevenue = BigDecimal.ZERO;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
