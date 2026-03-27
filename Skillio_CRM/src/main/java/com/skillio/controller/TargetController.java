package com.skillio.controller;

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

import com.skillio.dto.CreateTargetRequest;
import com.skillio.dto.TargetResponse;
import com.skillio.dto.UpdateTargetAchievementRequest;
import com.skillio.dto.UpdateTargetRequest;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.UserRepository;
import com.skillio.services.TargetService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/targets")
@Slf4j
@RequiredArgsConstructor
public class TargetController {

    private final TargetService targetService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("hasAuthority('TARGET_CREATE')")
    public ResponseEntity<TargetResponse> createTarget(
            @Valid @RequestBody CreateTargetRequest request,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        TargetResponse response = targetService.createTarget(request, loggedInUserId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('TARGET_LIST')")
    public ResponseEntity<List<TargetResponse>> getAllTargets() {
        List<TargetResponse> targets = targetService.getAllTargets();
        return ResponseEntity.ok(targets);
    }

    @GetMapping("/{targetId}")
    @PreAuthorize("hasAuthority('TARGET_READ')")
    public ResponseEntity<TargetResponse> getTargetById(@PathVariable Long targetId) {
        TargetResponse target = targetService.getTargetById(targetId);
        return ResponseEntity.ok(target);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('TARGET_LIST')")
    public ResponseEntity<List<TargetResponse>> getTargetsByUser(@PathVariable Long userId) {
        List<TargetResponse> targets = targetService.getTargetsByUser(userId);
        return ResponseEntity.ok(targets);
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("hasAuthority('TARGET_LIST')")
    public ResponseEntity<List<TargetResponse>> getTargetsByStatus(@PathVariable String status) {
        List<TargetResponse> targets = targetService.getTargetsByStatus(status);
        return ResponseEntity.ok(targets);
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('TARGET_LIST')")
    public ResponseEntity<List<TargetResponse>> getActiveTargets() {
        List<TargetResponse> targets = targetService.getActiveTargets();
        return ResponseEntity.ok(targets);
    }

    @GetMapping("/my-targets")
    @PreAuthorize("hasAuthority('TARGET_LIST')")
    public ResponseEntity<List<TargetResponse>> getMyTargets(Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        List<TargetResponse> targets = targetService.getTargetsByUser(loggedInUserId);
        return ResponseEntity.ok(targets);
    }

    @PutMapping("/{targetId}")
    @PreAuthorize("hasAuthority('TARGET_UPDATE')")
    public ResponseEntity<TargetResponse> updateTarget(
            @PathVariable Long targetId,
            @Valid @RequestBody UpdateTargetRequest request,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        TargetResponse updated = targetService.updateTarget(targetId, request, loggedInUserId);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{targetId}/achievement")
    @PreAuthorize("hasAuthority('TARGET_UPDATE')")
    public ResponseEntity<TargetResponse> updateTargetAchievement(
            @PathVariable Long targetId,
            @Valid @RequestBody UpdateTargetAchievementRequest request,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        TargetResponse updated = targetService.updateTargetAchievement(targetId, request, loggedInUserId);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{targetId}/mark-completed")
    @PreAuthorize("hasAuthority('TARGET_UPDATE')")
    public ResponseEntity<TargetResponse> markAsCompleted(@PathVariable Long targetId, Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        TargetResponse updated = targetService.markAsCompleted(targetId, loggedInUserId);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{targetId}")
    @PreAuthorize("hasAuthority('TARGET_DELETE')")
    public ResponseEntity<Map<String, String>> deleteTarget(
            @PathVariable Long targetId,
            Authentication authentication) {
        Long loggedInUserId = extractUserId(authentication);
        targetService.deleteTarget(targetId, loggedInUserId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Target deleted successfully");
        return ResponseEntity.ok(response);
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication == null) {
            throw new IllegalStateException("No authentication found");
        }

        String email = authentication.getName();
        log.info("Extracting userId for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        log.info("Found userId: {} (role: {}) for email: {}", user.getUserId(), user.getRole().getRoleName(), email);
        return user.getUserId();
    }
}
