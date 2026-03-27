package com.skillio.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateStudentRequest {
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100)
    private String fullName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Contact number must be 10 digits")
    private String contactNumber;
    
    @Pattern(regexp = "^[0-9]{10}$", message = "Alternate contact must be 10 digits")
    private String alternateContact; // Parent/Guardian contact
    
    @NotBlank(message = "Address is required")
    @Size(min = 10, max = 500)
    private String address;
    
    @NotNull(message = "Date of birth is required")
    @Past(message = "Date of birth must be in the past")
    private LocalDate  dateOfBirth;
//    private String remarks;
    
    // studentId NAHI - Auto-generated
    // studentCode NAHI - Auto-generated (e.g., STU001)
    // enrollmentDate NAHI - Auto-set
    // status NAHI - Default "ACTIVE"
}
