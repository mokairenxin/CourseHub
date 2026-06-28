package com.hnit.coursehub.controller;

import com.hnit.coursehub.entity.User;
import com.hnit.coursehub.service.UserService;
import com.hnit.coursehub.service.impl.UserServiceImpl;
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

@WebServlet(urlPatterns = {"/users", "/admin/users"})
@MultipartConfig(maxFileSize = 2 * 1024 * 1024, maxRequestSize = 5 * 1024 * 1024)
public class UserServlet extends BaseServlet {
    private static final int PAGE_SIZE = 8;
    private final UserService userService = new UserServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        popFlash(request);
        if (request.getServletPath().startsWith("/admin")) {
            list(request, response);
            return;
        }
        forward(request, response, "profile.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (!ensureCsrf(request, response)) {
            return;
        }
        if (request.getServletPath().startsWith("/admin")) {
            if ("delete".equals(action(request))) {
                delete(request, response);
            } else {
                updateByAdmin(request, response);
            }
            return;
        }
        updateSelf(request, response);
    }

    private void list(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String keyword = request.getParameter("keyword");
        Integer role = null;
        if (!StringUtil.isBlank(request.getParameter("role"))) {
            role = WebUtil.getInt(request, "role", 0);
        }
        int page = WebUtil.getInt(request, "page", 1);
        String sort = WebUtil.getSortColumn(request.getParameter("sort"), "created_at", "username", "real_name", "role", "created_at");
        String direction = WebUtil.getSortDirection(request.getParameter("direction"));
        request.setAttribute("pageBean", userService.list(keyword, role, page, PAGE_SIZE, sort, direction));
        request.setAttribute("keyword", StringUtil.escapeHtml(StringUtil.trim(keyword)));
        request.setAttribute("role", role);
        request.setAttribute("sort", sort);
        request.setAttribute("direction", direction);
        forward(request, response, "admin/user-list.jsp");
    }

    private void updateSelf(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        User loginUser = loginUser(request);
        try {
            User user = new User();
            user.setId(loginUser.getId());
            user.setRealName(request.getParameter("realName"));
            user.setEmail(request.getParameter("email"));
            user.setRole(loginUser.getRole());
            userService.updateProfile(user);
            Part avatar = request.getPart("avatar");
            if (UploadUtil.hasFile(avatar)) {
                String avatarPath = UploadUtil.saveAvatar(getServletContext(), avatar);
                userService.updateAvatar(loginUser.getId(), avatarPath);
                loginUser.setAvatarPath(avatarPath);
            }
            loginUser.setRealName(StringUtil.escapeHtml(user.getRealName()));
            loginUser.setEmail(StringUtil.escapeHtml(user.getEmail()));
            setFlash(request, "success", "个人资料已保存");
        } catch (RuntimeException e) {
            setFlash(request, "danger", e.getMessage());
        }
        redirect(request, response, "/users");
    }

    private void updateByAdmin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            User user = new User();
            user.setId(WebUtil.getInt(request, "id", 0));
            user.setRealName(request.getParameter("realName"));
            user.setEmail(request.getParameter("email"));
            user.setRole(WebUtil.getInt(request, "role", User.ROLE_USER));
            userService.updateProfile(user);
            setFlash(request, "success", "用户资料已保存");
        } catch (RuntimeException e) {
            setFlash(request, "danger", e.getMessage());
        }
        redirect(request, response, "/admin/users");
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws IOException {
        int id = WebUtil.getInt(request, "id", 0);
        if (loginUser(request).getId().equals(id)) {
            setFlash(request, "danger", "不能删除当前登录账号");
            redirect(request, response, "/admin/users");
            return;
        }
        try {
            userService.deleteUserWithData(id);
            setFlash(request, "success", "用户及其关联数据已删除");
        } catch (RuntimeException e) {
            setFlash(request, "danger", e.getMessage());
        }
        redirect(request, response, "/admin/users");
    }
}
