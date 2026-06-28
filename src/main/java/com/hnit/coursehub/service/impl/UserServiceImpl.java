package com.hnit.coursehub.service.impl;

import com.hnit.coursehub.dao.EnrollmentDao;
import com.hnit.coursehub.dao.CourseDao;
import com.hnit.coursehub.dao.UserDao;
import com.hnit.coursehub.dao.impl.CourseDaoImpl;
import com.hnit.coursehub.dao.impl.EnrollmentDaoImpl;
import com.hnit.coursehub.dao.impl.UserDaoImpl;
import com.hnit.coursehub.entity.PageBean;
import com.hnit.coursehub.entity.User;
import com.hnit.coursehub.service.UserService;
import com.hnit.coursehub.util.DBUtil;
import com.hnit.coursehub.util.PasswordUtil;
import com.hnit.coursehub.util.StringUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserServiceImpl implements UserService {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9_]{3,20}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[\\w.+-]+@[\\w.-]+\\.[A-Za-z]{2,}$");

    private final UserDao userDao = new UserDaoImpl();
    private final EnrollmentDao enrollmentDao = new EnrollmentDaoImpl();
    private final CourseDao courseDao = new CourseDaoImpl();

    @Override
    public User register(String username, String password, String realName, String email) {
        username = StringUtil.trim(username);
        realName = StringUtil.trim(realName);
        email = StringUtil.trim(email);
        validateUser(username, password, realName, email);
        try {
            if (userDao.existsByUsername(username)) {
                throw new IllegalArgumentException("用户名已存在");
            }
            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(PasswordUtil.sha256(password));
            user.setRealName(realName);
            user.setEmail(email);
            user.setRole(User.ROLE_USER);
            user.setAvatarPath("/images/default-avatar.svg");
            int id = userDao.insert(user);
            user.setId(id);
            return user;
        } catch (SQLException e) {
            throw new IllegalStateException("注册失败，请稍后重试", e);
        }
    }

    @Override
    public Optional<User> login(String username, String password) {
        if (StringUtil.isBlank(username) || StringUtil.isBlank(password)) {
            return Optional.empty();
        }
        try {
            Optional<User> user = userDao.findByUsername(username.trim());
            if (user.isPresent() && PasswordUtil.sha256(password).equals(user.get().getPasswordHash())) {
                return user;
            }
            return Optional.empty();
        } catch (SQLException e) {
            throw new IllegalStateException("登录失败，请稍后重试", e);
        }
    }

    @Override
    public boolean existsUsername(String username) {
        if (StringUtil.isBlank(username)) {
            return false;
        }
        try {
            return userDao.existsByUsername(username.trim());
        } catch (SQLException e) {
            throw new IllegalStateException("用户名检查失败", e);
        }
    }

    @Override
    public Optional<User> findById(int id) {
        try {
            return userDao.findById(id);
        } catch (SQLException e) {
            throw new IllegalStateException("用户查询失败", e);
        }
    }

    @Override
    public PageBean<User> list(String keyword, Integer role, int page, int pageSize, String sort, String direction) {
        try {
            return userDao.list(keyword, role, Math.max(page, 1), pageSize, sort, direction);
        } catch (SQLException e) {
            throw new IllegalStateException("用户列表查询失败", e);
        }
    }

    @Override
    public void updateProfile(User user) {
        validateProfile(user.getRealName(), user.getEmail());
        try {
            userDao.updateProfile(user);
        } catch (SQLException e) {
            throw new IllegalStateException("用户资料保存失败", e);
        }
    }

    @Override
    public void updateAvatar(int userId, String avatarPath) {
        if (StringUtil.isBlank(avatarPath)) {
            throw new IllegalArgumentException("头像路径不能为空");
        }
        try {
            userDao.updateAvatar(userId, avatarPath);
        } catch (SQLException e) {
            throw new IllegalStateException("头像保存失败", e);
        }
    }

    @Override
    public void deleteUserWithData(int userId) {
        try (Connection connection = DBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                for (Integer courseId : enrollmentDao.listCourseIdsByUserId(connection, userId)) {
                    courseDao.increaseCapacity(connection, courseId);
                }
                enrollmentDao.deleteByUserId(connection, userId);
                userDao.deleteById(connection, userId);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("删除用户失败", e);
        }
    }

    private void validateUser(String username, String password, String realName, String email) {
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("用户名必须为 3-20 位字母、数字或下划线");
        }
        if (StringUtil.isBlank(password) || password.length() < 6 || password.length() > 32) {
            throw new IllegalArgumentException("密码长度必须为 6-32 位");
        }
        validateProfile(realName, email);
    }

    private void validateProfile(String realName, String email) {
        if (StringUtil.isBlank(realName) || realName.trim().length() > 30) {
            throw new IllegalArgumentException("姓名不能为空，且不能超过 30 个字符");
        }
        if (StringUtil.isBlank(email) || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("邮箱格式不正确");
        }
    }
}
