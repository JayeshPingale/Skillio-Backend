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
public class CommissionPaymentResponse {
    
    private Long commissionPaymentId;
    private Long commissionId;
    
    // Sales Executive Info
    private Long salesExecutiveId;
    private String salesExecutiveName;
    private String salesExecutiveEmail;
    
    // Payment Info
    private BigDecimal amountPaid;
    private String paymentMode;
    private LocalDate paymentDate;
    private String transactionId;
    
    // Admin Info
    private Long paidByUserId;
    private String paidByUserName;
    
    private String remarks;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
