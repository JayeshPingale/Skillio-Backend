package com.skillio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadStatusChangeRequest {
    
    @NotNull(message = "Lead ID is required")
    private Long leadId;
    
    @NotBlank(message = "New status is required")
    private String newStatus; //  CONTACTED, INTERESTED, CONVERTED, LOST
    
    @NotBlank(message = "Remarks are required")
    private String remarks; // Why status changed
    
    // oldStatus NAHI - Will be fetched from database
    // changedBy NAHI - Auto set from logged-in user
}
