package com.hnit.coursehub.service;

import com.hnit.coursehub.entity.Course;
import com.hnit.coursehub.entity.PageBean;

import java.util.List;
import java.util.Optional;

public interface CourseService {
    Optional<Course> findById(int id);

    PageBean<Course> list(String keyword, String category, int page, int pageSize, String sort, String direction);

    List<Course> listAllOpen();

    int create(Course course);

    void update(Course course);

    void deleteWithEnrollments(int courseId);
}
