package com.hnit.coursehub.controller;

import com.hnit.coursehub.entity.User;
import com.hnit.coursehub.util.CsrfUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {
    protected String action(HttpServletRequest request) {
        String action = request.getParameter("action");
        return action == null ? "" : action;
    }

    protected User loginUser(HttpServletRequest request) {
        return (User) request.getSession().getAttribute("loginUser");
    }

    protected void forward(HttpServletRequest request, HttpServletResponse response, String jsp) throws ServletException, IOException {
        CsrfUtil.ensureToken(request);
        request.getRequestDispatcher("/WEB-INF/views/" + jsp).forward(request, response);
    }

    protected void redirect(HttpServletRequest request, HttpServletResponse response, String path) throws IOException {
        response.sendRedirect(request.getContextPath() + path);
    }

    protected void setFlash(HttpServletRequest request, String type, String message) {
        request.getSession().setAttribute("flashType", type);
        request.getSession().setAttribute("flashMessage", message);
    }

    protected void popFlash(HttpServletRequest request) {
        Object type = request.getSession().getAttribute("flashType");
        Object message = request.getSession().getAttribute("flashMessage");
        if (type != null && message != null) {
            request.setAttribute("flashType", type);
            request.setAttribute("flashMessage", message);
            request.getSession().removeAttribute("flashType");
            request.getSession().removeAttribute("flashMessage");
        }
    }

    protected boolean ensureCsrf(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!CsrfUtil.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "CSRF Token 校验失败");
            return false;
        }
        return true;
    }

    protected void handleException(HttpServletRequest request, HttpServletResponse response, Exception e, String fallback) throws IOException {
        setFlash(request, "danger", e.getMessage());
        redirect(request, response, fallback);
    }

    protected boolean requireAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        User user = loginUser(request);
        if (user == null || !user.isAdmin()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "需要管理员权限");
            return false;
        }
        return true;
    }
}
