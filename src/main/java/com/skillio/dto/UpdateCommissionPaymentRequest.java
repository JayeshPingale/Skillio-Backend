package com.skillio.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateCommissionPaymentRequest {

    private LocalDate paymentDate;

    @Size(max = 100, message = "Transaction ID cannot exceed 100 characters")
    private String transactionId;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    // amountPaid and paymentMode cannot be changed after creation
    // commissionId cannot be changed
}
