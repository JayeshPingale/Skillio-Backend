package com.skillio.controller;

import com.skillio.dto.LeadStatusHistoryResponse;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.UserRepository;
import com.skillio.services.LeadStatusHistoryService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lead-status-history")
@RequiredArgsConstructor
public class LeadStatusHistoryController {

    private final LeadStatusHistoryService leadStatusHistoryService;
    private final UserRepository userRepository;

    // Get Status History for a Specific Lead
    @GetMapping("/lead/{leadId}")
    @PreAuthorize("hasAuthority('LEAD_STATUS_HISTORY_LIST')")
    public ResponseEntity<List<LeadStatusHistoryResponse>> getHistoryByLead(@PathVariable Long leadId) {
        List<LeadStatusHistoryResponse> history = leadStatusHistoryService.getHistoryByLead(leadId);
        return ResponseEntity.ok(history);
    }

    // Get All Status Changes Made by a Specific User
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('LEAD_STATUS_HISTORY_LIST')")
    public ResponseEntity<List<LeadStatusHistoryResponse>> getHistoryByUser(@PathVariable Long userId) {
        List<LeadStatusHistoryResponse> history = leadStatusHistoryService.getHistoryByUser(userId);
        return ResponseEntity.ok(history);
    }

    // Get My Status Change History (Sales Exec sees their own changes)
    @GetMapping("/my-history")
    @PreAuthorize("hasAuthority('LEAD_STATUS_HISTORY_LIST')")
    public ResponseEntity<List<LeadStatusHistoryResponse>> getMyHistory(Authentication authentication) {
        Long userId = extractUserId(authentication);
        List<LeadStatusHistoryResponse> history = leadStatusHistoryService.getHistoryByUser(userId);
        return ResponseEntity.ok(history);
    }

    // Get All Lead Status History (Admin only)
    @GetMapping
    @PreAuthorize("hasAuthority('LEAD_STATUS_HISTORY_LIST')")
    public ResponseEntity<List<LeadStatusHistoryResponse>> getAllHistory() {
        List<LeadStatusHistoryResponse> history = leadStatusHistoryService.getAllHistory();
        return ResponseEntity.ok(history);
    }
    private Long extractUserId(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return user.getUserId();
    }
}
