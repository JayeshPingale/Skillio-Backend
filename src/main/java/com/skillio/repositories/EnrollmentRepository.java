package com.skillio.repositories;

import com.skillio.entities.Enrollment;
import com.skillio.entities.Student;
import com.skillio.entities.Batch;
import com.skillio.entities.Course;
import com.skillio.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    List<Enrollment> findByStudent(Student student);
    List<Enrollment> findByBatch(Batch batch);
    List<Enrollment> findByCourse(Course course);
    List<Enrollment> findByAdmittedBy(User admittedBy);
    List<Enrollment> findByStatus(String status);
    
    Optional<Enrollment> findByStudentAndBatch(Student student, Batch batch);
    
    @Query("SELECT e FROM Enrollment e WHERE e.batch.batchId = :batchId AND e.status = 'ACTIVE'")
    List<Enrollment> findActiveEnrollmentsByBatch(@Param("batchId") Long batchId);
    
    // Count queries
    Long countByBatch(Batch batch);
    Long countByBatchAndStatus(Batch batch, String status);
    Long countByStatus(String status);
    Long countByAdmittedBy(User admittedBy);
	boolean existsByStudentStudentIdAndBatchBatchId(Long studentId, Long batchId);
	List<Enrollment> findByBatchBatchId(Long batchId);
	List<Enrollment> findByStudentStudentId(Long studentId);
	boolean existsByStudentStudentId(Long studentId);
	List<Enrollment> findByAdmittedByUserIdOrderByEnrollmentDateDesc(Long userId);
	
	   // ✅ CORRECT: Use admittedBy field
    List<Enrollment> findByAdmittedBy_UserId(Long userId);
    
    // OR use custom query:
    @Query("SELECT e FROM Enrollment e WHERE e.admittedBy.userId = :userId")
    List<Enrollment> findByAdmittedByUserId(@Param("userId") Long userId);
}
