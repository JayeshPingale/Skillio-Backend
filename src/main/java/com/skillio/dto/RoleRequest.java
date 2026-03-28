package com.skillio.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class RoleRequest {

	@NotBlank(message = "Role name is required")
	@Pattern(regexp = "^ROLE_.*", message = "Role name must start with 'ROLE_'")
    @Size(min = 4, max = 50, message = "Role name must be 5-50 characters")
	private String roleName;
	
    @NotBlank(message = "Description is required")
    @Size(min = 10, max = 200, message = "Description must be 10-200 characters")
    private String description;
}
