package com.skillio.services;

import com.skillio.dto.CreateLeadSourceRequest;
import com.skillio.dto.LeadSourceResponse;

import java.util.List;

public interface LeadSourceService {
    LeadSourceResponse createLeadSource(CreateLeadSourceRequest request);
    LeadSourceResponse getLeadSourceById(Long sourceId);
    List<LeadSourceResponse> getAllLeadSources();
    List<LeadSourceResponse> getActiveLeadSources();
    LeadSourceResponse updateLeadSource(Long sourceId, CreateLeadSourceRequest request);
    void deleteLeadSource(Long sourceId);
    void toggleLeadSourceStatus(Long sourceId);
}
