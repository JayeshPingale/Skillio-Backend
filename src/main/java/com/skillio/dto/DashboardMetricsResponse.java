package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardMetricsResponse {
    
    // Overall Stats
    private Long totalLeads;
    private Long totalStudents;
    private Long totalEnrollments;
    private BigDecimal totalRevenue;
    
    // This Month Stats
    private Long leadsThisMonth;
    private Long enrollmentsThisMonth;
    private BigDecimal revenueThisMonth;
    
    // Payment Stats
    private Long totalPaymentsPending;
    private Long totalPaymentsPartial;
    private Long totalPaymentsFull;
    
    // Commission Stats
    private BigDecimal commissionPending;
    private BigDecimal commissionEligible;
    private BigDecimal commissionPaid;
    
    // Top Performers
    private List<TopSalesExecutive> topSalesExecutives;
    
    // Lead Sources Performance
    private List<LeadSourcePerformance> leadSourcePerformance;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopSalesExecutive {
        private Long userId;
        private String userName;
        private Integer leadsWon;
        private Integer enrollmentsConverted;
        private BigDecimal revenue;
        private BigDecimal commissionEarned;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LeadSourcePerformance {
        private String sourceName;
        private Integer totalLeads;
        private Integer converted;
        private Double conversionRate; // (converted / totalLeads) * 100
    }
}
