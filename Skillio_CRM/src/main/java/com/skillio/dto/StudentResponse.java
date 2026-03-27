package com.skillio.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentResponse {

	private Long studentId;
	private String studentCode;

	private String fullName;
	private String email;
	private String contactNumber;
	private String alternateContact;
	private String address;

	private LocalDate enrollmentDate;
	private String status; // ACTIVE, COMPLETED, DROPPED, ON_HOLD

	private String remarks;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	private LocalDate dateOfBirth;
}
