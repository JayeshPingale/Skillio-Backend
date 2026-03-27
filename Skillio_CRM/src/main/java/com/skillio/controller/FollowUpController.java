package com.skillio.controller;

import java.util.List;

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

import com.skillio.dto.FollowUpRequest;
import com.skillio.dto.FollowUpResponse;
import com.skillio.dto.UpdateFollowUpRequest;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.UserRepository;
import com.skillio.services.FollowUpService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/follow-ups")
@RequiredArgsConstructor
public class FollowUpController {

    private final FollowUpService followUpService;
    private final UserRepository userRepository;

    // Create Follow-up
    @PostMapping
    @PreAuthorize("hasAuthority('FOLLOW_UP_CREATE')")
    public ResponseEntity<FollowUpResponse> createFollowUp(
            @Valid @RequestBody FollowUpRequest request,
            Authentication authentication) {
        Long userId = extractUserId(authentication);
        FollowUpResponse response = followUpService.createFollowUp(request, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Update Follow-up
    @PutMapping("/{followUpId}")
    @PreAuthorize("hasAuthority('FOLLOW_UP_UPDATE')")
    public ResponseEntity<FollowUpResponse> updateFollowUp(
            @PathVariable Long followUpId,
            @Valid @RequestBody UpdateFollowUpRequest request) {
        FollowUpResponse response = followUpService.updateFollowUp(followUpId, request);
        return ResponseEntity.ok(response);
    }

    // Get Follow-up by ID
    @GetMapping("/{followUpId}")
    @PreAuthorize("hasAuthority('FOLLOW_UP_READ')")
    public ResponseEntity<FollowUpResponse> getFollowUpById(@PathVariable Long followUpId) {
        FollowUpResponse response = followUpService.getFollowUpById(followUpId);
        return ResponseEntity.ok(response);
    }

    // Get All Follow-ups (Admin only)
    @GetMapping
    @PreAuthorize("hasAuthority('FOLLOW_UP_LIST')")
    public ResponseEntity<List<FollowUpResponse>> getAllFollowUps() {
        List<FollowUpResponse> followUps = followUpService.getAllFollowUps();
        return ResponseEntity.ok(followUps);
    }

    // Get Follow-ups by Lead
    @GetMapping("/lead/{leadId}")
    @PreAuthorize("hasAuthority('FOLLOW_UP_LIST')")
    public ResponseEntity<List<FollowUpResponse>> getFollowUpsByLead(@PathVariable Long leadId) {
        List<FollowUpResponse> followUps = followUpService.getFollowUpsByLead(leadId);
        return ResponseEntity.ok(followUps);
    }

    // Get My Follow-ups (Sales Exec)
    @GetMapping("/my-follow-ups")
    @PreAuthorize("hasAuthority('FOLLOW_UP_LIST')")
    public ResponseEntity<List<FollowUpResponse>> getMyFollowUps(Authentication authentication) {
        Long userId = extractUserId(authentication);
        List<FollowUpResponse> followUps = followUpService.getFollowUpsByUser(userId);
        return ResponseEntity.ok(followUps);
    }

    // Get Follow-ups by Status
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('FOLLOW_UP_LIST')")
    public ResponseEntity<List<FollowUpResponse>> getFollowUpsByStatus(@PathVariable String status) {
        List<FollowUpResponse> followUps = followUpService.getFollowUpsByStatus(status);
        return ResponseEntity.ok(followUps);
    }

    // Get Follow-ups Due Today
    @GetMapping("/due-today")
    @PreAuthorize("hasAuthority('FOLLOW_UP_LIST')")
    public ResponseEntity<List<FollowUpResponse>> getFollowUpsDueToday() {
        List<FollowUpResponse> followUps = followUpService.getFollowUpsDueToday();
        return ResponseEntity.ok(followUps);
    }

    // Get Overdue Follow-ups
    @GetMapping("/overdue")
    @PreAuthorize("hasAuthority('FOLLOW_UP_LIST')")
    public ResponseEntity<List<FollowUpResponse>> getOverdueFollowUps() {
        List<FollowUpResponse> followUps = followUpService.getOverdueFollowUps();
        return ResponseEntity.ok(followUps);
    }

    // Mark Follow-up as Completed
    @PatchMapping("/{followUpId}/complete")
    @PreAuthorize("hasAuthority('FOLLOW_UP_UPDATE')")
    public ResponseEntity<Void> markFollowUpCompleted(@PathVariable Long followUpId) {
        followUpService.markFollowUpCompleted(followUpId);
        return ResponseEntity.noContent().build();
    }

    // Delete Follow-up
    @DeleteMapping("/{followUpId}")
    @PreAuthorize("hasAuthority('FOLLOW_UP_DELETE')")
    public ResponseEntity<Void> deleteFollowUp(@PathVariable Long followUpId) {
        followUpService.deleteFollowUp(followUpId);
        return ResponseEntity.noContent().build();
    }
    private Long extractUserId(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return user.getUserId();
    }
}
