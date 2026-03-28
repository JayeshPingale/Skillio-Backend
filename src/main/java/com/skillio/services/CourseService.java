package com.skillio.services;

import java.util.List;

import com.skillio.dto.CourseResponse;
import com.skillio.dto.CreateCourseRequest;
import com.skillio.dto.UpdateCourseRequest;

public interface CourseService {
    CourseResponse createCourse(CreateCourseRequest request);
    CourseResponse updateCourse(Long courseId, UpdateCourseRequest request);
    CourseResponse getCourseById(Long courseId);
    List<CourseResponse> getAllCourses();
    List<CourseResponse> getActiveCourses();
    void toggleCourseStatus(Long courseId);
    void deleteCourse(Long courseId);
}
