package com.skillio.services;

import java.math.BigDecimal;
import java.util.List;

import com.skillio.dto.CreateStudentFeesRequest;
import com.skillio.dto.StudentFeesResponse;
import com.skillio.dto.UpdateStudentFeesRequest;
import com.skillio.entities.StudentFees;

public interface StudentFeesService {
    
    // Create
    StudentFeesResponse createStudentFees(CreateStudentFeesRequest request, Long loggedInUserId);
    
    // Read
    StudentFeesResponse getStudentFeesById(Long feesId);
    List<StudentFeesResponse> getAllStudentFees();
    StudentFeesResponse getStudentFeesByEnrollmentId(Long enrollmentId);
    List<StudentFeesResponse> getStudentFeesByPaymentStatus(String paymentStatus);
    List<StudentFeesResponse> getOverdueStudentFees();
    
    // Update
    StudentFeesResponse updateStudentFees(Long feesId, UpdateStudentFeesRequest request, Long loggedInUserId);
    StudentFeesResponse applyDiscount(Long feesId, BigDecimal discountAmount, String discountReason, Long loggedInUserId);
    
    // Delete
    void deleteStudentFees(Long feesId, Long loggedInUserId);
    
 // In StudentFeesService.java interface
    List<StudentFeesResponse> getStudentFeesBySalesExecutive(Long salesExecutiveId);

    // Helper
    StudentFees getStudentFeesEntityById(Long feesId);
}
