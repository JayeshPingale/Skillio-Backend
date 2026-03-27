package com.skillio.controller;

import com.skillio.dto.BatchResponse;
import com.skillio.dto.CreateBatchRequest;
import com.skillio.dto.UpdateBatchRequest;
import com.skillio.services.BatchService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
public class BatchController {
    private final BatchService batchService;

    @PostMapping
    @PreAuthorize("hasAuthority('BATCH_CREATE')")
    public ResponseEntity<BatchResponse> createBatch(@Valid @RequestBody CreateBatchRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(batchService.createBatch(request));
    }

    @PutMapping("/{batchId}")
    @PreAuthorize("hasAuthority('BATCH_UPDATE')")
    public ResponseEntity<BatchResponse> updateBatch(
            @PathVariable Long batchId,
            @Valid @RequestBody UpdateBatchRequest request) {
        return ResponseEntity.ok(batchService.updateBatch(batchId, request));
    }

    @GetMapping("/{batchId}")
    @PreAuthorize("hasAuthority('BATCH_READ')")
    public ResponseEntity<BatchResponse> getBatchById(@PathVariable Long batchId) {
        return ResponseEntity.ok(batchService.getBatchById(batchId));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('BATCH_LIST')")
    public ResponseEntity<List<BatchResponse>> getAllBatches() {
        return ResponseEntity.ok(batchService.getAllBatches());
    }

    @GetMapping("/by-status/{status}")
    @PreAuthorize("hasAuthority('BATCH_LIST')")
    public ResponseEntity<List<BatchResponse>> getByStatus(@PathVariable String status) {
        return ResponseEntity.ok(batchService.getBatchesByStatus(status));
    }

    @GetMapping("/by-course/{courseId}")
    @PreAuthorize("hasAuthority('BATCH_LIST')")
    public ResponseEntity<List<BatchResponse>> getByCourse(@PathVariable Long courseId) {
        return ResponseEntity.ok(batchService.getBatchesByCourse(courseId));
    }

    @DeleteMapping("/{batchId}")
    @PreAuthorize("hasAuthority('BATCH_DELETE')")
    public ResponseEntity<Void> deleteBatch(@PathVariable Long batchId) {
        batchService.deleteBatch(batchId);
        return ResponseEntity.noContent().build();
    }
}
