package com.hnit.coursehub.dao;

import com.hnit.coursehub.entity.Enrollment;
import com.hnit.coursehub.entity.EnrollmentFile;
import com.hnit.coursehub.entity.PageBean;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public interface EnrollmentDao {
    Optional<Enrollment> findById(int id) throws SQLException;

    Optional<Enrollment> findByUserAndCourse(int userId, int courseId) throws SQLException;

    PageBean<Enrollment> list(Integer userId, String keyword, String status, int page, int pageSize, String sort, String direction) throws SQLException;

    List<Enrollment> listForExport(Integer userId) throws SQLException;

    int insert(Connection connection, Enrollment enrollment) throws SQLException;

    void update(Enrollment enrollment) throws SQLException;

    void deleteById(Connection connection, int id) throws SQLException;

    void deleteByUserId(Connection connection, int userId) throws SQLException;

    void deleteByCourseId(Connection connection, int courseId) throws SQLException;

    List<Integer> listCourseIdsByUserId(Connection connection, int userId) throws SQLException;

    void insertFile(Connection connection, int enrollmentId, EnrollmentFile file) throws SQLException;

    List<EnrollmentFile> listFiles(int enrollmentId) throws SQLException;

    Optional<EnrollmentFile> findFileById(int fileId) throws SQLException;
}
