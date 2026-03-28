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
public class CreateCommissionPaymentRequest {

    @NotNull(message = "Commission ID is required")
    private Long commissionId;

    @NotNull(message = "Amount paid is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Amount must be greater than 0")
    private BigDecimal amountPaid;

    @NotNull(message = "Payment date is required")
    private LocalDate paymentDate;

    @NotBlank(message = "Payment mode is required")
    private String paymentMode; // BANK_TRANSFER, CASH, CHEQUE, UPI

    @Size(max = 100, message = "Transaction ID cannot exceed 100 characters")
    private String transactionId; // Bank/Payment reference

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    // paidBy will be auto-set from logged-in admin
    // Commission status will be auto-updated to PAID
}
