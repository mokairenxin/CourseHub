package com.hnit.coursehub.dao.impl;

import com.hnit.coursehub.dao.CourseDao;
import com.hnit.coursehub.entity.Course;
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

public class CourseDaoImpl implements CourseDao {
    @Override
    public Optional<Course> findById(int id) throws SQLException {
        String sql = courseSelect() + " WHERE c.id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapCourse(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public PageBean<Course> list(String keyword, String category, int page, int pageSize, String sort, String direction) throws SQLException {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(keyword, category, params);
        int total = count(where, params);
        String sql = courseSelect() + " " + where + " ORDER BY " + safeSort(sort) + " " + safeDirection(direction) + " LIMIT ? OFFSET ?";
        List<Course> courses = new ArrayList<>();
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            int index = fillParams(ps, params);
            ps.setInt(index++, pageSize);
            ps.setInt(index, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    courses.add(mapCourse(rs));
                }
            }
        }
        return new PageBean<>(courses, page, pageSize, total);
    }

    @Override
    public List<Course> listAllOpen() throws SQLException {
        String sql = courseSelect() + " WHERE c.capacity > 0 ORDER BY c.course_name ASC";
        List<Course> courses = new ArrayList<>();
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                courses.add(mapCourse(rs));
            }
        }
        return courses;
    }

    @Override
    public int insert(Course course) throws SQLException {
        String sql = "INSERT INTO courses(course_code, course_name, teacher, category, capacity, enrolled_count, syllabus_path, cover_path, description, created_by) " +
                "VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?, ?)";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            fillCourseBase(ps, course);
            ps.setInt(9, course.getCreatedBy());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    @Override
    public void update(Course course) throws SQLException {
        String sql = "UPDATE courses SET course_code = ?, course_name = ?, teacher = ?, category = ?, capacity = ?, syllabus_path = ?, cover_path = ?, description = ? WHERE id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillCourseBase(ps, course);
            ps.setInt(9, course.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void deleteById(Connection connection, int id) throws SQLException {
        String sql = "DELETE FROM courses WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    @Override
    public boolean decreaseCapacityIfAvailable(Connection connection, int courseId) throws SQLException {
        String sql = "UPDATE courses SET capacity = capacity - 1, enrolled_count = enrolled_count + 1 WHERE id = ? AND capacity > 0";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            return ps.executeUpdate() == 1;
        }
    }

    @Override
    public void increaseCapacity(Connection connection, int courseId) throws SQLException {
        String sql = "UPDATE courses SET capacity = capacity + 1, enrolled_count = CASE WHEN enrolled_count > 0 THEN enrolled_count - 1 ELSE 0 END WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.executeUpdate();
        }
    }

    @Override
    public void refreshEnrollmentCount(Connection connection, int courseId) throws SQLException {
        String sql = "UPDATE courses SET enrolled_count = (SELECT COUNT(*) FROM enrollments WHERE course_id = ? AND status <> 'DROPPED') WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, courseId);
            ps.setInt(2, courseId);
            ps.executeUpdate();
        }
    }

    private void fillCourseBase(PreparedStatement ps, Course course) throws SQLException {
        ps.setString(1, course.getCourseCode());
        ps.setString(2, course.getCourseName());
        ps.setString(3, course.getTeacher());
        ps.setString(4, course.getCategory());
        ps.setInt(5, course.getCapacity());
        ps.setString(6, course.getSyllabusPath());
        ps.setString(7, course.getCoverPath());
        ps.setString(8, course.getDescription());
    }

    private int count(String where, List<Object> params) throws SQLException {
        String sql = "SELECT COUNT(*) FROM courses c " + where;
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private String courseSelect() {
        return "SELECT c.id, c.course_code, c.course_name, c.teacher, c.category, c.capacity, c.enrolled_count, " +
                "c.syllabus_path, c.cover_path, c.description, c.created_by, c.created_at FROM courses c";
    }

    private String buildWhere(String keyword, String category, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE 1=1 ");
        if (!StringUtil.isBlank(keyword)) {
            where.append("AND (c.course_name LIKE ? OR c.course_code LIKE ? OR c.teacher LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (!StringUtil.isBlank(category)) {
            where.append("AND c.category = ? ");
            params.add(category.trim());
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
        if ("course_name".equals(sort) || "course_code".equals(sort) || "teacher".equals(sort)
                || "category".equals(sort) || "capacity".equals(sort) || "created_at".equals(sort)) {
            return "c." + sort;
        }
        return "c.created_at";
    }

    private String safeDirection(String direction) {
        return "asc".equalsIgnoreCase(direction) ? "ASC" : "DESC";
    }

    private Course mapCourse(ResultSet rs) throws SQLException {
        Course course = new Course();
        course.setId(rs.getInt("id"));
        course.setCourseCode(StringUtil.escapeHtml(rs.getString("course_code")));
        course.setCourseName(StringUtil.escapeHtml(rs.getString("course_name")));
        course.setTeacher(StringUtil.escapeHtml(rs.getString("teacher")));
        course.setCategory(StringUtil.escapeHtml(rs.getString("category")));
        course.setCapacity(rs.getInt("capacity"));
        course.setEnrolledCount(rs.getInt("enrolled_count"));
        course.setSyllabusPath(rs.getString("syllabus_path"));
        course.setCoverPath(rs.getString("cover_path"));
        course.setDescription(StringUtil.escapeHtml(rs.getString("description")));
        course.setCreatedBy(rs.getInt("created_by"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            course.setCreatedAt(createdAt.toLocalDateTime());
        }
        return course;
    }
}
