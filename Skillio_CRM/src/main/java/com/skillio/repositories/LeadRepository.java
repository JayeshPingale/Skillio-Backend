package com.skillio.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.skillio.entities.Lead;
import com.skillio.entities.Student;
import com.skillio.entities.User;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {
    List<Lead> findByAssignedTo(User assignedTo);
    List<Lead> findByStatus(String status);
    List<Lead> findByInterestLevel(String interestLevel);
    List<Lead> findByLeadSourceSourceId(Long sourceId);
    List<Lead> findByAssignedToAndStatus(User assignedTo, String status);
    
    // Count queries for dashboard
    Long countByAssignedTo(User assignedTo);
    Long countByAssignedToAndStatus(User assignedTo, String status);
	Optional<Lead> findByConvertedStudent(Student student);
	Optional<Lead> findByConvertedStudentStudentId(Long studentId);
	
	   @Query("SELECT l FROM Lead l WHERE l.status != 'CONVERTED' ORDER BY l.createdDate DESC")
	    List<Lead> findNonConvertedLeads();
	    
	    // Optional: Find by specific statuses
	    List<Lead> findByStatusIn(List<String> statuses);
		List<Lead> findBySalesExecutiveAndStatus(User salesExecutive, String string);
}
