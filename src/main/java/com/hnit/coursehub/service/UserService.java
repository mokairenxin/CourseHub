package com.hnit.coursehub.service;

import com.hnit.coursehub.entity.PageBean;
import com.hnit.coursehub.entity.User;

import java.util.Optional;

public interface UserService {
    User register(String username, String password, String realName, String email);

    Optional<User> login(String username, String password);

    boolean existsUsername(String username);

    Optional<User> findById(int id);

    PageBean<User> list(String keyword, Integer role, int page, int pageSize, String sort, String direction);

    void updateProfile(User user);

    void updateAvatar(int userId, String avatarPath);

    void deleteUserWithData(int userId);
}
