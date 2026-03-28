package com.skillio.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FollowUpRequest {
    
    @NotNull(message = "Lead ID is required")
    @Positive(message = "Lead ID must be positive")
    private Long leadId;
    
    @NotNull(message = "Follow-up date is required")
    @FutureOrPresent(message = "Follow-up date must be today or in future")
    private LocalDate followUpDate;
    
    @NotBlank(message = "Follow-up type is required")
    @Pattern(regexp = "CALL|EMAIL|VISIT|WHATSAPP", message = "Follow-up type must be CALL, EMAIL, VISIT, or WHATSAPP")
    private String followUpType;
    
    @NotBlank(message = "Notes are required")
    @Size(min = 5, max = 500, message = "Notes must be between 5 and 500 characters")
    private String notes;
    
    @NotNull(message = "Next follow-up date is required")
    @Future(message = "Next follow-up date must be in future")
    private LocalDate nextFollowUpDate;
}
