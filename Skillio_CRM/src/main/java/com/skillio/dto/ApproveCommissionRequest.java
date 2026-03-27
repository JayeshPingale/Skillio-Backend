package com.skillio.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApproveCommissionRequest {
 
 @NotNull(message = "Commission ID is required")
 private Long commissionId;
 
 @NotNull(message = "Decision is required")  
 private Boolean approved; // ✅ ADD: true = approve, false = reject
 
 @NotBlank(message = "Comments are required")
 @Size(max = 500)
 private String comments; // ✅ ADD: Admin ka comment mandatory
 
 // Payment details (only when approved = true)
 private BigDecimal amountPaid;
 private String paymentMode; // BANK_TRANSFER, CASH, CHEQUE, UPI
 private String transactionId;
}
