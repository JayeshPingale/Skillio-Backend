package com.skillio.services;

import com.skillio.dto.CreateStudentRequest;
import com.skillio.dto.StudentResponse;
import com.skillio.dto.UpdateStudentRequest;
import java.util.List;

public interface StudentService {
    StudentResponse createStudent(CreateStudentRequest request);
    StudentResponse updateStudent(Long studentId, UpdateStudentRequest request);
    StudentResponse getStudentById(Long studentId);
    List<StudentResponse> getAllStudents();
    void deleteStudent(Long studentId);
    void changeStudentStatus(Long studentId, String newStatus);
    List<StudentResponse> getStudentsByEnrolledUser(Long userId);
}
