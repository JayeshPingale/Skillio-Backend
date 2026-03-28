package com.skillio.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeadRequest {
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100)
    private String fullName;
    
    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9]{10}$")
    private String contactNumber;
    
    @Email(message = "Invalid email format")
    private String email;
    
    private String courseInterested;
    private String collegeName;
    private String qualification;
    private String experience;
    
    @NotNull(message = "Interest level is required")
    private String interestLevel;
    
    private String comments;
    
    // leadId NAHI - From URL
    // status NAHI - Separate endpoint (Change Status)
    // assignedTo NAHI - Separate endpoint (Assign Lead)
    // sourceId NAHI - Can't change source after creation
}
