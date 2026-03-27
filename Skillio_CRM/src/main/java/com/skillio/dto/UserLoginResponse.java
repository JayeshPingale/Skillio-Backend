package com.skillio.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginResponse {
    
    private String token;        // JWT token
    private String type;         // "Bearer"
    private Long userId;         // Extra info
    private String roleName;     // Extra info
    private String fullName;
    private List<String> permissions;

    
    
}
