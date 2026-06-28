package com.hnit.coursehub.dao;

import com.hnit.coursehub.entity.PageBean;
import com.hnit.coursehub.entity.User;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;

public interface UserDao {
    Optional<User> findById(int id) throws SQLException;

    Optional<User> findByUsername(String username) throws SQLException;

    boolean existsByUsername(String username) throws SQLException;

    int insert(User user) throws SQLException;

    void updateProfile(User user) throws SQLException;

    void updateAvatar(int userId, String avatarPath) throws SQLException;

    PageBean<User> list(String keyword, Integer role, int page, int pageSize, String sort, String direction) throws SQLException;

    void deleteById(Connection connection, int userId) throws SQLException;
}
