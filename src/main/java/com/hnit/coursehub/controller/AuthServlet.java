package com.hnit.coursehub.controller;

import com.hnit.coursehub.entity.User;
import com.hnit.coursehub.service.UserService;
import com.hnit.coursehub.service.impl.UserServiceImpl;
import com.hnit.coursehub.util.CsrfUtil;
import com.hnit.coursehub.util.StringUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;

@WebServlet("/auth")
public class AuthServlet extends BaseServlet {
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        popFlash(request);
        switch (action(request)) {
            case "register":
                forward(request, response, "register.jsp");
                break;
            case "logout":
                request.getSession().invalidate();
                redirect(request, response, "/auth?action=login");
                break;
            case "checkUsername":
                checkUsername(request, response);
                break;
            case "login":
            default:
                fillRememberedUsername(request);
                forward(request, response, "login.jsp");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!ensureCsrf(request, response)) {
            return;
        }
        switch (action(request)) {
            case "register":
                register(request, response);
                break;
            case "login":
            default:
                login(request, response);
                break;
        }
    }

    private void login(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        Optional<User> user = userService.login(username, password);
        if (user.isPresent()) {
            request.getSession().setAttribute("loginUser", user.get());
            CsrfUtil.ensureToken(request);
            Cookie cookie = new Cookie("rememberUsername", user.get().getUsername());
            cookie.setMaxAge(7 * 24 * 60 * 60);
            cookie.setPath(request.getContextPath().isEmpty() ? "/" : request.getContextPath());
            response.addCookie(cookie);
            redirect(request, response, user.get().isAdmin() ? "/dashboard" : "/courses");
            return;
        }
        setFlash(request, "danger", "用户名或密码错误");
        redirect(request, response, "/auth?action=login");
    }

    private void register(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            userService.register(
                    request.getParameter("username"),
                    request.getParameter("password"),
                    request.getParameter("realName"),
                    request.getParameter("email")
            );
            setFlash(request, "success", "注册成功，请登录");
            redirect(request, response, "/auth?action=login");
        } catch (RuntimeException e) {
            setFlash(request, "danger", e.getMessage());
            redirect(request, response, "/auth?action=register");
        }
    }

    private void checkUsername(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String username = StringUtil.trim(request.getParameter("username"));
        boolean exists = userService.existsUsername(username);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"exists\":" + exists + ",\"valid\":" + username.matches("^[A-Za-z0-9_]{3,20}$") + "}");
    }

    private void fillRememberedUsername(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return;
        }
        for (Cookie cookie : cookies) {
            if ("rememberUsername".equals(cookie.getName())) {
                request.setAttribute("rememberUsername", cookie.getValue());
                return;
            }
        }
    }
}
