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
import com.skillio.services.LeadService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@Slf4j
public class LeadController {

    private final LeadService leadService;
    private final UserRepository userRepository;

    // ✅ Create Lead - Matches createLead(request, userId)
    @PostMapping
    @PreAuthorize("hasAuthority('LEAD_CREATE')")
    public ResponseEntity<LeadResponse> createLead(
            @Valid @RequestBody CreateLeadRequest request,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        log.info("🔥 Creating lead for: {} by User ID: {}", request.getFullName(), userId);
        LeadResponse response = leadService.createLead(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ✅ Update Lead - Matches updateLead(leadId, request) - NO userId param
    @PutMapping("/{leadId}")
    @PreAuthorize("hasAuthority('LEAD_UPDATE')")
    public ResponseEntity<LeadResponse> updateLead(
            @PathVariable Long leadId,
            @Valid @RequestBody UpdateLeadRequest request) {
        
        log.info("🔥 Updating lead ID: {}", leadId);
        LeadResponse response = leadService.updateLead(leadId, request);
        return ResponseEntity.ok(response);
    }

    // Get Lead by ID
    @GetMapping("/{leadId}")
    @PreAuthorize("hasAuthority('LEAD_READ')")
    public ResponseEntity<LeadResponse> getLeadById(@PathVariable Long leadId) {
        log.info("🔥 Getting lead by ID: {}", leadId);
        LeadResponse response = leadService.getLeadById(leadId);
        return ResponseEntity.ok(response);
    }

    // Get All Leads
    @GetMapping
    @PreAuthorize("hasAuthority('LEAD_LIST')")
    public ResponseEntity<List<LeadResponse>> getAllLeads() {
        log.info("🔥 Getting all leads");
        List<LeadResponse> leads = leadService.getAllLeads();
        return ResponseEntity.ok(leads);
    }

    // Get Leads by Status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('LEAD_LIST')")
    public ResponseEntity<List<LeadResponse>> getLeadsByStatus(@PathVariable String status) {
        log.info("🔥 Getting leads by status: {}", status);
        List<LeadResponse> leads = leadService.getLeadsByStatus(status);
        return ResponseEntity.ok(leads);
    }

    // ✅ Get My Leads - Matches getLeadsByAssignedUser(userId)
    @GetMapping("/my-leads")
    @PreAuthorize("hasAuthority('LEAD_LIST')")
    public ResponseEntity<List<LeadResponse>> getMyLeads(Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        log.info("🔥 Getting leads for Sales Executive ID: {}", userId);
        List<LeadResponse> leads = leadService.getLeadsByAssignedUser(userId);
        return ResponseEntity.ok(leads);
    }

    // ✅ Delete Lead - Matches deleteLead(leadId) - NO userId param
    @DeleteMapping("/{leadId}")
    @PreAuthorize("hasAuthority('LEAD_DELETE')")
    public ResponseEntity<Void> deleteLead(@PathVariable Long leadId) {
        log.info("🔥 Deleting lead ID: {}", leadId);
        leadService.deleteLead(leadId);
        return ResponseEntity.noContent().build();
    }

    // ✅ Change Lead Status - Matches changeLeadStatus(request, userId)
    @PutMapping("/change-status")
    @PreAuthorize("hasAuthority('LEAD_UPDATE')")
    public ResponseEntity<LeadResponse> changeLeadStatus(
            @Valid @RequestBody LeadStatusChangeRequest request,
            Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        log.info("🔥 Changing status for Lead ID: {} by User ID: {}", request.getLeadId(), userId);
        LeadResponse response = leadService.changeLeadStatus(request, userId);
        return ResponseEntity.ok(response);
    }

    // Get Lead Status History
    @GetMapping("/{leadId}/history")
    @PreAuthorize("hasAuthority('LEAD_LIST')")
    public ResponseEntity<List<LeadStatusHistoryResponse>> getLeadStatusHistory(@PathVariable Long leadId) {
        log.info("🔥 Getting status history for Lead ID: {}", leadId);
        List<LeadStatusHistoryResponse> history = leadService.getLeadStatusHistory(leadId);
        return ResponseEntity.ok(history);
    }

    // ✅ Assign Lead - Matches assignLead(request) - NO userId param
    @PutMapping("/assign")
    @PreAuthorize("hasAuthority('LEAD_UPDATE')")
    public ResponseEntity<LeadResponse> assignLead(@Valid @RequestBody AssignLeadRequest request) {
        log.info("🔥 Assigning Lead ID {} to Sales Executive ID {}", 
            request.getLeadId(), request.getSalesExecutiveId());
        LeadResponse response = leadService.assignLead(request);
        return ResponseEntity.ok(response);
    }

    // ✅ Unassign Lead - Matches unassignLead(leadId) - NO userId param
    @PutMapping("/{leadId}/unassign")
    @PreAuthorize("hasAuthority('LEAD_UPDATE')")
    public ResponseEntity<LeadResponse> unassignLead(@PathVariable Long leadId) {
        log.info("🔥 Unassigning Lead ID: {}", leadId);
        LeadResponse response = leadService.unassignLead(leadId);
        return ResponseEntity.ok(response);
    }

    // Get Non-Converted Leads
    @GetMapping("/non-converted")
    @PreAuthorize("hasAuthority('LEAD_LIST')")
    public ResponseEntity<List<LeadResponse>> getNonConvertedLeads() {
        log.info("🔥 Getting non-converted leads for enrollment");
        List<LeadResponse> leads = leadService.getNonConvertedLeads();
        return ResponseEntity.ok(leads);
    }

    private Long extractUserId(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return user.getUserId();
    }
}
