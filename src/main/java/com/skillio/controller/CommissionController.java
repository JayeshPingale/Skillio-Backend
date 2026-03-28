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
import org.springframework.web.bind.annotation.RestController;

import com.skillio.dto.ApproveCommissionRequest;
import com.skillio.dto.CommissionResponse;
import com.skillio.dto.CreateCommissionRequest;
import com.skillio.dto.EnrolledStudentCommissionView;
import com.skillio.dto.UpdateCommissionRequest;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.UserRepository;
import com.skillio.services.CommissionService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/commissions")
@RequiredArgsConstructor
@Slf4j
public class CommissionController {

    private final CommissionService commissionService;
    private final UserRepository userRepository;

    // ==================== CREATE ====================
    @PostMapping
    @PreAuthorize("hasAuthority('COMMISSION_CREATE')")
    public ResponseEntity<CommissionResponse> createCommission(
            @Valid @RequestBody CreateCommissionRequest request,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        CommissionResponse response = commissionService.createCommission(request, loggedInUserId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ==================== SALES EXEC REQUEST ====================

    // ✅ ADD: Sales Executive apna commission request kar sake
    @PostMapping("/request")
    @PreAuthorize("hasAuthority('COMMISSION_CREATE')")
    public ResponseEntity<CommissionResponse> requestCommission(
            @Valid @RequestBody CreateCommissionRequest request,
            Authentication authentication) {
        Long salesExecutiveId = extractUserId(authentication);
        CommissionResponse response = commissionService.requestCommission(request, salesExecutiveId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    // ✅ ADD: Sales Exec ke enrolled students with payment progress
    @GetMapping("/my-enrolled-students")
    @PreAuthorize("hasAuthority('COMMISSION_LIST')")
    public ResponseEntity<List<EnrolledStudentCommissionView>> getMyEnrolledStudents(
            Authentication authentication) {
        Long salesExecutiveId = extractUserId(authentication);
        return ResponseEntity.ok(commissionService.getEnrolledStudentsForCommission(salesExecutiveId));
    }

    // ✅ ADD: Pending requests for Admin
    @GetMapping("/pending-approval")
    @PreAuthorize("hasAuthority('COMMISSION_LIST')")
    public ResponseEntity<List<CommissionResponse>> getPendingApprovalCommissions() {
        return ResponseEntity.ok(commissionService.getCommissionsByStatus("PENDING_APPROVAL"));
    }

    // ==================== READ ====================

    // ✅ Sales Executive's own commissions
    @GetMapping("/my-commissions")
    @PreAuthorize("hasAuthority('COMMISSION_LIST')")
    public ResponseEntity<List<CommissionResponse>> getMyCommissions(Authentication authentication) {
        Long salesExecutiveId = extractUserId(authentication);
        log.info("🔥 Sales Executive {} requesting their commissions", salesExecutiveId);
        List<CommissionResponse> commissions = commissionService.getCommissionsBySalesExecutive(salesExecutiveId);
        log.info("🔥 Returning {} commissions", commissions.size());
        return ResponseEntity.ok(commissions);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('COMMISSION_LIST')")
    public ResponseEntity<List<CommissionResponse>> getAllCommissions() {
        log.info("🔥 Admin requesting all commissions");
        List<CommissionResponse> commissions = commissionService.getAllCommissions();
        return ResponseEntity.ok(commissions);
    }

    @GetMapping("/{commissionId}")
    @PreAuthorize("hasAuthority('COMMISSION_READ')")
    public ResponseEntity<CommissionResponse> getCommissionById(@PathVariable Long commissionId) {
        CommissionResponse commission = commissionService.getCommissionById(commissionId);
        return ResponseEntity.ok(commission);
    }

    @GetMapping("/sales-executive/{salesExecutiveId}")
    @PreAuthorize("hasAuthority('COMMISSION_LIST')")
    public ResponseEntity<List<CommissionResponse>> getCommissionsBySalesExecutive(@PathVariable Long salesExecutiveId) {
        log.info("🔥 Admin/Accountant requesting commissions for sales executive {}", salesExecutiveId);
        List<CommissionResponse> commissions = commissionService.getCommissionsBySalesExecutive(salesExecutiveId);
        return ResponseEntity.ok(commissions);
    }

    @GetMapping("/sales-executive/{salesExecutiveId}/status/{status}")
    @PreAuthorize("hasAuthority('COMMISSION_LIST')")
    public ResponseEntity<List<CommissionResponse>> getCommissionsBySalesExecutiveAndStatus(
            @PathVariable Long salesExecutiveId,
            @PathVariable String status) {
        List<CommissionResponse> commissions = commissionService.getCommissionsBySalesExecutiveAndStatus(salesExecutiveId, status);
        return ResponseEntity.ok(commissions);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('COMMISSION_LIST')")
    public ResponseEntity<List<CommissionResponse>> getCommissionsByStatus(@PathVariable String status) {
        List<CommissionResponse> commissions = commissionService.getCommissionsByStatus(status);
        return ResponseEntity.ok(commissions);
    }

    @GetMapping("/enrollment/{enrollmentId}")
    @PreAuthorize("hasAuthority('COMMISSION_LIST')")
    public ResponseEntity<List<CommissionResponse>> getCommissionsByEnrollment(@PathVariable Long enrollmentId) {
        List<CommissionResponse> commissions = commissionService.getCommissionsByEnrollment(enrollmentId);
        return ResponseEntity.ok(commissions);
    }

    // ==================== ANALYTICS ====================

    @GetMapping("/sales-executive/{salesExecutiveId}/total-eligible")
    @PreAuthorize("hasAuthority('COMMISSION_READ')")
    public ResponseEntity<Map<String, BigDecimal>> getTotalEligibleAmount(@PathVariable Long salesExecutiveId, Authentication authentication) {
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SALES_EXECUTIVE"))) {
            Long loggedInUserId = extractUserId(authentication);
            if (!loggedInUserId.equals(salesExecutiveId)) {
                throw new ResourceNotFoundException("Access denied: You can only view your own commissions");
            }
        }
        BigDecimal total = commissionService.getTotalEligibleAmountBySalesExecutive(salesExecutiveId);
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("totalEligibleAmount", total);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sales-executive/{salesExecutiveId}/total-paid")
    @PreAuthorize("hasAuthority('COMMISSION_READ')")
    public ResponseEntity<Map<String, BigDecimal>> getTotalPaidCommission(@PathVariable Long salesExecutiveId, Authentication authentication) {
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SALES_EXECUTIVE"))) {
            Long loggedInUserId = extractUserId(authentication);
            if (!loggedInUserId.equals(salesExecutiveId)) {
                throw new ResourceNotFoundException("Access denied: You can only view your own commissions");
            }
        }
        BigDecimal total = commissionService.getTotalPaidCommissionBySalesExecutive(salesExecutiveId);
        Map<String, BigDecimal> response = new HashMap<>();
        response.put("totalPaidCommission", total);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/sales-executive/{salesExecutiveId}/count/{status}")
    @PreAuthorize("hasAuthority('COMMISSION_READ')")
    public ResponseEntity<Map<String, Long>> getCommissionCount(
            @PathVariable Long salesExecutiveId,
            @PathVariable String status,
            Authentication authentication) {
        if (authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_SALES_EXECUTIVE"))) {
            Long loggedInUserId = extractUserId(authentication);
            if (!loggedInUserId.equals(salesExecutiveId)) {
                throw new ResourceNotFoundException("Access denied: You can only view your own commissions");
            }
        }
        Long count = commissionService.getCommissionCountBySalesExecutiveAndStatus(salesExecutiveId, status);
        Map<String, Long> response = new HashMap<>();
        response.put("count", count);
        return ResponseEntity.ok(response);
    }

    // ==================== UPDATE ====================

    @PutMapping("/{commissionId}")
    @PreAuthorize("hasAuthority('COMMISSION_UPDATE')")
    public ResponseEntity<CommissionResponse> updateCommission(
            @PathVariable Long commissionId,
            @Valid @RequestBody UpdateCommissionRequest request,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        CommissionResponse updated = commissionService.updateCommission(commissionId, request, loggedInUserId);
        return ResponseEntity.ok(updated);
    }

//    // ✅ CHANGE: approveOrRejectCommission — approve + reject dono handle karta hai (PENDING_APPROVAL status)
//    @PostMapping("/approve")
//    @PreAuthorize("hasRole('ROLE_ADMIN')")
//    public ResponseEntity<CommissionResponse> approveOrRejectCommission(
//            @Valid @RequestBody ApproveCommissionRequest request,
//            Authentication authentication) {
//        Long adminId = extractUserId(authentication);
//        CommissionResponse response = commissionService.approveOrRejectCommission(request, adminId);
//        return ResponseEntity.ok(response);
//    }

 // ✅ YEH RAKHO
    @PostMapping("/approve")
    @PreAuthorize("hasAuthority('COMMISSION_UPDATE')")
    public ResponseEntity<CommissionResponse> approveOrRejectCommission(
            @Valid @RequestBody ApproveCommissionRequest request,
            Authentication authentication) {
        Long adminId = extractUserId(authentication);
        CommissionResponse response = commissionService.approveOrRejectCommission(request, adminId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{commissionId}/mark-eligible")
    @PreAuthorize("hasAuthority('COMMISSION_UPDATE')")
    public ResponseEntity<CommissionResponse> markAsEligible(
            @PathVariable Long commissionId,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        CommissionResponse updated = commissionService.markAsEligible(commissionId, loggedInUserId);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{commissionId}/mark-paid")
    @PreAuthorize("hasAuthority('COMMISSION_UPDATE')")
    public ResponseEntity<CommissionResponse> markAsPaid(
            @PathVariable Long commissionId,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        CommissionResponse updated = commissionService.markAsPaid(commissionId, loggedInUserId);
        return ResponseEntity.ok(updated);
    }

    // ==================== DELETE ====================

    @DeleteMapping("/{commissionId}")
    @PreAuthorize("hasAuthority('COMMISSION_DELETE')")
    public ResponseEntity<Map<String, String>> deleteCommission(
            @PathVariable Long commissionId,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        commissionService.deleteCommission(commissionId, loggedInUserId);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Commission deleted successfully");
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
