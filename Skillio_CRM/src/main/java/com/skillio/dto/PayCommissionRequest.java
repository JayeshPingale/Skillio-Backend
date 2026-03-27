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
public class PayCommissionRequest {
    
    @NotNull(message = "Commission ID is required")
    private Long commissionId;
    
    @NotNull(message = "Amount to pay is required")
    @DecimalMin(value = "1.0", message = "Amount must be greater than 0")
    private BigDecimal amountPaid;
    
    @NotBlank(message = "Payment mode is required")
    private String paymentMode; // BANK_TRANSFER, CASH, CHEQUE
    
    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;
    
    private String transactionId; // Bank reference
    
    private String remarks;
    
    // commissionPaymentId NAHI - Auto-generated
}
