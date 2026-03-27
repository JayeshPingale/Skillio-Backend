package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowUpResponse {
    
    private Long followUpId;
    private Long leadId;
    private String leadName;
    
    private LocalDate followUpDate;
    private String followUpType; // CALL, EMAIL, VISIT, WHATSAPP
    private String notes;
    private LocalDate nextFollowUpDate;
    private String status; // SCHEDULED, COMPLETED, MISSED
    
    private Long createdByUserId;
    private String createdByUserName;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
