package com.skillio.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {
    
    @NotNull(message = "Student Fees ID is required")
    private Long feesId;
    
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "1.0", message = "Amount must be greater than 0")
    private BigDecimal amount;
    
    @NotBlank(message = "Payment mode is required")
    private String paymentMode; // CASH, CARD, UPI, NETBANKING, CHEQUE
    
    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;
    
    private String transactionId; // For online payments
    
    private String remarks;
    
    // paymentId NAHI - Auto-generated
    // status NAHI - Default "SUCCESS"
    // receivedBy NAHI - Auto-set from logged-in admin
    // receiptNumber NAHI - Auto-generated
}
