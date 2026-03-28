package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentBatchInfoResponse {
    
    private String studentName;
    private String courseName;
    private String batchCode;
    private String batchName;
    
    private LocalDate startDate;
    private LocalDate endDate;
    private String timing; // 10:00 AM - 12:00 PM
    private String modeOfClass; // ONLINE, OFFLINE, HYBRID
    
    private String instructor;
    
    // Payment Info
    private BigDecimal totalFees;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;
    private String paymentStatus; // PENDING, PARTIAL, FULL
    
    // Additional Info
    private String remarks;
}
