package com.skillio.controller;

import com.skillio.dto.CreateLeadSourceRequest;
import com.skillio.dto.LeadSourceResponse;
import com.skillio.services.LeadSourceService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lead-sources")
@RequiredArgsConstructor
public class LeadSourceController {

    private final LeadSourceService leadSourceService;

    // Create Lead Source (Admin only)
    @PostMapping
    @PreAuthorize("hasAuthority('LEAD_SOURCE_CREATE')")
    public ResponseEntity<LeadSourceResponse> createLeadSource(@Valid @RequestBody CreateLeadSourceRequest request) {
        LeadSourceResponse response = leadSourceService.createLeadSource(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Get Lead Source by ID
    @GetMapping("/{sourceId}")
    @PreAuthorize("hasAuthority('LEAD_SOURCE_READ')")
    public ResponseEntity<LeadSourceResponse> getLeadSourceById(@PathVariable Long sourceId) {
        LeadSourceResponse response = leadSourceService.getLeadSourceById(sourceId);
        return ResponseEntity.ok(response);
    }

    // Get All Lead Sources
    @GetMapping
    @PreAuthorize("hasAuthority('LEAD_SOURCE_LIST')")
    public ResponseEntity<List<LeadSourceResponse>> getAllLeadSources() {
        List<LeadSourceResponse> sources = leadSourceService.getAllLeadSources();
        return ResponseEntity.ok(sources);
    }

    // Get Active Lead Sources (for dropdown in lead creation form)
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('LEAD_SOURCE_LIST')")
    public ResponseEntity<List<LeadSourceResponse>> getActiveLeadSources() {
        List<LeadSourceResponse> sources = leadSourceService.getActiveLeadSources();
        return ResponseEntity.ok(sources);
    }

    // Update Lead Source (Admin only)
    @PutMapping("/{sourceId}")
    @PreAuthorize("hasAuthority('LEAD_SOURCE_UPDATE')")
    public ResponseEntity<LeadSourceResponse> updateLeadSource(
            @PathVariable Long sourceId,
            @Valid @RequestBody CreateLeadSourceRequest request) {
        LeadSourceResponse response = leadSourceService.updateLeadSource(sourceId, request);
        return ResponseEntity.ok(response);
    }

    // Toggle Lead Source Status (Admin only)
    @PatchMapping("/{sourceId}/toggle-status")
    @PreAuthorize("hasAuthority('LEAD_SOURCE_UPDATE')")
    public ResponseEntity<Void> toggleLeadSourceStatus(@PathVariable Long sourceId) {
        leadSourceService.toggleLeadSourceStatus(sourceId);
        return ResponseEntity.noContent().build();
    }

    // Delete Lead Source (Admin only)
    @DeleteMapping("/{sourceId}")
    @PreAuthorize("hasAuthority('LEAD_SOURCE_DELETE')")
    public ResponseEntity<Void> deleteLeadSource(@PathVariable Long sourceId) {
        leadSourceService.deleteLeadSource(sourceId);
        return ResponseEntity.noContent().build();
    }
}
