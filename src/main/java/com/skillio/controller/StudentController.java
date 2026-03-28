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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skillio.dto.CreateStudentRequest;
import com.skillio.dto.StudentResponse;
import com.skillio.dto.UpdateStudentRequest;
import com.skillio.entities.User;
import com.skillio.exepection.ResourceNotFoundException;
import com.skillio.repositories.UserRepository;
import com.skillio.services.StudentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
@Slf4j
public class StudentController {

    private final StudentService studentService;
    private final UserRepository userRepository;

    // Create Student (Admin only)
    @PostMapping
    @PreAuthorize("hasAuthority('STUDENT_CREATE')")
    public ResponseEntity<StudentResponse> createStudent(@Valid @RequestBody CreateStudentRequest request) {
        log.info("🔥 Creating student: {}", request.getFullName());
        return ResponseEntity.status(HttpStatus.CREATED).body(studentService.createStudent(request));
    }

    // ✅ FIXED: Update Student (Admin + Sales Executive can update their enrolled students)
    @PutMapping("/{studentId}")
    @PreAuthorize("hasAuthority('STUDENT_UPDATE')")
    public ResponseEntity<StudentResponse> updateStudent(
            @PathVariable Long studentId,
            @Valid @RequestBody UpdateStudentRequest request) {
        
        log.info("🔥 Updating student ID: {}", studentId);
        return ResponseEntity.ok(studentService.updateStudent(studentId, request));
    }

    // Get Student by ID
    @GetMapping("/{studentId}")
    @PreAuthorize("hasAuthority('STUDENT_READ')")
    public ResponseEntity<StudentResponse> getStudentById(@PathVariable Long studentId) {
        log.info("🔥 Getting student ID: {}", studentId);
        return ResponseEntity.ok(studentService.getStudentById(studentId));
    }

    // Get All Students (Admin + Sales Exec can view)
    @GetMapping
    @PreAuthorize("hasAuthority('STUDENT_LIST')")
    public ResponseEntity<List<StudentResponse>> getAllStudents() {
        log.info("🔥 Getting all students");
        return ResponseEntity.ok(studentService.getAllStudents());
    }

    // Delete Student (Admin only)
    @DeleteMapping("/{studentId}")
    @PreAuthorize("hasAuthority('STUDENT_DELETE')")
    public ResponseEntity<Void> deleteStudent(@PathVariable Long studentId) {
        log.info("🔥 Deleting student ID: {}", studentId);
        studentService.deleteStudent(studentId);
        return ResponseEntity.noContent().build();
    }

    // Change Student Status (Admin only)
    @PatchMapping("/{studentId}/status")
    @PreAuthorize("hasAuthority('STUDENT_UPDATE')")
    public ResponseEntity<Void> changeStudentStatus(
            @PathVariable Long studentId,
            @RequestParam String status) {
        
        log.info("🔥 Changing student ID {} status to: {}", studentId, status);
        studentService.changeStudentStatus(studentId, status);
        return ResponseEntity.noContent().build();
    }

    // ✅ Get My Students (Sales Executive - students they enrolled)
    @GetMapping("/my-students")
    @PreAuthorize("hasAuthority('STUDENT_LIST')")
    public ResponseEntity<List<StudentResponse>> getMyStudents(Authentication authentication) {
        
        Long userId = extractUserId(authentication);
        log.info("🔥 Getting students enrolled by Sales Executive ID: {}", userId);
        
        return ResponseEntity.ok(studentService.getStudentsByEnrolledUser(userId));
    }
    private Long extractUserId(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return user.getUserId();
    }
}
