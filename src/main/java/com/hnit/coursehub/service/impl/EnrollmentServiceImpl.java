package com.hnit.coursehub.service.impl;

import com.hnit.coursehub.dao.CourseDao;
import com.hnit.coursehub.dao.EnrollmentDao;
import com.hnit.coursehub.dao.impl.CourseDaoImpl;
import com.hnit.coursehub.dao.impl.EnrollmentDaoImpl;
import com.hnit.coursehub.entity.Enrollment;
import com.hnit.coursehub.entity.EnrollmentFile;
import com.hnit.coursehub.entity.PageBean;
import com.hnit.coursehub.service.EnrollmentService;
import com.hnit.coursehub.util.DBUtil;
import com.hnit.coursehub.util.StringUtil;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class EnrollmentServiceImpl implements EnrollmentService {
    private final EnrollmentDao enrollmentDao = new EnrollmentDaoImpl();
    private final CourseDao courseDao = new CourseDaoImpl();

    @Override
    public Optional<Enrollment> findById(int id) {
        try {
            return enrollmentDao.findById(id);
        } catch (SQLException e) {
            throw new IllegalStateException("选课记录查询失败", e);
        }
    }

    @Override
    public PageBean<Enrollment> list(Integer userId, String keyword, String status, int page, int pageSize, String sort, String direction) {
        try {
            return enrollmentDao.list(userId, keyword, status, Math.max(page, 1), pageSize, sort, direction);
        } catch (SQLException e) {
            throw new IllegalStateException("选课记录列表查询失败", e);
        }
    }

    @Override
    public List<Enrollment> listForExport(Integer userId) {
        try {
            return enrollmentDao.listForExport(userId);
        } catch (SQLException e) {
            throw new IllegalStateException("导出数据查询失败", e);
        }
    }

    @Override
    public int enroll(int userId, int courseId, String note, List<EnrollmentFile> files) {
        if (courseId <= 0) {
            throw new IllegalArgumentException("请选择课程");
        }
        try (Connection connection = DBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                if (enrollmentDao.findByUserAndCourse(userId, courseId).isPresent()) {
                    throw new IllegalArgumentException("你已经选择过这门课程");
                }
                boolean decreased = courseDao.decreaseCapacityIfAvailable(connection, courseId);
                if (!decreased) {
                    throw new IllegalArgumentException("课程剩余名额不足");
                }
                Enrollment enrollment = new Enrollment();
                enrollment.setUserId(userId);
                enrollment.setCourseId(courseId);
                enrollment.setStatus("ENROLLED");
                enrollment.setNote(StringUtil.trim(note));
                int enrollmentId = enrollmentDao.insert(connection, enrollment);
                if (files != null) {
                    for (EnrollmentFile file : files) {
                        enrollmentDao.insertFile(connection, enrollmentId, file);
                    }
                }
                connection.commit();
                return enrollmentId;
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("选课失败", e);
        }
    }

    @Override
    public void update(Enrollment enrollment) {
        if (StringUtil.isBlank(enrollment.getStatus())) {
            throw new IllegalArgumentException("状态不能为空");
        }
        enrollment.setNote(StringUtil.trim(enrollment.getNote()));
        try {
            enrollmentDao.update(enrollment);
        } catch (SQLException e) {
            throw new IllegalStateException("选课记录保存失败", e);
        }
    }

    @Override
    public void drop(int enrollmentId) {
        try (Connection connection = DBUtil.getConnection()) {
            try {
                connection.setAutoCommit(false);
                Optional<Enrollment> enrollment = enrollmentDao.findById(enrollmentId);
                if (!enrollment.isPresent()) {
                    throw new IllegalArgumentException("选课记录不存在");
                }
                enrollmentDao.deleteById(connection, enrollmentId);
                courseDao.increaseCapacity(connection, enrollment.get().getCourseId());
                connection.commit();
            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new IllegalStateException("退课失败", e);
        }
    }

    @Override
    public Optional<EnrollmentFile> findFileById(int fileId) {
        try {
            return enrollmentDao.findFileById(fileId);
        } catch (SQLException e) {
            throw new IllegalStateException("文件查询失败", e);
        }
    }
}
