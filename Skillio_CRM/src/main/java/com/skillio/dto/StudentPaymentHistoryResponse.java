package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentPaymentHistoryResponse {
    
    private String studentName;
    private String courseName;
    private String batchCode;
    
    // Fees Summary
    private BigDecimal totalFees;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;
    private BigDecimal discountAmount;
    private String paymentStatus; // PENDING, PARTIAL, FULL
    
    private LocalDate dueDate;
    
    // Payment History
    private List<PaymentHistoryItem> paymentHistory;
    
    // Installments (if applicable)
    private List<InstallmentHistoryItem> installments;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PaymentHistoryItem {
        private LocalDate paymentDate;
        private BigDecimal amount;
        private String mode;
        private String receiptNumber;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class InstallmentHistoryItem {
        private Integer installmentNumber;
        private LocalDate dueDate;
        private BigDecimal amount;
        private String status;
        private LocalDate paidDate;
    }
}
