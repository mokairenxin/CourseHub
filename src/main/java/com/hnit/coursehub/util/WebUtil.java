package com.hnit.coursehub.util;

import javax.servlet.http.HttpServletRequest;

public final class WebUtil {
    private WebUtil() {
    }

    public static int getInt(HttpServletRequest request, String name, int defaultValue) {
        String value = request.getParameter(name);
        if (StringUtil.isBlank(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static String getSortColumn(String input, String defaultColumn, String... allowedColumns) {
        if (!StringUtil.isBlank(input)) {
            for (String allowed : allowedColumns) {
                if (allowed.equals(input)) {
                    return input;
                }
            }
        }
        return defaultColumn;
    }

    public static String getSortDirection(String input) {
        return "desc".equalsIgnoreCase(input) ? "desc" : "asc";
    }
}
