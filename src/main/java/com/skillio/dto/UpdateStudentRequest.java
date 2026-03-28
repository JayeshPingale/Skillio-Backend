package com.skillio.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateStudentRequest {
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100)
    private String fullName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9]{10}$")
    private String contactNumber;
    
    @Pattern(regexp = "^[0-9]{10}$")
    private String alternateContact;
    
    @NotBlank(message = "Address is required")
    @Size(min = 10, max = 500)
    private String address;
    
    private String remarks;
    

	public LocalDate DateOfBirth;
    
    // studentId NAHI - From URL
    // studentCode NAHI - Can't change code
    // status NAHI - Separate endpoint (Change Student Status)
}
