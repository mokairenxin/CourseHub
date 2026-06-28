package com.hnit.coursehub.filter;

import com.hnit.coursehub.entity.User;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = {"/dashboard", "/courses", "/enrollments", "/users", "/download"})
public class AuthFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        User loginUser = session == null ? null : (User) session.getAttribute("loginUser");
        if (loginUser == null) {
            String target = httpRequest.getRequestURI();
            String query = httpRequest.getQueryString();
            if (query != null) {
                target += "?" + query;
            }
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/auth?action=login&redirect=" + httpResponse.encodeURL(target));
            return;
        }
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
    }
}
