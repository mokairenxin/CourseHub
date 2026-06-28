package com.hnit.coursehub.controller;

import com.hnit.coursehub.entity.Course;
import com.hnit.coursehub.entity.User;
import com.hnit.coursehub.service.CourseService;
import com.hnit.coursehub.service.impl.CourseServiceImpl;
import com.hnit.coursehub.util.StringUtil;
import com.hnit.coursehub.util.UploadUtil;
import com.hnit.coursehub.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/courses")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 30 * 1024 * 1024)
public class CourseServlet extends BaseServlet {
    private static final int PAGE_SIZE = 8;
    private final CourseService courseService = new CourseServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        popFlash(request);
        switch (action(request)) {
            case "new":
                if (!requireAdmin(request, response)) {
                    return;
                }
                forward(request, response, "course-form.jsp");
                break;
            case "edit":
                if (!requireAdmin(request, response)) {
                    return;
                }
                edit(request, response);
                break;
            default:
                list(request, response);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!ensureCsrf(request, response)) {
            return;
        }
        if (!requireAdmin(request, response)) {
            return;
        }
        switch (action(request)) {
            case "delete":
                delete(request, response);
                break;
            case "save":
            default:
                save(request, response);
                break;
        }
    }

    private void list(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        String category = request.getParameter("category");
        int page = WebUtil.getInt(request, "page", 1);
        String sort = WebUtil.getSortColumn(request.getParameter("sort"), "created_at", "course_name", "course_code", "teacher", "category", "capacity", "created_at");
        String direction = WebUtil.getSortDirection(request.getParameter("direction"));
        request.setAttribute("pageBean", courseService.list(keyword, category, page, PAGE_SIZE, sort, direction));
        request.setAttribute("keyword", StringUtil.escapeHtml(StringUtil.trim(keyword)));
        request.setAttribute("category", StringUtil.escapeHtml(StringUtil.trim(category)));
        request.setAttribute("sort", sort);
        request.setAttribute("direction", direction);
        forward(request, response, "course-list.jsp");
    }

    private void edit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id = WebUtil.getInt(request, "id", 0);
        Optional<Course> course = courseService.findById(id);
        if (!course.isPresent()) {
            setFlash(request, "danger", "课程不存在");
            redirect(request, response, "/courses");
            return;
        }
        request.setAttribute("course", course.get());
        forward(request, response, "course-form.jsp");
    }

    private void save(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            Course course = buildCourse(request);
            Part syllabus = request.getPart("syllabusFile");
            if (UploadUtil.hasFile(syllabus)) {
                course.setSyllabusPath(UploadUtil.saveCourseFile(getServletContext(), syllabus));
            }
            Part cover = request.getPart("coverFile");
            if (UploadUtil.hasFile(cover)) {
                course.setCoverPath(UploadUtil.saveCourseFile(getServletContext(), cover));
            }
            int id = WebUtil.getInt(request, "id", 0);
            if (id > 0) {
                course.setId(id);
                if (StringUtil.isBlank(course.getSyllabusPath())) {
                    course.setSyllabusPath(request.getParameter("oldSyllabusPath"));
                }
                if (StringUtil.isBlank(course.getCoverPath())) {
                    course.setCoverPath(request.getParameter("oldCoverPath"));
                }
                courseService.update(course);
                setFlash(request, "success", "课程已保存");
            } else {
                course.setCreatedBy(loginUser(request).getId());
                courseService.create(course);
                setFlash(request, "success", "课程已创建");
            }
            redirect(request, response, "/courses");
        } catch (RuntimeException e) {
            setFlash(request, "danger", e.getMessage());
            redirect(request, response, "/courses");
        }
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int id = WebUtil.getInt(request, "id", 0);
            courseService.deleteWithEnrollments(id);
            setFlash(request, "success", "课程及关联选课记录已删除");
        } catch (RuntimeException e) {
            setFlash(request, "danger", e.getMessage());
        }
        redirect(request, response, "/courses");
    }

    private Course buildCourse(HttpServletRequest request) {
        Course course = new Course();
        course.setCourseCode(request.getParameter("courseCode"));
        course.setCourseName(request.getParameter("courseName"));
        course.setTeacher(request.getParameter("teacher"));
        course.setCategory(request.getParameter("category"));
        course.setCapacity(WebUtil.getInt(request, "capacity", -1));
        course.setDescription(request.getParameter("description"));
        return course;
    }
}
