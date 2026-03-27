package com.skillio.controller;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillio.dto.CreateStudentFeesRequest;
import com.skillio.dto.PaymentResponse;
import com.skillio.dto.StudentFeesResponse;
import com.skillio.dto.UpdateStudentFeesRequest;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.UserRepository;
import com.skillio.services.PaymentService;
import com.skillio.services.StudentFeesService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/student-fees")
@Slf4j  
@RequiredArgsConstructor
public class StudentFeesController {

    private final StudentFeesService studentFeesService;
    private final PaymentService paymentService; 
    private final UserRepository userRepository; 


    // ==================== CREATE ====================

    @PostMapping
    @PreAuthorize("hasAnyAuthority('STUDENT_FEE_CREATE', 'STUDENT_FEES_CREATE')")
    public ResponseEntity<StudentFeesResponse> createStudentFees(
            @Valid @RequestBody CreateStudentFeesRequest request,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        StudentFeesResponse response = studentFeesService.createStudentFees(request, loggedInUserId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ==================== READ ====================

    @GetMapping
    @PreAuthorize("hasAnyAuthority('STUDENT_FEE_LIST', 'STUDENT_FEES_LIST')")
    public ResponseEntity<List<StudentFeesResponse>> getAllStudentFees() {
        List<StudentFeesResponse> fees = studentFeesService.getAllStudentFees();
        return ResponseEntity.ok(fees);
    }
    // ✅ ADD THIS ENDPOINT
    @GetMapping("/my-students")
    @PreAuthorize("hasAnyAuthority('STUDENT_FEE_LIST', 'STUDENT_FEES_LIST')")
    public ResponseEntity<List<StudentFeesResponse>> getMyStudentFees(Authentication authentication) {
        Long salesExecutiveId = extractUserId(authentication);
        List<StudentFeesResponse> fees = studentFeesService.getStudentFeesBySalesExecutive(salesExecutiveId);
        return ResponseEntity.ok(fees);
    }

    @GetMapping("/{feesId}")
    @PreAuthorize("hasAnyAuthority('STUDENT_FEE_READ', 'STUDENT_FEES_READ')")
    public ResponseEntity<StudentFeesResponse> getStudentFeesById(@PathVariable Long feesId) {
        StudentFeesResponse fees = studentFeesService.getStudentFeesById(feesId);
        return ResponseEntity.ok(fees);
    }

    @GetMapping("/enrollment/{enrollmentId}")
    @PreAuthorize("hasAnyAuthority('STUDENT_FEE_READ', 'STUDENT_FEES_READ')")
    public ResponseEntity<StudentFeesResponse> getStudentFeesByEnrollmentId(@PathVariable Long enrollmentId) {
        StudentFeesResponse fees = studentFeesService.getStudentFeesByEnrollmentId(enrollmentId);
        return ResponseEntity.ok(fees);
    }

    @GetMapping("/status/{paymentStatus}")
    @PreAuthorize("hasAnyAuthority('STUDENT_FEE_LIST', 'STUDENT_FEES_LIST')")
    public ResponseEntity<List<StudentFeesResponse>> getStudentFeesByPaymentStatus(@PathVariable String paymentStatus) {
        List<StudentFeesResponse> fees = studentFeesService.getStudentFeesByPaymentStatus(paymentStatus);
        return ResponseEntity.ok(fees);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAnyAuthority('STUDENT_FEE_LIST', 'STUDENT_FEES_LIST')")
    public ResponseEntity<List<StudentFeesResponse>> getOverdueStudentFees() {
        List<StudentFeesResponse> fees = studentFeesService.getOverdueStudentFees();
        return ResponseEntity.ok(fees);
    }




    // ==================== UPDATE ====================

    @PutMapping("/{feesId}")
    @PreAuthorize("hasAnyAuthority('STUDENT_FEE_UPDATE', 'STUDENT_FEES_UPDATE')")
    public ResponseEntity<StudentFeesResponse> updateStudentFees(
            @PathVariable Long feesId,
            @Valid @RequestBody UpdateStudentFeesRequest request,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        StudentFeesResponse updated = studentFeesService.updateStudentFees(feesId, request, loggedInUserId);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{feesId}/discount")
    @PreAuthorize("hasAnyAuthority('STUDENT_FEE_UPDATE', 'STUDENT_FEES_UPDATE')")
    public ResponseEntity<StudentFeesResponse> applyDiscount(
            @PathVariable Long feesId,
            @RequestParam BigDecimal discountAmount,
            @RequestParam String discountReason,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        StudentFeesResponse updated = studentFeesService.applyDiscount(feesId, discountAmount, discountReason, loggedInUserId);
        return ResponseEntity.ok(updated);
    }

    // ==================== DELETE ====================

    @DeleteMapping("/{feesId}")
    @PreAuthorize("hasAnyAuthority('STUDENT_FEE_DELETE', 'STUDENT_FEES_DELETE')")
    public ResponseEntity<Map<String, String>> deleteStudentFees(
            @PathVariable Long feesId,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        studentFeesService.deleteStudentFees(feesId, loggedInUserId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Student fees deleted successfully");
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{feesId}/payments")
    @PreAuthorize("hasAuthority('PAYMENT_LIST')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByFeesId(@PathVariable Long feesId) {
        // Ye endpoint student fees ki saari payments fetch karega
        List<PaymentResponse> payments = paymentService.getPaymentsByStudentFees(feesId);
        return ResponseEntity.ok(payments);
    }

    // ==================== HELPER ====================

    private Long extractUserId(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("User not authenticated");
        }
        
        // ✅ JWT mein email hoga, not username
        String email = authentication.getName();
        log.info("🔥 Extracting userId for email: {}", email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        log.info("🔥 Found userId: {} for email: {}", user.getUserId(), email);
        return user.getUserId();
    }

}
