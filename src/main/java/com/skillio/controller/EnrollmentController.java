package com.skillio.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import com.skillio.dto.*;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.UserRepository;
import com.skillio.services.EnrollmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Slf4j
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final UserRepository userRepository;

    // Create Enrollment (Admin only)
    @PostMapping
    @PreAuthorize("hasAuthority('ENROLLMENT_CREATE')")
    public ResponseEntity<EnrollmentResponse> createEnrollment(
            @Valid @RequestBody CreateEnrollmentRequest request,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        EnrollmentResponse response = enrollmentService.createEnrollment(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Convert Lead and Enroll (Admin + Sales Executive)
    @PostMapping("/convert-from-lead")
    @PreAuthorize("hasAuthority('ENROLLMENT_CREATE')")
    public ResponseEntity<ConvertLeadAndEnrollResponse> convertLeadAndEnroll(
            @Valid @RequestBody CreateEnrollmentFromLeadRequest request,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        log.info("🔥 Converting lead to enrollment by user: {}", userId);
        ConvertLeadAndEnrollResponse response = 
            enrollmentService.convertLeadAndEnroll(request, userId);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ✅ FIXED: Update Enrollment (Admin + Sales Executive can update)
    @PutMapping("/{enrollmentId}")
    @PreAuthorize("hasAuthority('ENROLLMENT_UPDATE')")
    public ResponseEntity<EnrollmentResponse> updateEnrollment(
            @PathVariable Long enrollmentId,
            @Valid @RequestBody UpdateEnrollmentRequest request) {
        
        log.info("🔥 Updating enrollment ID: {}", enrollmentId);
        return ResponseEntity.ok(enrollmentService.updateEnrollment(enrollmentId, request));
    }

    // Get Enrollment by ID
    @GetMapping("/{enrollmentId}")
    @PreAuthorize("hasAuthority('ENROLLMENT_READ')")
    public ResponseEntity<EnrollmentResponse> getEnrollmentById(@PathVariable Long enrollmentId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentById(enrollmentId));
    }

    // Get All Enrollments (Admin only)
    @GetMapping
    @PreAuthorize("hasAuthority('ENROLLMENT_LIST')")
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments() {
        return ResponseEntity.ok(enrollmentService.getAllEnrollments());
    }

    // Get My Enrollments (Sales Executive)
    @GetMapping("/my-enrollments")
    @PreAuthorize("hasAuthority('ENROLLMENT_LIST')")
    public ResponseEntity<List<EnrollmentResponse>> getMyEnrollments(Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByAdmittedUser(userId));
    }

    // Get Enrollments by Student
    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAuthority('ENROLLMENT_LIST')")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsByStudent(@PathVariable Long studentId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByStudent(studentId));
    }

    // Get Enrollments by Batch
    @GetMapping("/batch/{batchId}")
    @PreAuthorize("hasAuthority('ENROLLMENT_LIST')")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsByBatch(@PathVariable Long batchId) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentsByBatch(batchId));
    }

    // Delete Enrollment (Admin only)
    @DeleteMapping("/{enrollmentId}")
    @PreAuthorize("hasAuthority('ENROLLMENT_DELETE')")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Long enrollmentId) {
        enrollmentService.deleteEnrollment(enrollmentId);
        return ResponseEntity.noContent().build();
    }

    // Change status (Admin only)
    @PatchMapping("/{enrollmentId}/status")
    @PreAuthorize("hasAuthority('ENROLLMENT_UPDATE')")
    public ResponseEntity<Void> changeStatus(
            @PathVariable Long enrollmentId,
            @RequestParam String status) {
        
        enrollmentService.changeEnrollmentStatus(enrollmentId, status);
        return ResponseEntity.noContent().build();
    }
    private Long extractUserId(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return user.getUserId();
    }
}
