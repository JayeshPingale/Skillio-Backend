package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadResponse {
    
    private Long leadId;
    private String fullName;
    private String contactNumber;
    private String email;
    private String courseInterested;
    private String collegeName;
    private String qualification;
    private String experience;
    
    private String status; // NEW, CONTACTED, INTERESTED, CONVERTED, LOST
    private String interestLevel; // HIGH, MEDIUM, LOW
    
    private Long assignedToUserId;
    private String assignedToUserName;
    
    private Long salesExecutiveId;
    private String salesExecutiveName;
    
    // ✅ ADD THIS - Converted Student ID
    private Long convertedStudentId;
    
    private Long sourceId;
    private String sourceName; // Instagram, Facebook, etc.
    
    private String comments;
    
    private LocalDateTime createdDate;
    private LocalDate lastContactDate;
    private LocalDate conversionDate;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
