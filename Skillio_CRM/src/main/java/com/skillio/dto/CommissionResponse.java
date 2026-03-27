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
public class CommissionResponse {
private Long commissionId;
    
    // Enrollment Info
    private Long enrollmentId;
    private Long studentId;
    private String studentCode;
    private String studentName;
    
    // Course Info
    private String courseName;
    
    // Sales Executive Info
    private Long salesExecutiveId;
    private String salesExecutiveName;
    
    // Commission Details
    private BigDecimal totalCourseFees; // ✅ After discount
    
    // ✅ OPTIONAL: Add these
    private BigDecimal originalCourseFees; // Before discount
    private BigDecimal discountAmount; // Discount applied
    
    private BigDecimal commissionPercentage;
    private BigDecimal eligibleAmount;
    
    private String status;
    private String eligibilityCondition;
    private LocalDate eligibilityDate;
    private LocalDate paidDate;
    
    private String remarks;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;	 	
    private String adminComments;       // Admin's comment on approval/rejection
    private String requestedRemarks;    // Sales Exec's original request note

    // Payment info (when PAID)
    private BigDecimal amountPaid;
    private String paymentMode;
    private String transactionId;
    private Long commissionPaymentId;

    // Student payment progress (for Sales Exec view)
    private BigDecimal totalFeesPaid;   // Kitna payment hua hai
    private BigDecimal totalFeesPending; // Kitna bakki hai
    private BigDecimal totalCourseFee;   // Total fee
}
