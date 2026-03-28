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
public class PaymentResponse {
    
    private Long paymentId;
    
    // Student Info
    private Long studentId;
    private String studentCode;
    private String studentName;
    private String studentEmail;
    
    // Fees Info
    private Long feesId;
    private BigDecimal totalFees;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;
    
    // Payment Info
    private BigDecimal amount;
    private String paymentMode;
    private LocalDate paymentDate;
    private String transactionId;
    private String receiptNumber;
    private String status; // SUCCESS, PENDING, FAILED
    
    // Admin Info
    private Long receivedByUserId;
    private String receivedByUserName;
    
    private String remarks;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
