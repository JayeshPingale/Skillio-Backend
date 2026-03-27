package com.skillio.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadRequest {
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100)
    private String fullName;
    
    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be 10 digits")
    private String contactNumber;
    
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Course interested is required")
    private String courseInterested; // Java Fullstack, Software Testing, etc.
    
    private String collegeName;
    private String qualification; // B.Tech, MCA, etc.
    private String experience; // Fresher, 1-2 years, etc.
    
    @NotNull(message = "Interest level is required")
    private String interestLevel; // HIGH, MEDIUM, LOW
    
    @NotNull(message = "Lead source is required")
    private Long sourceId; // Instagram, Facebook, Website, etc.
    
    private String comments; // Additional notes
    
    // leadId NAHI - Auto-generated
    // status NAHI - Default "NEW"
    // assignedTo NAHI - Admin assigns later
    // createdDate NAHI - Auto set
}
