package com.skillio.services;

import com.skillio.dto.LeadStatusHistoryResponse;

import java.util.List;

public interface LeadStatusHistoryService {
    List<LeadStatusHistoryResponse> getHistoryByLead(Long leadId);
    List<LeadStatusHistoryResponse> getHistoryByUser(Long userId);
    List<LeadStatusHistoryResponse> getAllHistory();
}
