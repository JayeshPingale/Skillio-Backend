package com.skillio.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    
    @NotBlank(message = "Full name is required")
    @Size(min = 2, max = 100)
    private String fullName;
    
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
    
    @NotBlank(message = "Contact number is required")
    @Pattern(regexp = "^[0-9]{10}$")
    private String contactNumber;
    
    private String profilePic;
    
    private Long roleId;  // Role change ke liye add karein
    
    private Boolean isActive;  // ✅ Yeh line add karein
}
