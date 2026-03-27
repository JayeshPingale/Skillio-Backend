package com.skillio.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillio.entities.Lead;
import com.skillio.entities.LeadStatusHistory;

@Repository
public interface LeadStatusHistoryRepository extends JpaRepository<LeadStatusHistory, Long> {
    List<LeadStatusHistory> findByLead(Lead lead);
    List<LeadStatusHistory> findByLeadOrderByChangedAtDesc(Lead lead);
    List<LeadStatusHistory> findByChangedBy_UserId(Long userId);
	List<LeadStatusHistory> findByLeadLeadIdOrderByChangedAtDesc(Long leadId);
	
	    
	    // Find all status changes made by a specific user
	    List<LeadStatusHistory> findByChangedByUserIdOrderByChangedAtDesc(Long userId);
	    
	    // Get all history ordered by most recent first
	    List<LeadStatusHistory> findAllByOrderByChangedAtDesc();
}
