CREATE DATABASE IF NOT EXISTS coursehub
  DEFAULT CHARACTER SET utf8mb4
  DEFAULT COLLATE utf8mb4_unicode_ci;

USE coursehub;

DROP TABLE IF EXISTS enrollment_files;
DROP TABLE IF EXISTS enrollments;
DROP TABLE IF EXISTS courses;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id INT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(20) NOT NULL UNIQUE,
    password_hash CHAR(64) NOT NULL,
    real_name VARCHAR(30) NOT NULL,
    email VARCHAR(120) NOT NULL,
    role TINYINT NOT NULL DEFAULT 0 COMMENT '0=普通用户,1=管理员',
    avatar_path VARCHAR(255) DEFAULT '/images/default-avatar.svg',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE courses (
    id INT PRIMARY KEY AUTO_INCREMENT,
    course_code VARCHAR(20) NOT NULL UNIQUE,
    course_name VARCHAR(80) NOT NULL,
    teacher VARCHAR(30) NOT NULL,
    category VARCHAR(30) NOT NULL,
    capacity INT NOT NULL DEFAULT 0,
    enrolled_count INT NOT NULL DEFAULT 0,
    syllabus_path VARCHAR(255),
    cover_path VARCHAR(255),
    description VARCHAR(500),
    created_by INT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_courses_created_by FOREIGN KEY (created_by) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE enrollments (
    id INT PRIMARY KEY AUTO_INCREMENT,
    user_id INT NOT NULL,
    course_id INT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ENROLLED',
    note VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_user_course UNIQUE (user_id, course_id),
    CONSTRAINT fk_enrollments_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_enrollments_course FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE enrollment_files (
    id INT PRIMARY KEY AUTO_INCREMENT,
    enrollment_id INT NOT NULL,
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(255) NOT NULL,
    content_type VARCHAR(120),
    file_size BIGINT NOT NULL DEFAULT 0,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_files_enrollment FOREIGN KEY (enrollment_id) REFERENCES enrollments(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO users(username, password_hash, real_name, email, role, avatar_path) VALUES
('admin', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '系统管理员', 'admin@example.com', 1, '/images/default-avatar.svg'),
('student', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '张同学', 'student@example.com', 0, '/images/default-avatar.svg'),
('lisi', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', '李四', 'lisi@example.com', 0, '/images/default-avatar.svg');

INSERT INTO courses(course_code, course_name, teacher, category, capacity, enrolled_count, description, created_by) VALUES
('JAVA-101', 'JavaWeb 开发技术', '王老师', '计算机', 39, 1, 'Servlet、JSP、JDBC 与 MVC 分层实践课程。', 1),
('DB-201', '数据库系统应用', '刘老师', '计算机', 30, 0, '学习 MySQL 表设计、事务和索引。', 1),
('UI-120', 'Web 前端基础', '陈老师', '设计', 24, 1, 'HTML、CSS、JavaScript 与响应式页面。', 1),
('ENG-210', '实用英语写作', '赵老师', '通识', 45, 0, '面向课程论文和项目文档的写作训练。', 1);

INSERT INTO enrollments(user_id, course_id, status, note) VALUES
(2, 1, 'ENROLLED', '希望加强 JavaWeb 实战能力。'),
(3, 3, 'APPROVED', '已完成前置课程。');
