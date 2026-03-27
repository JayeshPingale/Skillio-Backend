package com.skillio.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentInstallmentRequest {
    
    @NotNull(message = "Fees ID is required")
    private Long feesId;
    
    @NotNull(message = "Number of installments is required")
    @Min(value = 1, message = "Minimum 1 installment")
    @Max(value = 12, message = "Maximum 12 installments")
    private Integer numberOfInstallments;
    
    // Example: If totalFees = 30000 and numberOfInstallments = 3
    // System will auto-create 3 installments of 10000 each
    // with due dates 30 days apart
}
