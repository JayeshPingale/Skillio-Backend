package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TargetResponse {
    
    private Long targetId;
    
    // Sales Executive Info
    private Long userId;
    private String userName;
    private String userEmail;
    
    private String targetPeriod;
    
    // Target Values
    private Integer targetLeads;
    private Integer targetEnrollments;
    private BigDecimal targetRevenue;
    
    // Achieved Values
    private Integer achievedLeads;
    private Integer achievedEnrollments;
    private BigDecimal achievedRevenue;
    
    // Calculated Percentages
    private Double leadsAchievementPercentage; // (achievedLeads / targetLeads) * 100
    private Double enrollmentsAchievementPercentage;
    private Double revenueAchievementPercentage;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // ACTIVE, COMPLETED, ON_TRACK, AT_RISK
    
    private String remarks;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

