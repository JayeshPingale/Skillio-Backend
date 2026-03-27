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
public class PaymentInstallmentResponse {
    
    private Long installmentId;
    private Long feesId;
    
    // Student Info
    private Long studentId;
    private String studentCode;
    private String studentName;
    
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal amount;
    private String status; // PENDING, PAID, OVERDUE
    private LocalDate paidDate;
    
    private Long paymentId; // If paid
    
    private String remarks;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
