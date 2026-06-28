package com.hnit.coursehub.util;

import com.hnit.coursehub.entity.EnrollmentFile;

import javax.servlet.ServletContext;
import javax.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class UploadUtil {
    private static final Set<String> IMAGE_EXTENSIONS = new HashSet<>(Arrays.asList("jpg", "jpeg", "png", "gif"));
    private static final Set<String> DOCUMENT_EXTENSIONS = new HashSet<>(Arrays.asList("pdf", "doc", "docx", "xls", "xlsx", "txt", "zip", "png", "jpg", "jpeg"));
    private static final long AVATAR_MAX_SIZE = 2 * 1024 * 1024;
    private static final long FILE_MAX_SIZE = 10 * 1024 * 1024;

    private UploadUtil() {
    }

    public static String saveAvatar(ServletContext context, Part part) throws IOException {
        validatePart(part, IMAGE_EXTENSIONS, AVATAR_MAX_SIZE);
        return savePart(context, part, "avatars");
    }

    public static String saveCourseFile(ServletContext context, Part part) throws IOException {
        validatePart(part, DOCUMENT_EXTENSIONS, FILE_MAX_SIZE);
        return savePart(context, part, "courses");
    }

    public static EnrollmentFile saveEnrollmentFile(ServletContext context, Part part) throws IOException {
        validatePart(part, DOCUMENT_EXTENSIONS, FILE_MAX_SIZE);
        String path = savePart(context, part, "enrollments");
        EnrollmentFile file = new EnrollmentFile();
        file.setOriginalName(getSubmittedFileName(part));
        file.setStoredName(new File(path).getName());
        file.setFilePath(path);
        file.setContentType(part.getContentType());
        file.setFileSize(part.getSize());
        return file;
    }

    public static boolean hasFile(Part part) {
        return part != null && part.getSize() > 0 && !StringUtil.isBlank(getSubmittedFileName(part));
    }

    private static void validatePart(Part part, Set<String> allowedExtensions, long maxSize) {
        if (!hasFile(part)) {
            throw new IllegalArgumentException("请选择要上传的文件");
        }
        if (part.getSize() > maxSize) {
            throw new IllegalArgumentException("文件大小不能超过 " + (maxSize / 1024 / 1024) + "MB");
        }
        String extension = getExtension(getSubmittedFileName(part));
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("不支持的文件类型：" + extension);
        }
    }

    private static String savePart(ServletContext context, Part part, String folder) throws IOException {
        String fileName = getSubmittedFileName(part);
        String extension = getExtension(fileName);
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        String relativeFolder = "/uploads/" + folder;
        String realFolder = context.getRealPath(relativeFolder);
        File directory = new File(realFolder);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IOException("无法创建上传目录：" + realFolder);
        }
        part.write(new File(directory, storedName).getAbsolutePath());
        return relativeFolder + "/" + storedName;
    }

    private static String getExtension(String fileName) {
        if (StringUtil.isBlank(fileName) || !fileName.contains(".")) {
            return "";
        }
        return fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
    }

    private static String getSubmittedFileName(Part part) {
        if (part == null) {
            return "";
        }
        String submitted = part.getSubmittedFileName();
        return submitted == null ? "" : Paths.get(submitted).getFileName().toString();
    }
}
