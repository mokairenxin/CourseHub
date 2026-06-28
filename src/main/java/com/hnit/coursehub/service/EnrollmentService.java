package com.hnit.coursehub.service;

import com.hnit.coursehub.entity.Enrollment;
import com.hnit.coursehub.entity.EnrollmentFile;
import com.hnit.coursehub.entity.PageBean;

import java.util.List;
import java.util.Optional;

public interface EnrollmentService {
    Optional<Enrollment> findById(int id);

    PageBean<Enrollment> list(Integer userId, String keyword, String status, int page, int pageSize, String sort, String direction);

    List<Enrollment> listForExport(Integer userId);

    int enroll(int userId, int courseId, String note, List<EnrollmentFile> files);

    void update(Enrollment enrollment);

    void drop(int enrollmentId);

    Optional<EnrollmentFile> findFileById(int fileId);
}
