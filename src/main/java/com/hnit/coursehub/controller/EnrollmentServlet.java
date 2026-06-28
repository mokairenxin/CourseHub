package com.hnit.coursehub.controller;

import com.hnit.coursehub.entity.Enrollment;
import com.hnit.coursehub.entity.EnrollmentFile;
import com.hnit.coursehub.entity.User;
import com.hnit.coursehub.service.CourseService;
import com.hnit.coursehub.service.EnrollmentService;
import com.hnit.coursehub.service.impl.CourseServiceImpl;
import com.hnit.coursehub.service.impl.EnrollmentServiceImpl;
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
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@WebServlet("/enrollments")
@MultipartConfig(maxFileSize = 10 * 1024 * 1024, maxRequestSize = 50 * 1024 * 1024)
public class EnrollmentServlet extends BaseServlet {
    private static final int PAGE_SIZE = 8;
    private final EnrollmentService enrollmentService = new EnrollmentServiceImpl();
    private final CourseService courseService = new CourseServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        popFlash(request);
        switch (action(request)) {
            case "new":
                request.setAttribute("courses", courseService.listAllOpen());
                forward(request, response, "enrollment-form.jsp");
                break;
            case "edit":
                edit(request, response);
                break;
            case "export":
                exportCsv(request, response);
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
        switch (action(request)) {
            case "drop":
                drop(request, response);
                break;
            case "update":
                update(request, response);
                break;
            case "create":
            default:
                create(request, response);
                break;
        }
    }

    private void list(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        User user = loginUser(request);
        Integer userId = user.isAdmin() ? null : user.getId();
        String keyword = request.getParameter("keyword");
        String status = request.getParameter("status");
        int page = WebUtil.getInt(request, "page", 1);
        String sort = WebUtil.getSortColumn(request.getParameter("sort"), "created_at", "created_at", "username", "course_name", "status");
        String direction = WebUtil.getSortDirection(request.getParameter("direction"));
        request.setAttribute("pageBean", enrollmentService.list(userId, keyword, status, page, PAGE_SIZE, sort, direction));
        request.setAttribute("keyword", StringUtil.escapeHtml(StringUtil.trim(keyword)));
        request.setAttribute("status", StringUtil.escapeHtml(StringUtil.trim(status)));
        request.setAttribute("sort", sort);
        request.setAttribute("direction", direction);
        forward(request, response, "enrollment-list.jsp");
    }

    private void edit(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int id = WebUtil.getInt(request, "id", 0);
        Optional<Enrollment> enrollment = enrollmentService.findById(id);
        if (!enrollment.isPresent() || !canOperate(request, enrollment.get())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "无权访问该记录");
            return;
        }
        request.setAttribute("enrollment", enrollment.get());
        forward(request, response, "enrollment-edit.jsp");
    }

    private void create(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        try {
            List<EnrollmentFile> files = new ArrayList<>();
            for (Part part : request.getParts()) {
                if ("attachments".equals(part.getName()) && UploadUtil.hasFile(part)) {
                    files.add(UploadUtil.saveEnrollmentFile(getServletContext(), part));
                }
            }
            int courseId = WebUtil.getInt(request, "courseId", 0);
            enrollmentService.enroll(loginUser(request).getId(), courseId, request.getParameter("note"), files);
            setFlash(request, "success", "选课成功");
            redirect(request, response, "/enrollments");
        } catch (RuntimeException e) {
            setFlash(request, "danger", e.getMessage());
            redirect(request, response, "/enrollments?action=new");
        }
    }

    private void update(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int id = WebUtil.getInt(request, "id", 0);
            Optional<Enrollment> old = enrollmentService.findById(id);
            if (!old.isPresent() || !canOperate(request, old.get())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "无权修改该记录");
                return;
            }
            Enrollment enrollment = new Enrollment();
            enrollment.setId(id);
            enrollment.setStatus(loginUser(request).isAdmin() ? request.getParameter("status") : old.get().getStatus());
            enrollment.setNote(request.getParameter("note"));
            enrollmentService.update(enrollment);
            setFlash(request, "success", "选课记录已保存");
        } catch (RuntimeException e) {
            setFlash(request, "danger", e.getMessage());
        }
        redirect(request, response, "/enrollments");
    }

    private void drop(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            int id = WebUtil.getInt(request, "id", 0);
            Optional<Enrollment> old = enrollmentService.findById(id);
            if (!old.isPresent() || !canOperate(request, old.get())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "无权删除该记录");
                return;
            }
            enrollmentService.drop(id);
            setFlash(request, "success", "退课成功，课程名额已恢复");
        } catch (RuntimeException e) {
            setFlash(request, "danger", e.getMessage());
        }
        redirect(request, response, "/enrollments");
    }

    private void exportCsv(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = loginUser(request);
        Integer userId = user.isAdmin() ? null : user.getId();
        List<Enrollment> rows = enrollmentService.listForExport(userId);
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/csv;charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"enrollments.csv\"");
        response.getOutputStream().write(new byte[]{(byte) 0xEF, (byte) 0xBB, (byte) 0xBF});
        StringBuilder csv = new StringBuilder();
        csv.append("学生账号,学生姓名,课程编号,课程名称,教师,分类,状态,备注,创建时间\n");
        for (Enrollment row : rows) {
            csv.append(csv(row.getUsername())).append(',')
                    .append(csv(row.getRealName())).append(',')
                    .append(csv(row.getCourseCode())).append(',')
                    .append(csv(row.getCourseName())).append(',')
                    .append(csv(row.getTeacher())).append(',')
                    .append(csv(row.getCategory())).append(',')
                    .append(csv(row.getStatus())).append(',')
                    .append(csv(row.getNote())).append(',')
                    .append(csv(String.valueOf(row.getCreatedAt()))).append('\n');
        }
        response.getOutputStream().write(csv.toString().getBytes(StandardCharsets.UTF_8));
    }

    private boolean canOperate(HttpServletRequest request, Enrollment enrollment) {
        User user = loginUser(request);
        return user.isAdmin() || user.getId().equals(enrollment.getUserId());
    }

    private String csv(String value) {
        String safe = value == null ? "" : value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }
}
