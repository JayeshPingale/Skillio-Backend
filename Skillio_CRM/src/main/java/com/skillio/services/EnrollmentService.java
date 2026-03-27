package com.skillio.services;

import java.util.List;

import com.skillio.dto.ConvertLeadAndEnrollResponse;
import com.skillio.dto.CreateEnrollmentFromLeadRequest;
import com.skillio.dto.CreateEnrollmentRequest;
import com.skillio.dto.EnrollmentResponse;
import com.skillio.dto.UpdateEnrollmentRequest;

public interface EnrollmentService {
    EnrollmentResponse createEnrollment(CreateEnrollmentRequest request, Long admittedByUserId);
    EnrollmentResponse updateEnrollment(Long enrollmentId, UpdateEnrollmentRequest request);
    EnrollmentResponse getEnrollmentById(Long enrollmentId);
    List<EnrollmentResponse> getAllEnrollments();
    List<EnrollmentResponse> getEnrollmentsByStudent(Long studentId);
    List<EnrollmentResponse> getEnrollmentsByBatch(Long batchId);
    void deleteEnrollment(Long enrollmentId);
    void changeEnrollmentStatus(Long enrollmentId, String status);
//    EnrollmentResponse convertLeadAndEnroll(CreateEnrollmentFromLeadRequest request, Long admittedByUserId);
    ConvertLeadAndEnrollResponse convertLeadAndEnroll(CreateEnrollmentFromLeadRequest request, Long admittedByUserId);
    List<EnrollmentResponse> getEnrollmentsByAdmittedUser(Long userId);
}
