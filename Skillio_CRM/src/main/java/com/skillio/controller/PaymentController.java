package com.skillio.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillio.dto.CreatePaymentRequest;
import com.skillio.dto.PaymentResponse;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.UserRepository;
import com.skillio.services.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/payments")
@Slf4j
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;
    private final UserRepository userRepository; // ✅ ADD THIS

    // ==================== CREATE ====================

    @PostMapping
    @PreAuthorize("hasAuthority('PAYMENT_CREATE')")
    public ResponseEntity<PaymentResponse> createPayment(
            @Valid @RequestBody CreatePaymentRequest request,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        PaymentResponse response = paymentService.createPayment(request, loggedInUserId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ==================== READ ====================

    @GetMapping
    @PreAuthorize("hasAuthority('PAYMENT_LIST')")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<PaymentResponse> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }
    @GetMapping("/my-payments")
    @PreAuthorize("hasAuthority('PAYMENT_LIST')")
    public ResponseEntity<List<PaymentResponse>> getMyPayments(Authentication authentication) {
        Long salesExecutiveId = extractUserId(authentication);
        log.info("🔥 Sales Executive {} requesting their payments", salesExecutiveId);
        List<PaymentResponse> payments = paymentService.getPaymentsByReceivedBy(salesExecutiveId);
        log.info("🔥 Returning {} payments", payments.size());
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAuthority('PAYMENT_READ')")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long paymentId) {
        PaymentResponse payment = paymentService.getPaymentById(paymentId);
        return ResponseEntity.ok(payment);
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAuthority('PAYMENT_LIST')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStudent(@PathVariable Long studentId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByStudent(studentId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/fees/{feesId}")
    @PreAuthorize("hasAuthority('PAYMENT_LIST')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStudentFees(@PathVariable Long feesId) {
        log.info("🔥 Fetching payment history for fees ID: {}", feesId);
        List<PaymentResponse> payments = paymentService.getPaymentsByStudentFees(feesId);
        log.info("🔥 Returning {} payment records", payments.size());
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/received-by/{userId}")
    @PreAuthorize("hasAuthority('PAYMENT_LIST')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByReceivedBy(@PathVariable Long userId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByReceivedBy(userId);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/date-range")
    @PreAuthorize("hasAuthority('PAYMENT_LIST')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<PaymentResponse> payments = paymentService.getPaymentsByDateRange(startDate, endDate);
        return ResponseEntity.ok(payments);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('PAYMENT_LIST')")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByStatus(@PathVariable String status) {
        List<PaymentResponse> payments = paymentService.getPaymentsByStatus(status);
        return ResponseEntity.ok(payments);
    }

    // ==================== ANALYTICS ====================

    @GetMapping("/total-paid/{studentId}")
    @PreAuthorize("hasAuthority('PAYMENT_READ')")
    public ResponseEntity<Map<String, BigDecimal>> getTotalPaidByStudent(@PathVariable Long studentId) {
        BigDecimal totalPaid = paymentService.getTotalPaidAmountByStudent(studentId);
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("totalPaidAmount", totalPaid);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count-by-receiver/{userId}")
    @PreAuthorize("hasAuthority('PAYMENT_READ')")
    public ResponseEntity<Map<String, Long>> getPaymentsCountByReceiver(@PathVariable Long userId) {
        Long count = paymentService.getTotalPaymentsCountByReceivedBy(userId);
        Map<String, Long> response = new HashMap<>();
        response.put("totalPayments", count);
        return ResponseEntity.ok(response);
    }

    // ==================== UPDATE ====================

    @PatchMapping("/{paymentId}/status")
    @PreAuthorize("hasAuthority('PAYMENT_UPDATE')")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable Long paymentId,
            @RequestParam String status,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        PaymentResponse updated = paymentService.updatePaymentStatus(paymentId, status, loggedInUserId);
        return ResponseEntity.ok(updated);
    }

    // ==================== DELETE ====================

    @DeleteMapping("/{paymentId}")
    @PreAuthorize("hasAuthority('PAYMENT_DELETE')")
    public ResponseEntity<Map<String, String>> deletePayment(
            @PathVariable Long paymentId,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        paymentService.deletePayment(paymentId, loggedInUserId);
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Payment deleted successfully");
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
