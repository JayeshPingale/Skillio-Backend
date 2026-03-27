package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SalesExecutiveDashboardResponse {
    
    // Personal Stats
    private String userName;
    private String userEmail;
    
    // Lead Stats
    private Integer leadsWon; // Status = CONVERTED
    private Integer leadsLost; // Status = LOST
    private Integer leadsInProgress; // Status = CONTACTED, INTERESTED
    
    // Enrollment Stats
    private Integer totalEnrollments;
    private Integer activeEnrollments;
    
    // Revenue Stats
    private BigDecimal totalRevenueGenerated;
    
    // Commission Stats
    private BigDecimal commissionPending;
    private BigDecimal commissionEligible;
    private BigDecimal commissionPaid;
    
    // Target Achievement
    private TargetAchievementInfo targetAchievement;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TargetAchievementInfo {
        private String period; // MONTHLY, QUARTERLY
        private Integer targetLeads;
        private Integer achievedLeads;
        private Double leadsPercentage;
        
        private Integer targetEnrollments;
        private Integer achievedEnrollments;
        private Double enrollmentsPercentage;
        
        private BigDecimal targetRevenue;//500000
        private BigDecimal achievedRevenue;//100000
        private Double revenuePercentage;//
        
        private String status; // ON_TRACK, AT_RISK
    }
}
