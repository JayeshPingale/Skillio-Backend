package com.skillio.repositories;

import com.skillio.entities.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    Optional<Course> findByCourseName(String courseName);
    List<Course> findByIsActive(Boolean isActive);
	List<Course> findByIsActiveTrue();
	boolean existsByCourseName(String courseName);
}
