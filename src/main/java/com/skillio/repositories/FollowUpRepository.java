package com.skillio.repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillio.entities.FollowUp;
import com.skillio.entities.Lead;

@Repository
public interface FollowUpRepository extends JpaRepository<FollowUp, Long> {
    List<FollowUp> findByLead(Lead lead);
    List<FollowUp> findByCreatedBy_UserId(Long userId);
    List<FollowUp> findByStatus(String status);
    List<FollowUp> findByFollowUpDate(LocalDate followUpDate);
    List<FollowUp> findByNextFollowUpDateBefore(LocalDate date);
    
    List<FollowUp> findByLeadLeadIdOrderByFollowUpDateDesc(Long leadId);
    
    List<FollowUp> findByCreatedByUserIdOrderByFollowUpDateDesc(Long userId);
    
    List<FollowUp> findByStatusOrderByFollowUpDateDesc(String status);
    
    List<FollowUp> findByFollowUpDateAndStatusOrderByFollowUpDateAsc(LocalDate date, String status);
    
    List<FollowUp> findByFollowUpDateBeforeAndStatusOrderByFollowUpDateAsc(LocalDate date, String status);
}
