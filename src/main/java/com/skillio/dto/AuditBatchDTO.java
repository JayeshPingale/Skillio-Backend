package com.skillio.dto;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ✅ Simplified DTO for Batch audit logging
 * No circular references, only essential fields
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditBatchDTO {
    private Long batchId;
    private String batchCode;
    private String batchName;
    private Long courseId;
    private String courseName;
    private LocalDate startDate;
    private LocalDate endDate;
    private String timing;
    private String modeOfClass;
    private String instructor;
    private String description;
    private String status;
    private Integer enrolledCount;
}
