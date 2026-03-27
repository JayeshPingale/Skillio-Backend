package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnrolledStudentCommissionView {

    private Long enrollmentId;
    private Long studentId;
    private String studentName;
    private String studentCode;
    private String courseName;
    private String enrollmentDate;

    // Payment progress
    private BigDecimal totalCourseFee;
    private BigDecimal totalFeesPaid;
    private BigDecimal totalFeesPending;

    // Commission info (null if no request yet)
    private Long commissionId;
    private String commissionStatus;
    private BigDecimal eligibleAmount;
    private String requestedRemarks;
    private String adminComments;
    private Integer commissionAttemptsUsed;
    private Integer commissionAttemptsRemaining;
    private Boolean canRequestCommission;
    private String lastAttemptStatus;
}
