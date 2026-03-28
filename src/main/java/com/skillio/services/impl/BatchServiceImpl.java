package com.skillio.services.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skillio.dto.AuditBatchDTO;
import com.skillio.dto.BatchResponse;
import com.skillio.dto.CreateBatchRequest;
import com.skillio.dto.UpdateBatchRequest;
import com.skillio.entities.Batch;
import com.skillio.entities.Course;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.BatchRepository;
import com.skillio.repositories.CourseRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.services.AuditLogService;
import com.skillio.services.BatchService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class BatchServiceImpl implements BatchService {

    private final BatchRepository batchRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    @Override
    public BatchResponse createBatch(CreateBatchRequest request) {
        log.info("Creating new batch: {}", request.getBatchCode());

        if (batchRepository.existsByBatchCode(request.getBatchCode())) {
            throw new IllegalStateException("Batch code already exists: " + request.getBatchCode());
        }

        Course course = courseRepository.findById(request.getCourseId())
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + request.getCourseId()));

        if (request.getEndDate().isBefore(request.getStartDate()) || request.getEndDate().isEqual(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        Batch batch = new Batch();
        batch.setBatchCode(request.getBatchCode());
        batch.setBatchName(request.getBatchName());
        batch.setCourse(course);
        batch.setStartDate(request.getStartDate());
        batch.setEndDate(request.getEndDate());
        batch.setTiming(request.getTiming());
        batch.setModeOfClass(request.getModeOfClass());
        batch.setInstructor(request.getInstructor());
        batch.setDescription(request.getDescription());
        batch.setStatus("UPCOMING");
        batch.setEnrolledCount(0);

        Batch saved = batchRepository.save(batch);

        // ✅ Convert to audit DTO before logging
        AuditBatchDTO auditDTO = convertToAuditDTO(saved);
        User performedBy = getLoggedInUser();
        auditLogService.createAuditLog("Batch", saved.getBatchId(), "CREATE", 
                                       null, auditDTO, performedBy);

        log.info("Batch created successfully with ID: {} and code: {}", saved.getBatchId(), saved.getBatchCode());

        return mapToResponse(saved);
    }

    @Override
    public BatchResponse updateBatch(Long batchId, UpdateBatchRequest request) {
        log.info("Updating batch with ID: {}", batchId);

        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + batchId));

        if (request.getEndDate().isBefore(request.getStartDate()) || request.getEndDate().isEqual(request.getStartDate())) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        // ✅ Clone old batch and convert to DTO
        AuditBatchDTO oldBatchDTO = convertToAuditDTO(batch);

        batch.setBatchName(request.getBatchName());
        batch.setStartDate(request.getStartDate());
        batch.setEndDate(request.getEndDate());
        batch.setTiming(request.getTiming());
        batch.setModeOfClass(request.getModeOfClass());
        batch.setInstructor(request.getInstructor());
        batch.setDescription(request.getDescription());

        Batch updated = batchRepository.save(batch);

        // ✅ Convert updated batch to DTO
        AuditBatchDTO newBatchDTO = convertToAuditDTO(updated);
        User performedBy = getLoggedInUser();
        auditLogService.createAuditLog("Batch", batchId, "UPDATE", 
                                       oldBatchDTO, newBatchDTO, performedBy);

        log.info("Batch updated successfully with ID: {}", batchId);

        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public BatchResponse getBatchById(Long batchId) {
        log.info("Fetching batch with ID: {}", batchId);
        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + batchId));
        return mapToResponse(batch);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> getAllBatches() {
        log.info("Fetching all batches");
        return batchRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> getBatchesByStatus(String status) {
        log.info("Fetching batches with status: {}", status);
        return batchRepository.findByStatusIgnoreCase(status).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<BatchResponse> getBatchesByCourse(Long courseId) {
        log.info("Fetching batches for course ID: {}", courseId);
        return batchRepository.findByCourseCourseId(courseId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteBatch(Long batchId) {
        log.info("Deleting batch with ID: {}", batchId);

        Batch batch = batchRepository.findById(batchId)
                .orElseThrow(() -> new ResourceNotFoundException("Batch not found: " + batchId));

        // ✅ Convert to DTO before deletion
        AuditBatchDTO auditDTO = convertToAuditDTO(batch);
        User performedBy = getLoggedInUser();
        auditLogService.createAuditLog("Batch", batchId, "DELETE", 
                                       auditDTO, null, performedBy);

        batchRepository.delete(batch);
        log.info("Batch deleted successfully with ID: {}", batchId);
    }

    // ==================== HELPER METHODS ====================

    /**
     * ✅ Convert Batch entity to audit DTO (no circular references)
     */
    private AuditBatchDTO convertToAuditDTO(Batch batch) {
        AuditBatchDTO dto = new AuditBatchDTO();
        dto.setBatchId(batch.getBatchId());
        dto.setBatchCode(batch.getBatchCode());
        dto.setBatchName(batch.getBatchName());
        dto.setCourseId(batch.getCourse().getCourseId());
        dto.setCourseName(batch.getCourse().getCourseName());
        dto.setStartDate(batch.getStartDate());
        dto.setEndDate(batch.getEndDate());
        dto.setTiming(batch.getTiming());
        dto.setModeOfClass(batch.getModeOfClass());
        dto.setInstructor(batch.getInstructor());
        dto.setDescription(batch.getDescription());
        dto.setStatus(batch.getStatus());
        dto.setEnrolledCount(batch.getEnrolledCount());
        return dto;
    }

    private BatchResponse mapToResponse(Batch batch) {
        BatchResponse res = new BatchResponse();
        res.setBatchId(batch.getBatchId());
        res.setBatchCode(batch.getBatchCode());
        res.setBatchName(batch.getBatchName());
        res.setCourseId(batch.getCourse().getCourseId());
        res.setCourseName(batch.getCourse().getCourseName());
        res.setStartDate(batch.getStartDate());
        res.setEndDate(batch.getEndDate());
        res.setTiming(batch.getTiming());
        res.setModeOfClass(batch.getModeOfClass());
        res.setInstructor(batch.getInstructor());
        res.setDescription(batch.getDescription());
        res.setStatus(batch.getStatus());
        res.setCreatedAt(batch.getCreatedAt());
        res.setUpdatedAt(batch.getUpdatedAt());
        res.setEnrolledCount(batch.getEnrolledCount());
        return res;
    }

    /**
     * Get currently logged-in user from Spring Security Context
     */
    private User getLoggedInUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName();
                return userRepository.findByEmail(email).orElse(null);
            }
        } catch (Exception e) {
            log.warn("Could not fetch logged-in user from SecurityContext", e);
        }
        return null;
    }
    
    // ❌ REMOVED cloneBatch() method - not needed anymore
}
