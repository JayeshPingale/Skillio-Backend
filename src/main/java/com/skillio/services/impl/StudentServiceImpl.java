package com.skillio.services.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillio.dto.CreateStudentRequest;
import com.skillio.dto.StudentResponse;
import com.skillio.dto.UpdateStudentRequest;
import com.skillio.entities.AuditLog;
import com.skillio.entities.Student;
import com.skillio.entities.User;
import com.skillio.exepection.DuplicateEmailException;
import com.skillio.exepection.DuplicatePhoneException;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.AuditLogRepository;
import com.skillio.repositories.StudentRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.services.StudentService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest httpServletRequest;
    private final ObjectMapper objectMapper;

    public void validateAgeRange(LocalDate dob) {
        if (dob == null) return;
        LocalDate today = LocalDate.now();
        int age = Period.between(dob, today).getYears();
        if (age < 15 || age > 70) {
            throw new IllegalArgumentException("Age must be between 15 and 70 years");
        }
    }
    @Override
    public StudentResponse createStudent(CreateStudentRequest request) {
        log.info("Creating new student with email: {}", request.getEmail());

        // Check for duplicate email & phone
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Student with this email already exists: " + request.getEmail());
        }
        if (studentRepository.existsByContactNumber(request.getContactNumber())) {
            throw new DuplicatePhoneException("Student with this Contact Number already exists: " + request.getContactNumber());
        }

        // Generate next student code
        String studentCode = generateStudentCode();

        Student student = new Student();
        student.setStudentCode(studentCode);
        student.setFullName(request.getFullName());
        student.setEmail(request.getEmail());
        student.setContactNumber(request.getContactNumber());
        student.setAlternateContact(request.getAlternateContact());
        student.setAddress(request.getAddress());
        student.setEnrollmentDate(LocalDate.now());
        validateAgeRange(request.getDateOfBirth());

        student.setStatus("ACTIVE");

        Student saved = studentRepository.save(student);

        // Create Audit Log
        User performedBy = getLoggedInUser();
        createAuditLog("Student", saved.getStudentId(), "CREATE", null, saved, performedBy);

        log.info("Student created successfully with ID: {} and code: {}", saved.getStudentId(), saved.getStudentCode());

        return mapToResponse(saved);
    }

    @Override
    public StudentResponse updateStudent(Long studentId, UpdateStudentRequest request) {
        log.info("Updating student with ID: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        // Clone old student for audit log
        Student oldStudent = cloneStudent(student);

        student.setFullName(request.getFullName());
        student.setEmail(request.getEmail());
        student.setContactNumber(request.getContactNumber());
        student.setAlternateContact(request.getAlternateContact());
        student.setAddress(request.getAddress());
//        student.setRemarks(request.getRemarks());
        if (request.getDateOfBirth() != null) {
            student.setDateOfBirth(LocalDate.parse(request.getDateOfBirth().toString()));
        }
        Student updated = studentRepository.save(student);

        // Create Audit Log
        User performedBy = getLoggedInUser();
        createAuditLog("Student", studentId, "UPDATE", oldStudent, updated, performedBy);

        log.info("Student updated successfully with ID: {}", studentId);

        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public StudentResponse getStudentById(Long studentId) {
        log.info("Fetching student with ID: {}", studentId);
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));
        return mapToResponse(student);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getAllStudents() {
        log.info("Fetching all students");
        return studentRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteStudent(Long studentId) {
        log.info("Deleting student with ID: {}", studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        // Create Audit Log before deletion
        User performedBy = getLoggedInUser();
        createAuditLog("Student", studentId, "DELETE", student, null, performedBy);

        studentRepository.delete(student);
        log.info("Student deleted successfully with ID: {}", studentId);
    }

//    @Override
//    @Transactional(readOnly = true)
//    public List<StudentResponse> getStudentsByEnrolledUser(Long userId) {
//        log.info("Fetching students enrolled by user ID: {}", userId);
//        
//        // Validate user exists
//        if (!userRepository.existsById(userId)) {
//            throw new ResourceNotFoundException("User not found with ID: " + userId);
//        }
//        
//        return studentRepository.findStudentsByEnrolledUser(userId).stream()
//            .map(this::mapToResponse)
//            .collect(Collectors.toList());
//    }
    
    @Override
    @Transactional(readOnly = true)
    public List<StudentResponse> getStudentsByEnrolledUser(Long userId) {
        log.info("Fetching students enrolled by user ID: {}", userId);
        
        // Validate user exists
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        
        // ✅ This should fetch students enrolled by this Sales Executive
        return studentRepository.findStudentsByEnrolledUser(userId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void changeStudentStatus(Long studentId, String newStatus) {
        log.info("Changing status for student ID: {} to {}", studentId, newStatus);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found: " + studentId));

        // Clone old student for audit log
        Student oldStudent = cloneStudent(student);

        student.setStatus(newStatus);
        Student updated = studentRepository.save(student);

        // Create Audit Log
        User performedBy = getLoggedInUser();
        createAuditLog("Student", studentId, "STATUS_CHANGE", oldStudent, updated, performedBy);

        log.info("Student status changed successfully to: {}", newStatus);
    }

    // ==================== HELPER METHODS ====================

    // Generates code like STU001, STU002, ...
    private String generateStudentCode() {
        Long count = studentRepository.count() + 1;
        return String.format("STU%03d", count);
    }

    private StudentResponse mapToResponse(Student student) {
        StudentResponse res = new StudentResponse();
        res.setStudentId(student.getStudentId());
        res.setStudentCode(student.getStudentCode());
        res.setFullName(student.getFullName());
        res.setEmail(student.getEmail());
        res.setContactNumber(student.getContactNumber());
        res.setAlternateContact(student.getAlternateContact());
        res.setAddress(student.getAddress());
        res.setDateOfBirth(student.getDateOfBirth());      // ✅ ab type match
        res.setEnrollmentDate(student.getEnrollmentDate());
        res.setStatus(student.getStatus());
        res.setRemarks(student.getRemarks());
        res.setCreatedAt(student.getCreatedAt());
        res.setUpdatedAt(student.getUpdatedAt());
        return res;
    }

    private Student cloneStudent(Student student) {
        Student clone = new Student();
        clone.setStudentId(student.getStudentId());
        clone.setStudentCode(student.getStudentCode());
        clone.setFullName(student.getFullName());
        clone.setEmail(student.getEmail());
        clone.setContactNumber(student.getContactNumber());
        clone.setAlternateContact(student.getAlternateContact());
        clone.setAddress(student.getAddress());
        clone.setEnrollmentDate(student.getEnrollmentDate());
        clone.setStatus(student.getStatus());
//        clone.setRemarks(student.getRemarks());
        clone.setDateOfBirth(student.getDateOfBirth()); 
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
            log.info("Audit log created: {} {} on Student ID: {}", action, entityType, entityId);
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
