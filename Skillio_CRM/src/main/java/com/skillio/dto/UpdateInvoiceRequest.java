package com.skillio.dto;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateInvoiceRequest {

    @Size(max = 500, message = "PDF path cannot exceed 500 characters")
    private String pdfPath;

    @Size(max = 500, message = "Remarks cannot exceed 500 characters")
    private String remarks;

    // invoiceNumber and paymentId cannot be changed
    // sentToEmail and sentToWhatsApp are updated via separate endpoints
}
