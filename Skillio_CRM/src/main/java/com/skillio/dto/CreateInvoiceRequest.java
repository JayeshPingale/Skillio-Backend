package com.skillio.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceRequest {
    
    @NotNull(message = "Payment ID is required")
    private Long paymentId;
    
    // Other fields auto-generated
    // invoiceNumber, generatedDate, pdfPath
}
