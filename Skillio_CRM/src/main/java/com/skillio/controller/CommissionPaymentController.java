package com.skillio.controller;

import com.skillio.dto.CreateCommissionPaymentRequest;
import com.skillio.dto.UpdateCommissionPaymentRequest;
import com.skillio.dto.CommissionPaymentResponse;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.UserRepository;
import com.skillio.services.CommissionPaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;  // ✅ ADD
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/commission-payments")
@RequiredArgsConstructor
@Slf4j  // ✅ ADD
public class CommissionPaymentController {

    private final CommissionPaymentService commissionPaymentService;
    private final UserRepository userRepository;  // ✅ ADD

    // ==================== CREATE ====================
    @PostMapping
    @PreAuthorize("hasAuthority('COMMISSION_PAYMENT_CREATE')")
    public ResponseEntity<CommissionPaymentResponse> payCommission(
            @Valid @RequestBody CreateCommissionPaymentRequest request,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        CommissionPaymentResponse response = commissionPaymentService.payCommission(request, loggedInUserId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ==================== READ ====================
    @GetMapping
    @PreAuthorize("hasAuthority('COMMISSION_PAYMENT_LIST')")
    public ResponseEntity<List<CommissionPaymentResponse>> getAllCommissionPayments() {
        log.info("🔥 Fetching all commission payments");
        List<CommissionPaymentResponse> payments = commissionPaymentService.getAllCommissionPayments();
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{commissionPaymentId}")
    @PreAuthorize("hasAuthority('COMMISSION_PAYMENT_READ')")
    public ResponseEntity<CommissionPaymentResponse> getCommissionPaymentById(@PathVariable Long commissionPaymentId) {
        CommissionPaymentResponse payment = commissionPaymentService.getCommissionPaymentById(commissionPaymentId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/commission/{commissionId}")
    @PreAuthorize("hasAuthority('COMMISSION_PAYMENT_READ')")
    public ResponseEntity<CommissionPaymentResponse> getCommissionPaymentByCommissionId(@PathVariable Long commissionId) {
        CommissionPaymentResponse payment = commissionPaymentService.getCommissionPaymentByCommissionId(commissionId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/paid-by/{paidByUserId}")
    @PreAuthorize("hasAuthority('COMMISSION_PAYMENT_LIST')")
    public ResponseEntity<List<CommissionPaymentResponse>> getCommissionPaymentsPaidBy(@PathVariable Long paidByUserId) {
        List<CommissionPaymentResponse> payments = commissionPaymentService.getCommissionPaymentsPaidBy(paidByUserId);
        return ResponseEntity.ok(payments);
    }

    // ==================== UPDATE ====================
    @PutMapping("/{commissionPaymentId}")
    @PreAuthorize("hasAuthority('COMMISSION_PAYMENT_UPDATE')")
    public ResponseEntity<CommissionPaymentResponse> updateCommissionPayment(
            @PathVariable Long commissionPaymentId,
            @Valid @RequestBody UpdateCommissionPaymentRequest request,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        CommissionPaymentResponse updated = commissionPaymentService.updateCommissionPayment(commissionPaymentId, request, loggedInUserId);
        return ResponseEntity.ok(updated);
    }

    // ==================== DELETE ====================
    @DeleteMapping("/{commissionPaymentId}")
    @PreAuthorize("hasAuthority('COMMISSION_PAYMENT_DELETE')")
    public ResponseEntity<Map<String, String>> deleteCommissionPayment(
            @PathVariable Long commissionPaymentId,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        commissionPaymentService.deleteCommissionPayment(commissionPaymentId, loggedInUserId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Commission payment deleted successfully and commission reverted to ELIGIBLE");
        return ResponseEntity.ok(response);
    }

    // ==================== HELPER ====================
    private Long extractUserId(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("User not authenticated");
        }
        
        String email = authentication.getName();
        log.info("🔥 Extracting userId for email: {}", email);
        
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        log.info("🔥 Found userId: {} (role: {}) for email: {}", 
            user.getUserId(), user.getRole().getRoleName(), email);
        return user.getUserId();
    }
}
