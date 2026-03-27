package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentFeesResponse {
    
    private Long feesId;
    
    // Enrollment Info
    private Long enrollmentId;
    private Long studentId;
    private String studentCode;
    private String studentName;
    private String studentEmail;
    
    //batch 
    private Long batchId;
    private String batchName;
    
    private Long courseId;
    private String courseName;
    
    // Fees Details
    private BigDecimal totalFees;
    private BigDecimal paidAmount;
    private BigDecimal balanceAmount;
    private BigDecimal discountAmount;
    private String discountReason;
    
    private String paymentStatus; // PENDING, PARTIAL, FULL
    
    private LocalDate dueDate;
    private LocalDate lastPaymentDate;
    
    private String remarks;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
