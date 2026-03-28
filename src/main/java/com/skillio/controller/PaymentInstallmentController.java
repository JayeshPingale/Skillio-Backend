package com.skillio.controller;

import com.skillio.dto.CreatePaymentInstallmentRequest;
import com.skillio.dto.UpdatePaymentInstallmentRequest;
import com.skillio.dto.PaymentInstallmentResponse;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.UserRepository;
import com.skillio.services.PaymentInstallmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payment-installments")
@RequiredArgsConstructor
public class PaymentInstallmentController {

    private final PaymentInstallmentService installmentService;
    private final UserRepository userRepository;

    // ==================== CREATE ====================

    @PostMapping
    @PreAuthorize("hasAuthority('PAYMENT_INSTALLMENT_CREATE')")
    public ResponseEntity<List<PaymentInstallmentResponse>> createInstallmentPlan(
            @Valid @RequestBody CreatePaymentInstallmentRequest request,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        List<PaymentInstallmentResponse> installments = installmentService.createInstallmentPlan(request, loggedInUserId);
        return new ResponseEntity<>(installments, HttpStatus.CREATED);
    }

    // ==================== READ ====================

    @GetMapping
    @PreAuthorize("hasAuthority('PAYMENT_INSTALLMENT_LIST')")
    public ResponseEntity<List<PaymentInstallmentResponse>> getAllInstallments() {
        List<PaymentInstallmentResponse> installments = installmentService.getAllInstallments();
        return ResponseEntity.ok(installments);
    }

    @GetMapping("/{installmentId}")
    @PreAuthorize("hasAuthority('PAYMENT_INSTALLMENT_READ')")
    public ResponseEntity<PaymentInstallmentResponse> getInstallmentById(@PathVariable Long installmentId) {
        PaymentInstallmentResponse installment = installmentService.getInstallmentById(installmentId);
        return ResponseEntity.ok(installment);
    }

    @GetMapping("/fees/{feesId}")
    @PreAuthorize("hasAuthority('PAYMENT_INSTALLMENT_LIST')")
    public ResponseEntity<List<PaymentInstallmentResponse>> getInstallmentsByFeesId(@PathVariable Long feesId) {
        List<PaymentInstallmentResponse> installments = installmentService.getInstallmentsByStudentFeesId(feesId);
        return ResponseEntity.ok(installments);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('PAYMENT_INSTALLMENT_LIST')")
    public ResponseEntity<List<PaymentInstallmentResponse>> getInstallmentsByStatus(@PathVariable String status) {
        List<PaymentInstallmentResponse> installments = installmentService.getInstallmentsByStatus(status);
        return ResponseEntity.ok(installments);
    }

    @GetMapping("/overdue")
    @PreAuthorize("hasAuthority('PAYMENT_INSTALLMENT_LIST')")
    public ResponseEntity<List<PaymentInstallmentResponse>> getOverdueInstallments() {
        List<PaymentInstallmentResponse> installments = installmentService.getOverdueInstallments();
        return ResponseEntity.ok(installments);
    }

    @GetMapping("/pending")
    @PreAuthorize("hasAuthority('PAYMENT_INSTALLMENT_LIST')")
    public ResponseEntity<List<PaymentInstallmentResponse>> getPendingInstallments() {
        List<PaymentInstallmentResponse> installments = installmentService.getPendingInstallments();
        return ResponseEntity.ok(installments);
    }

    // ==================== UPDATE ====================

    @PutMapping("/{installmentId}")
    @PreAuthorize("hasAuthority('PAYMENT_INSTALLMENT_UPDATE')")
    public ResponseEntity<PaymentInstallmentResponse> updateInstallment(
            @PathVariable Long installmentId,
            @Valid @RequestBody UpdatePaymentInstallmentRequest request,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        PaymentInstallmentResponse updated = installmentService.updateInstallment(installmentId, request, loggedInUserId);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{installmentId}/mark-paid")
    @PreAuthorize("hasAuthority('PAYMENT_INSTALLMENT_UPDATE')")
    public ResponseEntity<PaymentInstallmentResponse> markAsPaid(
            @PathVariable Long installmentId,
            @RequestParam Long paymentId,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        PaymentInstallmentResponse updated = installmentService.markAsPaid(installmentId, paymentId, loggedInUserId);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{installmentId}/mark-overdue")
    @PreAuthorize("hasAuthority('PAYMENT_INSTALLMENT_UPDATE')")
    public ResponseEntity<PaymentInstallmentResponse> markAsOverdue(
            @PathVariable Long installmentId,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        PaymentInstallmentResponse updated = installmentService.markAsOverdue(installmentId, loggedInUserId);
        return ResponseEntity.ok(updated);
    }

    // ==================== DELETE ====================

    @DeleteMapping("/{installmentId}")
    @PreAuthorize("hasAuthority('PAYMENT_INSTALLMENT_DELETE')")
    public ResponseEntity<Map<String, String>> deleteInstallment(
            @PathVariable Long installmentId,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        installmentService.deleteInstallment(installmentId, loggedInUserId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Payment installment deleted successfully");
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/fees/{feesId}")
    @PreAuthorize("hasAuthority('PAYMENT_INSTALLMENT_DELETE')")
    public ResponseEntity<Map<String, String>> deleteAllInstallmentsForFees(
            @PathVariable Long feesId,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        installmentService.deleteAllInstallmentsForFees(feesId, loggedInUserId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "All installments deleted successfully for fees ID: " + feesId);
        return ResponseEntity.ok(response);
    }

    // ==================== HELPER ====================

    private Long extractUserId(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return user.getUserId();
    }
}
