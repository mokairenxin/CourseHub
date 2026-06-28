package com.hnit.coursehub.controller;

import com.hnit.coursehub.entity.Enrollment;
import com.hnit.coursehub.entity.EnrollmentFile;
import com.hnit.coursehub.entity.User;
import com.hnit.coursehub.service.EnrollmentService;
import com.hnit.coursehub.service.impl.EnrollmentServiceImpl;
import com.hnit.coursehub.util.WebUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@WebServlet("/download")
public class DownloadServlet extends BaseServlet {
    private final EnrollmentService enrollmentService = new EnrollmentServiceImpl();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int fileId = WebUtil.getInt(request, "fileId", 0);
        Optional<EnrollmentFile> file = enrollmentService.findFileById(fileId);
        if (!file.isPresent()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "文件不存在");
            return;
        }
        Optional<Enrollment> enrollment = enrollmentService.findById(file.get().getEnrollmentId());
        if (!enrollment.isPresent() || !canDownload(request, enrollment.get())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "无权下载该文件");
            return;
        }
        File diskFile = new File(getServletContext().getRealPath(file.get().getFilePath()));
        if (!diskFile.exists() || !diskFile.isFile()) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "服务器文件不存在");
            return;
        }
        response.setContentType(file.get().getContentType() == null ? "application/octet-stream" : file.get().getContentType());
        String encodedName = URLEncoder.encode(file.get().getOriginalName(), StandardCharsets.UTF_8.name()).replace("+", "%20");
        response.setHeader("Content-Disposition", "attachment; filename*=UTF-8''" + encodedName);
        response.setContentLengthLong(diskFile.length());
        try (FileInputStream input = new FileInputStream(diskFile);
             OutputStream output = response.getOutputStream()) {
            byte[] buffer = new byte[8192];
            int len;
            while ((len = input.read(buffer)) != -1) {
                output.write(buffer, 0, len);
            }
        }
    }

    private boolean canDownload(HttpServletRequest request, Enrollment enrollment) {
        User user = loginUser(request);
        return user.isAdmin() || user.getId().equals(enrollment.getUserId());
    }
}
