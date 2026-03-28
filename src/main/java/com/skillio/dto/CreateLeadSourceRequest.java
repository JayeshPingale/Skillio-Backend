package com.skillio.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadSourceRequest {
    
    @NotBlank(message = "Source name is required")
    @Size(min = 3, max = 50)
    private String name; // Instagram, Facebook, Website, Referral, Email, Cold_Call
    
    @NotBlank(message = "Channel is required")
    private String channel; // SOCIAL_MEDIA, ORGANIC, PAID_ADS, REFERRAL, COLD_CALLING
    
    @NotBlank(message = "Description is required")
    @Size(min = 5, max = 200)
    private String description;
    
    // sourceId NAHI - Auto-generated
}
