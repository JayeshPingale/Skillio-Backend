package com.skillio.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    
    private Long invoiceId;
    private Long paymentId;
    private String invoiceNumber;
    
    // Student Info
    private Long studentId;
    private String studentCode;
    private String studentName;
    private String studentEmail;
    
    // Payment Info
    private String paymentMode;
    private BigDecimal amount;  // ✅ ADD THIS LINE
    private LocalDate paymentDate;
    private String transactionId;
    private String receiptNumber;
    
    private LocalDateTime generatedDate;
    private String pdfPath;
    
    private Boolean sentToEmail;
    private Boolean sentToWhatsApp;
    private LocalDateTime sentDate;
    
    private String remarks;
    
    private LocalDateTime createdAt;
}
