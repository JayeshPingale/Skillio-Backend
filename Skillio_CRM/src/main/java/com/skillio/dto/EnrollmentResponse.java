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
public class EnrollmentResponse {
    
    private Long enrollmentId;
    
    // Student Info
    private Long studentId;
    private String studentCode;
    private String studentName;
    private String studentEmail;
    
    // Batch Info
    private Long batchId;
    private String batchCode;
    private String batchName;
    
    // Course Info
    private Long courseId;
    private String courseName;
    
    private LocalDate enrollmentDate;
    private BigDecimal totalCourseFees;
    private String status; // ACTIVE, COMPLETED, CANCELLED, ON_HOLD
    
    //discount 
    private Double discountPercentage;
    private Double discountAmount;
    private String discountReason;

    
    // Admin Info
    private Long admittedByUserId;
    private String admittedByUserName;
    
    private String remarks;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
