package com.hnit.coursehub.dao.impl;

import com.hnit.coursehub.dao.UserDao;
import com.hnit.coursehub.entity.PageBean;
import com.hnit.coursehub.entity.User;
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

public class UserDaoImpl implements UserDao {
    @Override
    public Optional<User> findById(int id) throws SQLException {
        String sql = "SELECT id, username, password_hash, real_name, email, role, avatar_path, created_at FROM users WHERE id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapUser(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public Optional<User> findByUsername(String username) throws SQLException {
        String sql = "SELECT id, username, password_hash, real_name, email, role, avatar_path, created_at FROM users WHERE username = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(mapUser(rs)) : Optional.empty();
            }
        }
    }

    @Override
    public boolean existsByUsername(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        }
    }

    @Override
    public int insert(User user) throws SQLException {
        String sql = "INSERT INTO users(username, password_hash, real_name, email, role, avatar_path) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.setString(3, user.getRealName());
            ps.setString(4, user.getEmail());
            ps.setInt(5, user.getRole());
            ps.setString(6, user.getAvatarPath());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getInt(1) : 0;
            }
        }
    }

    @Override
    public void updateProfile(User user) throws SQLException {
        String sql = "UPDATE users SET real_name = ?, email = ?, role = ? WHERE id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, user.getRealName());
            ps.setString(2, user.getEmail());
            ps.setInt(3, user.getRole());
            ps.setInt(4, user.getId());
            ps.executeUpdate();
        }
    }

    @Override
    public void updateAvatar(int userId, String avatarPath) throws SQLException {
        String sql = "UPDATE users SET avatar_path = ? WHERE id = ?";
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, avatarPath);
            ps.setInt(2, userId);
            ps.executeUpdate();
        }
    }

    @Override
    public PageBean<User> list(String keyword, Integer role, int page, int pageSize, String sort, String direction) throws SQLException {
        List<Object> params = new ArrayList<>();
        String where = buildWhere(keyword, role, params);
        int total = count(where, params);
        String sql = "SELECT id, username, password_hash, real_name, email, role, avatar_path, created_at FROM users " +
                where + " ORDER BY " + safeSort(sort) + " " + safeDirection(direction) + " LIMIT ? OFFSET ?";
        List<User> users = new ArrayList<>();
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            int index = fillParams(ps, params);
            ps.setInt(index++, pageSize);
            ps.setInt(index, (page - 1) * pageSize);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    users.add(mapUser(rs));
                }
            }
        }
        return new PageBean<>(users, page, pageSize, total);
    }

    @Override
    public void deleteById(Connection connection, int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE id = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }

    private int count(String where, List<Object> params) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users " + where;
        try (Connection connection = DBUtil.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            fillParams(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    private String buildWhere(String keyword, Integer role, List<Object> params) {
        StringBuilder where = new StringBuilder("WHERE 1=1 ");
        if (!StringUtil.isBlank(keyword)) {
            where.append("AND (username LIKE ? OR real_name LIKE ? OR email LIKE ?) ");
            String like = "%" + keyword.trim() + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (role != null) {
            where.append("AND role = ? ");
            params.add(role);
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
        if ("username".equals(sort) || "real_name".equals(sort) || "role".equals(sort) || "created_at".equals(sort)) {
            return sort;
        }
        return "created_at";
    }

    private String safeDirection(String direction) {
        return "asc".equalsIgnoreCase(direction) ? "ASC" : "DESC";
    }

    private User mapUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(StringUtil.escapeHtml(rs.getString("username")));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setRealName(StringUtil.escapeHtml(rs.getString("real_name")));
        user.setEmail(StringUtil.escapeHtml(rs.getString("email")));
        user.setRole(rs.getInt("role"));
        user.setAvatarPath(rs.getString("avatar_path"));
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            user.setCreatedAt(createdAt.toLocalDateTime());
        }
        return user;
    }
}
