package com.skillio.controller;

import com.skillio.dto.AuditLogResponse;
import com.skillio.services.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    // ==================== GET ALL AUDIT LOGS ====================

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_LOG_LIST')")
    public ResponseEntity<List<AuditLogResponse>> getAllAuditLogs() {
        List<AuditLogResponse> auditLogs = auditLogService.getAllAuditLogs();
        return ResponseEntity.ok(auditLogs);
    }

    // ==================== GET BY AUDIT ID ====================

    @GetMapping("/{auditId}")
    @PreAuthorize("hasAuthority('AUDIT_LOG_READ')")
    public ResponseEntity<AuditLogResponse> getAuditLogById(@PathVariable Long auditId) {
        AuditLogResponse auditLog = auditLogService.getAuditLogById(auditId);
        return ResponseEntity.ok(auditLog);
    }

    // ==================== GET BY ENTITY TYPE ====================

    @GetMapping("/entity-type/{entityType}")
    @PreAuthorize("hasAuthority('AUDIT_LOG_LIST')")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByEntityType(@PathVariable String entityType) {
        List<AuditLogResponse> auditLogs = auditLogService.getAuditLogsByEntityType(entityType);
        return ResponseEntity.ok(auditLogs);
    }

    // ==================== GET BY ENTITY TYPE AND ID ====================

    @GetMapping("/entity/{entityType}/{entityId}")
    @PreAuthorize("hasAuthority('AUDIT_LOG_LIST')")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByEntityTypeAndId(
            @PathVariable String entityType,
            @PathVariable Long entityId) {
        List<AuditLogResponse> auditLogs = auditLogService.getAuditLogsByEntityTypeAndId(entityType, entityId);
        return ResponseEntity.ok(auditLogs);
    }

    // ==================== GET BY USER ====================

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAuthority('AUDIT_LOG_LIST')")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByUser(@PathVariable Long userId) {
        List<AuditLogResponse> auditLogs = auditLogService.getAuditLogsByUser(userId);
        return ResponseEntity.ok(auditLogs);
    }

    // ==================== GET BY ACTION ====================

    @GetMapping("/action/{action}")
    @PreAuthorize("hasAuthority('AUDIT_LOG_LIST')")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByAction(@PathVariable String action) {
        List<AuditLogResponse> auditLogs = auditLogService.getAuditLogsByAction(action);
        return ResponseEntity.ok(auditLogs);
    }

    // ==================== GET BY DATE RANGE ====================

    @GetMapping("/date-range")
    @PreAuthorize("hasAuthority('AUDIT_LOG_LIST')")
    public ResponseEntity<List<AuditLogResponse>> getAuditLogsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<AuditLogResponse> auditLogs = auditLogService.getAuditLogsByDateRange(startDate, endDate);
        return ResponseEntity.ok(auditLogs);
    }
}
