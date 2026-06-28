package com.hnit.coursehub.controller;

import com.hnit.coursehub.entity.User;
import com.hnit.coursehub.service.CourseService;
import com.hnit.coursehub.service.EnrollmentService;
import com.hnit.coursehub.service.UserService;
import com.hnit.coursehub.service.impl.CourseServiceImpl;
import com.hnit.coursehub.service.impl.EnrollmentServiceImpl;
import com.hnit.coursehub.service.impl.UserServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/dashboard")
public class DashboardServlet extends BaseServlet {
    private final CourseService courseService = new CourseServiceImpl();
    private final EnrollmentService enrollmentService = new EnrollmentServiceImpl();
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        popFlash(request);
        User user = loginUser(request);
        request.setAttribute("coursePage", courseService.list("", "", 1, 5, "created_at", "desc"));
        request.setAttribute("enrollmentPage", enrollmentService.list(user.isAdmin() ? null : user.getId(), "", "", 1, 5, "created_at", "desc"));
        if (user.isAdmin()) {
            request.setAttribute("userPage", userService.list("", null, 1, 5, "created_at", "desc"));
        }
        forward(request, response, "dashboard.jsp");
    }
}
