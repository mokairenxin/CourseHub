package com.hnit.coursehub.dao;

import com.hnit.coursehub.entity.Course;
import com.hnit.coursehub.entity.PageBean;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface CourseDao {
    Optional<Course> findById(int id) throws SQLException;

    PageBean<Course> list(String keyword, String category, int page, int pageSize, String sort, String direction) throws SQLException;

    List<Course> listAllOpen() throws SQLException;

    int insert(Course course) throws SQLException;

    void update(Course course) throws SQLException;

    void deleteById(Connection connection, int id) throws SQLException;

    boolean decreaseCapacityIfAvailable(Connection connection, int courseId) throws SQLException;

    void increaseCapacity(Connection connection, int courseId) throws SQLException;

    void refreshEnrollmentCount(Connection connection, int courseId) throws SQLException;
}
