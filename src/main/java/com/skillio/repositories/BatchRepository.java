package com.skillio.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skillio.entities.Batch;

@Repository
public interface BatchRepository extends JpaRepository<Batch, Long> {
    boolean existsByBatchCode(String batchCode);
    
    List<Batch> findByStatus(String status);
    
    List<Batch> findByCourseCourseId(Long courseId);

	List<Batch> findByStatusIgnoreCase(String status);
}
