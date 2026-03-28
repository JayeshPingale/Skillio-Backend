package com.skillio.services;

import com.skillio.dto.BatchResponse;
import com.skillio.dto.CreateBatchRequest;
import com.skillio.dto.UpdateBatchRequest;
import java.util.List;

public interface BatchService {
    BatchResponse createBatch(CreateBatchRequest request);
    BatchResponse updateBatch(Long batchId, UpdateBatchRequest request);
    BatchResponse getBatchById(Long batchId);
    List<BatchResponse> getAllBatches();
    List<BatchResponse> getBatchesByStatus(String status); // UPCOMING, ONGOING, COMPLETED, CANCELLED
    List<BatchResponse> getBatchesByCourse(Long courseId);
    void deleteBatch(Long batchId);
}
