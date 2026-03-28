package com.skillio.services;

import com.skillio.dto.CreateTargetRequest;
import com.skillio.dto.UpdateTargetRequest;
import com.skillio.dto.UpdateTargetAchievementRequest;
import com.skillio.dto.TargetResponse;
import com.skillio.entities.Target;

import java.util.List;

public interface TargetService {
    
    // Create
    TargetResponse createTarget(CreateTargetRequest request, Long loggedInUserId);
    
    // Read
    TargetResponse getTargetById(Long targetId);
    List<TargetResponse> getAllTargets();
    List<TargetResponse> getTargetsByUser(Long userId);
    List<TargetResponse> getTargetsByStatus(String status);
    List<TargetResponse> getActiveTargets();
    
    // Update
    TargetResponse updateTarget(Long targetId, UpdateTargetRequest request, Long loggedInUserId);
    TargetResponse updateTargetAchievement(Long targetId, UpdateTargetAchievementRequest request, Long loggedInUserId);
    TargetResponse markAsCompleted(Long targetId, Long loggedInUserId);
    
    // Delete
    void deleteTarget(Long targetId, Long loggedInUserId);
    
    // Helper
    Target getTargetEntityById(Long targetId);
    void evaluateTargetStatus(Target target);
}
