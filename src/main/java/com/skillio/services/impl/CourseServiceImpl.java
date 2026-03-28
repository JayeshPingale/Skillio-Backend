package com.skillio.services.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skillio.dto.CourseResponse;
import com.skillio.dto.CreateCourseRequest;
import com.skillio.dto.UpdateCourseRequest;
import com.skillio.entities.AuditLog;
import com.skillio.entities.Course;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.AuditLogRepository;
import com.skillio.repositories.CourseRepository;
import com.skillio.repositories.UserRepository;
import com.skillio.services.CourseService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class CourseServiceImpl implements CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final AuditLogRepository auditLogRepository;
    private final HttpServletRequest httpServletRequest;
    private final ObjectMapper objectMapper;

    @Override
    public CourseResponse createCourse(CreateCourseRequest request) {
        log.info("Creating new course: {}", request.getCourseName());

        // 1. Check if course with same name already exists
        if (courseRepository.existsByCourseName(request.getCourseName())) {
            throw new IllegalStateException("Course already exists with name: " + request.getCourseName());
        }

        // 2. Create new course
        Course course = new Course();
        course.setCourseName(request.getCourseName());
        course.setDescription(request.getDescription());
        course.setDuration(request.getDuration());
        course.setTotalFees(request.getTotalFees());
        course.setIsActive(true);

        // 3. Save to database
        Course savedCourse = courseRepository.save(course);

        // 4. Create Audit Log
        User performedBy = getLoggedInUser();
        createAuditLog("Course", savedCourse.getCourseId(), "CREATE", null, savedCourse, performedBy);

        log.info("Course created successfully with ID: {}", savedCourse.getCourseId());

        // 5. Map to response DTO
        return mapToResponse(savedCourse);
    }

    @Override
    public CourseResponse updateCourse(Long courseId, UpdateCourseRequest request) {
        log.info("Updating course with ID: {}", courseId);

        // 1. Fetch existing course
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        // Clone old course for audit log
        Course oldCourse = cloneCourse(course);

        // 2. Check if course name is being changed and if it already exists
        if (!course.getCourseName().equalsIgnoreCase(request.getCourseName())) {
            if (courseRepository.existsByCourseName(request.getCourseName())) {
                throw new IllegalStateException("Course name already taken: " + request.getCourseName());
            }
        }

        // 3. Update fields
        course.setCourseName(request.getCourseName());
        course.setDescription(request.getDescription());
        course.setDuration(request.getDuration());
        course.setTotalFees(request.getTotalFees());
        
        // ✅ Update isActive if provided
        if (request.getIsActive() != null) {
            course.setIsActive(request.getIsActive());
        }

        // 4. Save and return
        Course updatedCourse = courseRepository.save(course);

        // 5. Create Audit Log
        User performedBy = getLoggedInUser();
        createAuditLog("Course", courseId, "UPDATE", oldCourse, updatedCourse, performedBy);

        log.info("Course updated successfully with ID: {}", courseId);

        return mapToResponse(updatedCourse);
    }

    @Override
    @Transactional(readOnly = true)
    public CourseResponse getCourseById(Long courseId) {
        log.info("Fetching course with ID: {}", courseId);
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));
        return mapToResponse(course);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getAllCourses() {
        log.info("Fetching all courses");
        return courseRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CourseResponse> getActiveCourses() {
        log.info("Fetching active courses");
        return courseRepository.findByIsActiveTrue()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public void toggleCourseStatus(Long courseId) {
        log.info("Toggling status for course ID: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        // Clone old course for audit log
        Course oldCourse = cloneCourse(course);

        course.setIsActive(!course.getIsActive());
        Course updated = courseRepository.save(course);

        // Create Audit Log
        User performedBy = getLoggedInUser();
        createAuditLog("Course", courseId, "TOGGLE_STATUS", oldCourse, updated, performedBy);

        log.info("Course status toggled to: {} for ID: {}", updated.getIsActive(), courseId);
    }

    @Override
    public void deleteCourse(Long courseId) {
        log.info("Deleting course with ID: {}", courseId);

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("Course not found with ID: " + courseId));

        // Create Audit Log before deletion
        User performedBy = getLoggedInUser();
        createAuditLog("Course", courseId, "DELETE", course, null, performedBy);

        courseRepository.delete(course);
        log.info("Course deleted successfully with ID: {}", courseId);
    }

    // ==================== HELPER METHODS ====================

    // Helper method to map Course entity to CourseResponse DTO
    private CourseResponse mapToResponse(Course course) {
        CourseResponse response = new CourseResponse();
        response.setCourseId(course.getCourseId());
        response.setCourseName(course.getCourseName());
        response.setDescription(course.getDescription());
        response.setDuration(course.getDuration());
        response.setTotalFees(course.getTotalFees());
        response.setIsActive(course.getIsActive());
        response.setCreatedAt(course.getCreatedAt());
        return response;
    }

    private Course cloneCourse(Course course) {
        Course clone = new Course();
        clone.setCourseId(course.getCourseId());
        clone.setCourseName(course.getCourseName());
        clone.setDescription(course.getDescription());
        clone.setDuration(course.getDuration());
        clone.setTotalFees(course.getTotalFees());
        clone.setIsActive(course.getIsActive());
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
            log.info("Audit log created: {} {} on Course ID: {}", action, entityType, entityId);
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
