package com.hnit.coursehub.dao.impl;

import com.hnit.coursehub.dao.EnrollmentDao;
import com.hnit.coursehub.entity.Enrollment;
import com.hnit.coursehub.entity.EnrollmentFile;
import com.hnit.coursehub.entity.PageBean;
import com.hnit.coursehub.util.DBUtil;
import com.hnit.coursehub.util.StringUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EnrollmentDaoImpl implements EnrollmentDao {
    @Override
    public Optional<Enrollment> findById(int id) throws SQLException {
        String sql = enrollmentSelect() + " WHERE e.id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapEnrollment(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Optional<Enrollment> findByUserAndCourse(int userId, int courseId) throws SQLException {
        String sql = enrollmentSelect() + " WHERE e.user_id = ? AND e.course_id = ? AND e.status <> 'DROPPED'";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, courseId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapEnrollment(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public PageBean<Enrollment> list(Integer userId, String keyword, String status, int page, int pageSize, String sort, String direction) throws SQLException {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, keyword, status, params);
        int total = count(where, params);
        String sql = enrollmentSelect() + " " + where + " ORDER BY " + safeSort(sort) + " " + safeDirection(direction) + " LIMIT ? OFFSET ?";
        List<Enrollment> enrollments = new ArrayList<>();
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            int index = fillParams(ps, params);
            ps.setInt(index++, pageSize);
            ps.setInt(index, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapEnrollment(rs));
                }
            }
        }
        for (Enrollment enrollment : enrollments) {
            enrollment.getFiles().addAll(listFiles(enrollment.getId()));
        }
        return new PageBean<>(enrollments, page, pageSize, total);
    }

    @Override
    public List<Enrollment> listForExport(Integer userId) throws SQLException {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(userId, "", "", params);
        String sql = enrollmentSelect() + " " + where + " ORDER BY e.created_at DESC";
        List<Enrollment> enrollments = new ArrayList<>();
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    enrollments.add(mapEnrollment(rs));
                }
            }
        }
        return enrollments;
    }

    @Override
    public int insert(Connection connection, Enrollment enrollment) throws SQLException {
        String sql = "INSERT INTO enrollments(user_id, course_id, status, note) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, enrollment.getUserId());
            ps.setInt(2, enrollment.getCourseId());
            ps.setString(3, enrollment.getStatus());
            ps.setString(4, enrollment.getNote());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    @Override
    public void update(Enrollment enrollment) throws SQLException {
        String sql = "UPDATE enrollments SET status = ?, note = ? WHERE id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, enrollment.getStatus());
            ps.setString(2, enrollment.getNote());
            ps.setInt(3, enrollment.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteById(Connection connection, int id) throws SQLException {
        String sql = "DELETE FROM enrollments WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteByUserId(Connection connection, int userId) throws SQLException {
        String sql = "DELETE FROM enrollments WHERE user_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteByCourseId(Connection connection, int courseId) throws SQLException {
        String sql = "DELETE FROM enrollments WHERE course_id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Integer> listCourseIdsByUserId(Connection connection, int userId) throws SQLException {
        String sql = "SELECT course_id FROM enrollments WHERE user_id = ?";
        List<Integer> courseIds = new ArrayList<>();
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    courseIds.add(rs.getInt("course_id"));
                }
            }
        }
        return courseIds;
    }

    @Override
    public void insertFile(Connection connection, int enrollmentId, EnrollmentFile file) throws SQLException {
        String sql = "INSERT INTO enrollment_files(enrollment_id, original_name, stored_name, file_path, content_type, file_size) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            ps.setString(2, file.getOriginalName());
            ps.setString(3, file.getStoredName());
            ps.setString(4, file.getFilePath());
            ps.setString(5, file.getContentType());
            ps.setLong(6, file.getFileSize());
            ps.executeUpdate();
        }
    }

    @Override
    public List<EnrollmentFile> listFiles(int enrollmentId) throws SQLException {
        String sql = "SELECT id, enrollment_id, original_name, stored_name, file_path, content_type, file_size, uploaded_at FROM enrollment_files WHERE enrollment_id = ? ORDER BY uploaded_at DESC";
        List<EnrollmentFile> files = new ArrayList<>();
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, enrollmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    files.add(mapFile(rs));
                }
            }
        }
        return files;
    }

    @Override
    public Optional<EnrollmentFile> findFileById(int fileId) throws SQLException {
        String sql = "SELECT id, enrollment_id, original_name, stored_name, file_path, content_type, file_size, uploaded_at FROM enrollment_files WHERE id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, fileId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapFile(rs)) : Optional.empty();
            }
        }
    }

    private int count(String where, List<Object> params) throws SQLException {
        String sql = "SELECT COUNT(*) FROM enrollments e JOIN users u ON e.user_id = u.id JOIN courses c ON e.course_id = c.id " + where;
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private String enrollmentSelect() {
        return "SELECT e.id, e.user_id, e.course_id, e.status, e.note, e.created_at, " +
                "u.username, u.real_name, c.course_code, c.course_name, c.teacher, c.category " +
                "FROM enrollments e JOIN users u ON e.user_id = u.id JOIN courses c ON e.course_id = c.id";
    }

    private String buildWhere(Integer userId, String keyword, String status, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE 1=1 ");
        if (userId != null) {
            where.append("AND e.user_id = ? ");
            params.add(userId);
        }
        if (!StringUtil.isBlank(keyword)) {
            where.append("AND (u.username LIKE ? OR u.real_name LIKE ? OR c.course_name LIKE ? OR c.course_code LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (!StringUtil.isBlank(status)) {
            where.append("AND e.status = ? ");
            params.add(status.trim());
        }
        return where.toString();
    }

    private int fillParams(PreparedStatement ps, List<Object> params) throws SQLException {
        int index = 1;
        for (Object param : params) {
            ps.setObject(index++, param);
        }
        return index;
    }

    private String safeSort(String sort) {
        if ("created_at".equals(sort)) {
            return "e.created_at";
        }
        if ("username".equals(sort)) {
            return "u.username";
        }
        if ("course_name".equals(sort)) {
            return "c.course_name";
        }
        if ("status".equals(sort)) {
            return "e.status";
        }
        return "e.created_at";
    }

    private String safeDirection(String direction) {
        return "asc".equalsIgnoreCase(direction) ? "ASC" : "DESC";
    }

    private Enrollment mapEnrollment(ResultSet rs) throws SQLException {
        Enrollment enrollment = new Enrollment();
        enrollment.setId(rs.getInt("id"));
        enrollment.setUserId(rs.getInt("user_id"));
        enrollment.setCourseId(rs.getInt("course_id"));
        enrollment.setStatus(StringUtil.escapeHtml(rs.getString("status")));
        enrollment.setNote(StringUtil.escapeHtml(rs.getString("note")));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            enrollment.setCreatedAt(createdAt.toLocalDateTime());
        }
        enrollment.setUsername(StringUtil.escapeHtml(rs.getString("username")));
        enrollment.setRealName(StringUtil.escapeHtml(rs.getString("real_name")));
        enrollment.setCourseCode(StringUtil.escapeHtml(rs.getString("course_code")));
        enrollment.setCourseName(StringUtil.escapeHtml(rs.getString("course_name")));
        enrollment.setTeacher(StringUtil.escapeHtml(rs.getString("teacher")));
        enrollment.setCategory(StringUtil.escapeHtml(rs.getString("category")));
        return enrollment;
    }

    private EnrollmentFile mapFile(ResultSet rs) throws SQLException {
        EnrollmentFile file = new EnrollmentFile();
        file.setId(rs.getInt("id"));
        file.setEnrollmentId(rs.getInt("enrollment_id"));
        file.setOriginalName(StringUtil.escapeHtml(rs.getString("original_name")));
        file.setStoredName(rs.getString("stored_name"));
        file.setFilePath(rs.getString("file_path"));
        file.setContentType(rs.getString("content_type"));
        file.setFileSize(rs.getLong("file_size"));
        Timestamp uploadedAt = rs.getTimestamp("uploaded_at");
        if (uploadedAt != null) {
            file.setUploadedAt(uploadedAt.toLocalDateTime());
        }
        return file;
    }
}
