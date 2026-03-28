package com.skillio.repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.skillio.entities.Student;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {
    Optional<Student> findByStudentCode(String studentCode);
    Optional<Student> findByEmail(String email);
    List<Student> findByStatus(String status);
    List<Student> findByStatusOrderByEnrollmentDateDesc(String status);
    
    // Count queries
    Long countByStatus(String status);
	boolean existsByEmail(String email);
	boolean existsByContactNumber(String contactNumber);
	  @Query("SELECT DISTINCT s FROM Student s " +
	           "JOIN Enrollment e ON e.student.studentId = s.studentId " +
	           "WHERE e.admittedBy.userId = :userId")
	    List<Student> findStudentsByEnrolledUser(@Param("userId") Long userId);
}
