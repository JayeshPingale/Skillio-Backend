package com.skillio.services;

import java.util.List;

import com.skillio.dto.FollowUpRequest;
import com.skillio.dto.FollowUpResponse;
import com.skillio.dto.UpdateFollowUpRequest;

public interface FollowUpService {
    FollowUpResponse createFollowUp(FollowUpRequest request, Long userId);
    FollowUpResponse updateFollowUp(Long followUpId, UpdateFollowUpRequest request);
    FollowUpResponse getFollowUpById(Long followUpId);
    List<FollowUpResponse> getAllFollowUps();
    List<FollowUpResponse> getFollowUpsByLead(Long leadId);
    List<FollowUpResponse> getFollowUpsByUser(Long userId);
    List<FollowUpResponse> getFollowUpsByStatus(String status);
    List<FollowUpResponse> getFollowUpsDueToday();
    List<FollowUpResponse> getOverdueFollowUps();
    void markFollowUpCompleted(Long followUpId);
    void deleteFollowUp(Long followUpId);
}
