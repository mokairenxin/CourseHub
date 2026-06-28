package com.hnit.coursehub.service.impl;

import com.hnit.coursehub.dao.CourseDao;
import com.hnit.coursehub.dao.EnrollmentDao;
import com.hnit.coursehub.dao.impl.CourseDaoImpl;
import com.hnit.coursehub.dao.impl.EnrollmentDaoImpl;
import com.hnit.coursehub.entity.Course;
import com.hnit.coursehub.entity.PageBean;
import com.hnit.coursehub.service.CourseService;
import com.hnit.coursehub.util.DBUtil;
import com.hnit.coursehub.util.StringUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

public class CourseServiceImpl implements CourseService {
    private static final Pattern CODE_PATTERN = Pattern.compile("^[A-Za-z0-9-]{3,20}$");

    private final CourseDao courseDao = new CourseDaoImpl();
    private final EnrollmentDao enrollmentDao = new EnrollmentDaoImpl();

    @Override
    public Optional<Course> findById(int id) {
        try {
            return courseDao.findById(id);
        } catch (SQLException e) {
            throw new IllegalStateException("课程查询失败", e);
        }
    }

    @Override
    public PageBean<Course> list(String keyword, String category, int page, int pageSize, String sort, String direction) {
        try {
            return courseDao.list(keyword, category, Math.max(page, 1), pageSize, sort, direction);
        } catch (SQLException e) {
            throw new IllegalStateException("课程列表查询失败", e);
        }
    }

    @Override
    public List<Course> listAllOpen() {
        try {
            return courseDao.listAllOpen();
        } catch (SQLException e) {
            throw new IllegalStateException("课程下拉列表查询失败", e);
        }
    }

    @Override
    public int create(Course course) {
        normalizeAndValidate(course);
        try {
            return courseDao.insert(course);
        } catch (SQLException e) {
            throw new IllegalStateException("课程创建失败", e);
        }
    }

    @Override
    public void update(Course course) {
        normalizeAndValidate(course);
        try {
            courseDao.update(course);
        } catch (SQLException e) {
            throw new IllegalStateException("课程保存失败", e);
        }
    }

    @Override
    public void deleteWithEnrollments(int courseId) {
        try (Connection connection = DBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                enrollmentDao.deleteByCourseId(connection, courseId);
                courseDao.deleteById(connection, courseId);
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("删除课程失败", e);
        }
    }

    private void normalizeAndValidate(Course course) {
        course.setCourseCode(StringUtil.trim(course.getCourseCode()));
        course.setCourseName(StringUtil.trim(course.getCourseName()));
        course.setTeacher(StringUtil.trim(course.getTeacher()));
        course.setCategory(StringUtil.trim(course.getCategory()));
        course.setDescription(StringUtil.trim(course.getDescription()));
        if (!CODE_PATTERN.matcher(course.getCourseCode()).matches()) {
            throw new IllegalArgumentException("课程编号必须为 3-20 位字母、数字或短横线");
        }
        if (StringUtil.isBlank(course.getCourseName()) || course.getCourseName().length() > 80) {
            throw new IllegalArgumentException("课程名称不能为空，且不能超过 80 个字符");
        }
        if (StringUtil.isBlank(course.getTeacher()) || course.getTeacher().length() > 30) {
            throw new IllegalArgumentException("授课教师不能为空，且不能超过 30 个字符");
        }
        if (StringUtil.isBlank(course.getCategory()) || course.getCategory().length() > 30) {
            throw new IllegalArgumentException("课程分类不能为空，且不能超过 30 个字符");
        }
        if (course.getCapacity() == null || course.getCapacity() < 0 || course.getCapacity() > 999) {
            throw new IllegalArgumentException("剩余名额必须为 0-999");
        }
    }
}
