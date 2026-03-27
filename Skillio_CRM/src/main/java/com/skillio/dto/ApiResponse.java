package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse {
    private String message;
    private boolean success;
    private Object data;
    
    // Constructor with message and success only
    public ApiResponse(String message, boolean success) {
        this.message = message;
        this.success = success;
    }
}
