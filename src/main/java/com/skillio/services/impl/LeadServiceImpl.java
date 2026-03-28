package com.skillio.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillio.dto.AssignLeadRequest;
import com.skillio.dto.CreateLeadRequest;
import com.skillio.dto.LeadResponse;
import com.skillio.dto.LeadStatusChangeRequest;
import com.skillio.dto.LeadStatusHistoryResponse;
import com.skillio.dto.UpdateLeadRequest;
import com.skillio.entities.AuditLog;
import com.skillio.entities.Lead;
import com.skillio.entities.LeadSource;
import com.skillio.entities.LeadStatusHistory;
import com.skillio.entities.Student;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.AuditLogRepository;
import com.skillio.repositories.LeadRepository;
import com.skillio.repositories.LeadSourceRepository;
import com.skillio.repositories.LeadStatusHistoryRepository;
import com.skillio.repositories.StudentRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.services.LeadService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;
    private final LeadSourceRepository leadSourceRepository;
    private final UserRepository userRepository;
    private final LeadStatusHistoryRepository leadStatusHistoryRepository;
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest httpServletRequest;
    private final ObjectMapper objectMapper;
    private final StudentRepository studentRepository;

//    @Override
//    public LeadResponse createLead(CreateLeadRequest request) {
//        log.info("Creating new lead: {}", request.getFullName());
//
//        // 1. Validate lead source
//        LeadSource source = leadSourceRepository.findById(request.getSourceId())
//                .orElseThrow(() -> new ResourceNotFoundException("Lead source not found with ID: " + request.getSourceId()));
//
//        // 2. Create new lead
//        Lead lead = new Lead();
//        lead.setFullName(request.getFullName());
//        lead.setContactNumber(request.getContactNumber());
//        lead.setEmail(request.getEmail());
//        lead.setCourseInterested(request.getCourseInterested());
//        lead.setCollegeName(request.getCollegeName());
//        lead.setQualification(request.getQualification());
//        lead.setExperience(request.getExperience());
//        lead.setInterestLevel(request.getInterestLevel());
//        lead.setLeadSource(source);
//        lead.setComments(request.getComments());
//        lead.setStatus("NEW"); // Default status
//        lead.setCreatedDate(LocalDateTime.now());
//
//        // 3. Save to database
//        Lead savedLead = leadRepository.save(lead);
//
//        // 4. Create Audit Log
//        User performedBy = getLoggedInUser();
//        createAuditLog("Lead", savedLead.getLeadId(), "CREATE", null, savedLead, performedBy);
//
//        log.info("Lead created successfully with ID: {}", savedLead.getLeadId());
//
//        // 5. Map to response DTO
//        return mapToResponse(savedLead);
//    }

//    @Override
//    public LeadResponse createLead(CreateLeadRequest request) {
//        log.info("Creating new lead: {}", request.getFullName());
//
//        // 1. Validate lead source
//        LeadSource source = leadSourceRepository.findById(request.getSourceId())
//                .orElseThrow(() -> new ResourceNotFoundException("Lead source not found with ID: " + request.getSourceId()));
//
//        // 2. Get currently logged-in user
//        User performedBy = getLoggedInUser();
//        
//        // 3. Create new lead
//        Lead lead = new Lead();
//        lead.setFullName(request.getFullName());
//        lead.setContactNumber(request.getContactNumber());
//        lead.setEmail(request.getEmail());
//        lead.setCourseInterested(request.getCourseInterested());
//        lead.setCollegeName(request.getCollegeName());
//        lead.setQualification(request.getQualification());
//        lead.setExperience(request.getExperience());
//        lead.setInterestLevel(request.getInterestLevel());
//        lead.setLeadSource(source);
//        lead.setComments(request.getComments());
//        lead.setStatus("NEW"); // Default status
//        lead.setCreatedDate(LocalDateTime.now());
//
//        // ✅ AUTO-ASSIGN LOGIC: If sales executive creates, auto-assign to self
//        if (performedBy != null && "ROLE_SALES_EXECUTIVE".equals(performedBy.getRole().getRoleName())) {
//            lead.setAssignedTo(performedBy);
//            log.info("✅ Auto-assigned lead to sales executive: {}", performedBy.getFullName());
//        } else {
//            lead.setAssignedTo(null);
//            log.info("ℹ️ Lead created by admin - leaving unassigned for manual assignment");
//        }
//
//        // 4. Save to database
//        Lead savedLead = leadRepository.save(lead);
//
//        // 5. Create Audit Log
//        createAuditLog("Lead", savedLead.getLeadId(), "CREATE", null, savedLead, performedBy);
//
//        log.info("Lead created successfully with ID: {}", savedLead.getLeadId());
//
//        // 6. Map to response DTO
//        return mapToResponse(savedLead);
//    }

    @Override
    @Transactional
    public LeadResponse createLead(CreateLeadRequest request, Long loggedInUserId) {
        log.info("🔥 Creating new lead: {}", request.getFullName());
        
        // Validate Lead Source
        LeadSource source = leadSourceRepository.findById(request.getSourceId())
            .orElseThrow(() -> new ResourceNotFoundException("Lead Source not found with ID: " + request.getSourceId()));
        
        // Get logged-in user
        User createdBy = userRepository.findById(loggedInUserId)
            .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + loggedInUserId));
        
        // ✅ AUTO-ASSIGN SALES EXECUTIVE BASED ON ROLE
        User salesExecutive = null;
        
        if (createdBy.getRole().getRoleName().equals("ROLE_SALES_EXECUTIVE")) {
            // ✅ If Sales Executive creates lead, assign to themselves
            salesExecutive = createdBy;
            log.info("✅ Sales Executive created lead - Auto-assigned to: {}", createdBy.getFullName());
        } else if (createdBy.getRole().getRoleName().equals("ROLE_ADMIN")) {
            // ✅ If Admin creates lead, leave unassigned (null)
            salesExecutive = null;
            log.info("✅ Admin created lead - Leaving unassigned for manual assignment");
        }
        
        // Create Lead
        Lead lead = new Lead();
        lead.setFullName(request.getFullName());
        lead.setContactNumber(request.getContactNumber());
        lead.setEmail(request.getEmail());
        lead.setCourseInterested(request.getCourseInterested());
        lead.setCollegeName(request.getCollegeName());
        lead.setQualification(request.getQualification());
        lead.setExperience(request.getExperience());
        lead.setInterestLevel(request.getInterestLevel());
        lead.setLeadSource(source);
        lead.setStatus("NEW");
        lead.setComments(request.getComments());
        
        // ✅ Set Sales Executive (can be null if Admin created)
        lead.setSalesExecutive(salesExecutive);
        lead.setAssignedTo(salesExecutive);
        
        Lead savedLead = leadRepository.save(lead);
        
        // Create Audit Log
        createAuditLog("Lead", savedLead.getLeadId(), "CREATE", null, savedLead, createdBy);
        
        log.info("✅ Lead created successfully with ID: {}", savedLead.getLeadId());
        return mapToResponse(savedLead);
    }


//    @Override
//    public LeadResponse updateLead(Long leadId, UpdateLeadRequest request) {
//        log.info("Updating lead with ID: {}", leadId);
//
//        // 1. Fetch existing lead
//        Lead lead = leadRepository.findById(leadId)
//                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + leadId));
//
//        // Clone old lead for audit log
//        Lead oldLead = cloneLead(lead);
//
//        // 2. Update fields
//        lead.setFullName(request.getFullName());
//        lead.setContactNumber(request.getContactNumber());
//        lead.setEmail(request.getEmail());
//        lead.setCourseInterested(request.getCourseInterested());
//        lead.setCollegeName(request.getCollegeName());
//        lead.setQualification(request.getQualification());
//        lead.setExperience(request.getExperience());
//        lead.setInterestLevel(request.getInterestLevel());
//        lead.setComments(request.getComments());
//
//     // >>> SYNC EMAIL TO STUDENT IF CONVERTED <<<
//        if (lead.getConvertedStudent() != null) {
//            Student s = lead.getConvertedStudent();
//            s.setEmail(lead.getEmail());
//            studentRepository.save(s);
//        }
//        // 3. Save and return
//        Lead updatedLead = leadRepository.save(lead);
//
//        // 4. Create Audit Log
//        User performedBy = getLoggedInUser();
//        createAuditLog("Lead", leadId, "UPDATE", oldLead, updatedLead, performedBy);
//
//        log.info("Lead updated successfully with ID: {}", leadId);
//
//        return mapToResponse(updatedLead);
//    }

    @Override
    public LeadResponse updateLead(Long leadId, UpdateLeadRequest request) {
        log.info("Updating lead with ID: {}", leadId);

        // 1. Fetch existing lead
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + leadId));

        // Clone old lead for audit log
        Lead oldLead = cloneLead(lead);

        // 2. Update fields
        lead.setFullName(request.getFullName());
        lead.setContactNumber(request.getContactNumber());
        lead.setEmail(request.getEmail());
        lead.setCourseInterested(request.getCourseInterested());
        lead.setCollegeName(request.getCollegeName());
        lead.setQualification(request.getQualification());
        lead.setExperience(request.getExperience());
        lead.setInterestLevel(request.getInterestLevel());
        lead.setComments(request.getComments());

        // >>> Sync email to converted student, if present <<<
        Student converted = lead.getConvertedStudent();
        if (converted != null) {
            converted.setEmail(lead.getEmail());
            studentRepository.save(converted);
        }

        // 3. Save and return
        Lead updatedLead = leadRepository.save(lead);

        // 4. Create Audit Log
        User performedBy = getLoggedInUser();
        createAuditLog("Lead", leadId, "UPDATE", oldLead, updatedLead, performedBy);

        log.info("Lead updated successfully with ID: {}", leadId);

        return mapToResponse(updatedLead);
    }

    @Override
    @Transactional(readOnly = true)
    public LeadResponse getLeadById(Long leadId) {
        log.info("Fetching lead with ID: {}", leadId);
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + leadId));
        return mapToResponse(lead);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadResponse> getAllLeads() {
        log.info("Fetching all leads");
        return leadRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadResponse> getLeadsByStatus(String status) {
        log.info("Fetching leads by status: {}", status);
        return leadRepository.findByStatus(status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadResponse> getLeadsByAssignedUser(Long userId) {
        log.info("Fetching leads assigned to user ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));
        return leadRepository.findByAssignedTo(user)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteLead(Long leadId) {
        log.info("Deleting lead with ID: {}", leadId);

        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + leadId));

        // Create Audit Log before deletion
        User performedBy = getLoggedInUser();
        createAuditLog("Lead", leadId, "DELETE", lead, null, performedBy);

        leadRepository.delete(lead);
        log.info("Lead deleted successfully with ID: {}", leadId);
    }

    @Override
    public LeadResponse changeLeadStatus(LeadStatusChangeRequest request, Long userId) {
        log.info("Changing status for lead ID: {} to {}", request.getLeadId(), request.getNewStatus());

        // 1. Fetch lead
        Lead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + request.getLeadId()));

        // 2. Fetch user who is changing status
        User changedBy = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with ID: " + userId));

        // Clone old lead for audit log
        Lead oldLead = cloneLead(lead);

        // 3. Store old status for history
        String oldStatus = lead.getStatus();

        // 4. Update lead status
        lead.setStatus(request.getNewStatus());
        lead.setLastContactDate(LocalDate.now());

        if (request.getNewStatus().equals("CONVERTED")) {
            lead.setConversionDate(LocalDate.now());
        }

        Lead updatedLead = leadRepository.save(lead);

        // 5. Create status history entry
        LeadStatusHistory history = new LeadStatusHistory();
        history.setLead(updatedLead);
        history.setOldStatus(oldStatus);
        history.setNewStatus(request.getNewStatus());
        history.setChangedBy(changedBy);
        history.setRemarks(request.getRemarks());
        history.setChangedAt(LocalDateTime.now());
        leadStatusHistoryRepository.save(history);

        // 6. Create Audit Log for status change
        User performedBy = getLoggedInUser();
        createAuditLog("Lead", request.getLeadId(), "STATUS_CHANGE", oldLead, updatedLead, performedBy);

        log.info("Lead status changed from {} to {} for lead ID: {}", oldStatus, request.getNewStatus(), request.getLeadId());

        return mapToResponse(updatedLead);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LeadStatusHistoryResponse> getLeadStatusHistory(Long leadId) {
        log.info("Fetching status history for lead ID: {}", leadId);
        return leadStatusHistoryRepository.findByLeadLeadIdOrderByChangedAtDesc(leadId)
                .stream()
                .map(this::mapToHistoryResponse)
                .collect(Collectors.toList());
    }

//    @Override
//    public LeadResponse assignLead(AssignLeadRequest request) {
//        log.info("Assigning lead ID: {} to user ID: {}", request.getLeadId(), request.getSalesExecutiveId());
//
//        // 1. Fetch lead
//        Lead lead = leadRepository.findById(request.getLeadId())
//                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + request.getLeadId()));
//
//        // Check if already assigned
//        if (lead.getAssignedTo() != null) {
//            throw new IllegalStateException("Lead is already assigned to " + 
//                lead.getAssignedTo().getFullName());
//        }
//
//        // Clone old lead for audit log
//        Lead oldLead = cloneLead(lead);
//
//        // 2. Fetch sales executive
//        User salesExec = userRepository.findById(request.getSalesExecutiveId())
//                .orElseThrow(() -> new ResourceNotFoundException("Sales Executive not found with ID: " + request.getSalesExecutiveId()));
//
//        // 3. Assign lead
//        lead.setAssignedTo(salesExec);
//        Lead updatedLead = leadRepository.save(lead);
//
//        // 4. Create Audit Log
//        User performedBy = getLoggedInUser();
//        createAuditLog("Lead", request.getLeadId(), "ASSIGN", oldLead, updatedLead, performedBy);
//
//        log.info("Lead assigned successfully to: {}", salesExec.getFullName());
//
//        return mapToResponse(updatedLead);
//    }

    @Override
    public LeadResponse assignLead(AssignLeadRequest request) {
        log.info("Assigning lead ID: {} to user ID: {}", request.getLeadId(), request.getSalesExecutiveId());

        // 1. Fetch lead
        Lead lead = leadRepository.findById(request.getLeadId())
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + request.getLeadId()));

        // ✅ Check if already assigned (prevent reassignment)
        if (lead.getAssignedTo() != null) {
            throw new IllegalStateException(
                "Lead is already assigned to " + lead.getAssignedTo().getFullName() + 
                ". Please unassign first before reassigning to another user."
            );
        }

        // Clone old lead for audit log
        Lead oldLead = cloneLead(lead);

        // 2. Fetch sales executive
        User salesExec = userRepository.findById(request.getSalesExecutiveId())
                .orElseThrow(() -> new ResourceNotFoundException("Sales Executive not found with ID: " + request.getSalesExecutiveId()));

        // 3. Validate that target user is a sales executive or admin
        String roleName = salesExec.getRole().getRoleName();
        if (!("ROLE_SALES_EXECUTIVE".equals(roleName) || "ROLE_ADMIN".equals(roleName))) {
            throw new IllegalArgumentException("User must be a Sales Executive or Admin to be assigned leads");
        }

        // 4. Assign lead
        lead.setAssignedTo(salesExec);
        Lead updatedLead = leadRepository.save(lead);

        // 5. Create Audit Log
        User performedBy = getLoggedInUser();
        createAuditLog("Lead", request.getLeadId(), "ASSIGN", oldLead, updatedLead, performedBy);

        log.info("✅ Lead assigned successfully to: {}", salesExec.getFullName());

        return mapToResponse(updatedLead);
    }

    
    //unassign lead 
    @Override
    public LeadResponse unassignLead(Long leadId) {
        log.info("Unassigning lead ID: {}", leadId);

        // 1. Fetch lead
        Lead lead = leadRepository.findById(leadId)
                .orElseThrow(() -> new ResourceNotFoundException("Lead not found with ID: " + leadId));

        // 2. Check if already unassigned
        if (lead.getAssignedTo() == null) {
            throw new IllegalStateException("Lead is already unassigned");
        }

        // Clone old lead for audit log
        Lead oldLead = cloneLead(lead);

        // 3. Unassign
        lead.setAssignedTo(null);
        Lead updatedLead = leadRepository.save(lead);

        // 4. Create Audit Log
        User performedBy = getLoggedInUser();
        createAuditLog("Lead", leadId, "UNASSIGN", oldLead, updatedLead, performedBy);

        log.info("✅ Lead unassigned successfully");

        return mapToResponse(updatedLead);
    }

 // LeadServiceImpl.java
    @Override
    @Transactional(readOnly = true)
    public List<LeadResponse> getNonConvertedLeads() {
        log.info("Fetching all non-converted leads");
        
        // Get all leads where status != 'CONVERTED'
        List<Lead> leads = leadRepository.findAll().stream()
            .filter(lead -> !"CONVERTED".equals(lead.getStatus()))
            .collect(Collectors.toList());
        
        return leads.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }
    // ==================== HELPER METHODS ====================

    // Helper method: Map Lead to LeadResponse
//    private LeadResponse mapToResponse(Lead lead) {
//        LeadResponse response = new LeadResponse();
//        response.setLeadId(lead.getLeadId());
//        response.setFullName(lead.getFullName());
//        response.setContactNumber(lead.getContactNumber());
//        response.setEmail(lead.getEmail());
//        response.setCourseInterested(lead.getCourseInterested());
//        response.setCollegeName(lead.getCollegeName());
//        response.setQualification(lead.getQualification());
//        response.setExperience(lead.getExperience());
//        response.setStatus(lead.getStatus());
//        response.setInterestLevel(lead.getInterestLevel());
//
//        if (lead.getAssignedTo() != null) {
//            response.setAssignedToUserId(lead.getAssignedTo().getUserId());
//            response.setAssignedToUserName(lead.getAssignedTo().getFullName());
//        }
//
//        if (lead.getLeadSource() != null) {
//            response.setSourceId(lead.getLeadSource().getSourceId());
//            response.setSourceName(lead.getLeadSource().getName());
//        }
//
//        response.setComments(lead.getComments());
//        response.setCreatedDate(lead.getCreatedDate());
//        response.setLastContactDate(lead.getLastContactDate());
//        response.setConversionDate(lead.getConversionDate());
//        response.setCreatedAt(lead.getCreatedAt());
//        response.setUpdatedAt(lead.getUpdatedAt());
//
//        return response;
//    }

    private LeadResponse mapToResponse(Lead lead) {
        LeadResponse response = new LeadResponse();
        response.setLeadId(lead.getLeadId());
        response.setFullName(lead.getFullName());
        response.setContactNumber(lead.getContactNumber());
        response.setEmail(lead.getEmail());
        response.setCourseInterested(lead.getCourseInterested());
        response.setCollegeName(lead.getCollegeName());
        response.setQualification(lead.getQualification());
        response.setExperience(lead.getExperience());
        response.setInterestLevel(lead.getInterestLevel());
        response.setStatus(lead.getStatus());
        
        // Lead Source
        if (lead.getLeadSource() != null) {
            response.setSourceId(lead.getLeadSource().getSourceId());
            response.setSourceName(lead.getLeadSource().getName());
        }
        
        // ✅ FIXED: Assigned To (handle null safely)
        if (lead.getAssignedTo() != null) {
            response.setAssignedToUserId(lead.getAssignedTo().getUserId());
            response.setAssignedToUserName(lead.getAssignedTo().getFullName());
        } else {
            response.setAssignedToUserId(null);
            response.setAssignedToUserName("Unassigned");
        }
        
        // ✅ FIXED: Sales Executive (handle null safely)
        if (lead.getSalesExecutive() != null) {
            response.setSalesExecutiveId(lead.getSalesExecutive().getUserId());
            response.setSalesExecutiveName(lead.getSalesExecutive().getFullName());
        } else {
            response.setSalesExecutiveId(null);
            response.setSalesExecutiveName("Unassigned");
        }
        
        // ✅ FIXED: Converted Student (handle null safely)
        if (lead.getConvertedStudent() != null) {
            response.setConvertedStudentId(lead.getConvertedStudent().getStudentId());
        } else {
            response.setConvertedStudentId(null);
        }
        
        response.setComments(lead.getComments());
        response.setCreatedDate(lead.getCreatedDate());
        response.setLastContactDate(lead.getLastContactDate());
        response.setConversionDate(lead.getConversionDate());
        response.setCreatedAt(lead.getCreatedAt());
        response.setUpdatedAt(lead.getUpdatedAt());
        
        return response;
    }


    // Helper method: Map LeadStatusHistory to LeadStatusHistoryResponse
    private LeadStatusHistoryResponse mapToHistoryResponse(LeadStatusHistory history) {
        LeadStatusHistoryResponse response = new LeadStatusHistoryResponse();
        response.setHistoryId(history.getHistoryId());
        response.setLeadId(history.getLead().getLeadId());
        response.setLeadName(history.getLead().getFullName());
        response.setOldStatus(history.getOldStatus());
        response.setNewStatus(history.getNewStatus());
        response.setChangedByUserId(history.getChangedBy().getUserId());
        response.setChangedByUserName(history.getChangedBy().getFullName());
        response.setRemarks(history.getRemarks());
        response.setChangedAt(history.getChangedAt());
        return response;
    }

    private Lead cloneLead(Lead lead) {
        Lead clone = new Lead();
        clone.setLeadId(lead.getLeadId());
        clone.setFullName(lead.getFullName());
        clone.setContactNumber(lead.getContactNumber());
        clone.setEmail(lead.getEmail());
        clone.setCourseInterested(lead.getCourseInterested());
        clone.setCollegeName(lead.getCollegeName());
        clone.setQualification(lead.getQualification());
        clone.setExperience(lead.getExperience());
        clone.setStatus(lead.getStatus());
        clone.setInterestLevel(lead.getInterestLevel());
        clone.setAssignedTo(lead.getAssignedTo());
        clone.setLeadSource(lead.getLeadSource());
        clone.setComments(lead.getComments());
        clone.setCreatedDate(lead.getCreatedDate());
        clone.setLastContactDate(lead.getLastContactDate());
        clone.setConversionDate(lead.getConversionDate());
        return clone;
    }

    /**
     * Get currently logged-in user from Spring Security Context
     */
    private User getLoggedInUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()) {
                String email = authentication.getName(); // Email from JWT
                return userRepository.findByEmail(email)
                        .orElse(null);
            }
        } catch (Exception e) {
            log.warn("Could not fetch logged-in user from SecurityContext", e);
        }
        return null;
    }

    private void createAuditLog(String entityType, Long entityId, String action,
                                Object oldValue, Object newValue, User performedBy) {
        try {
            AuditLog auditLog = new AuditLog();
            auditLog.setEntityType(entityType);
            auditLog.setEntityId(entityId);
            auditLog.setAction(action);
            auditLog.setOldValue(oldValue != null ? objectMapper.writeValueAsString(oldValue) : null);
            auditLog.setNewValue(newValue != null ? objectMapper.writeValueAsString(newValue) : null);
            auditLog.setPerformedBy(performedBy);
            auditLog.setIpAddress(getClientIp());
            auditLog.setUserAgent(httpServletRequest.getHeader("User-Agent"));
            auditLog.setPerformedAt(LocalDateTime.now());

            auditLogRepository.save(auditLog);
            log.info("Audit log created: {} {} on Lead ID: {}", action, entityType, entityId);
        } catch (JsonProcessingException e) {
            log.error("Error creating audit log", e);
        }
    }

    private String getClientIp() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0];
        }
        return httpServletRequest.getRemoteAddr();
    }
}
