<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="登录"/>
<%@ include file="common/header.jspf" %>
<section class="auth-shell">
    <div class="auth-panel">
        <div class="mb-4">
            <p class="eyebrow">JavaWeb 课程项目</p>
            <h1>登录 CourseHub</h1>
            <p class="text-muted mb-0">进入课程、选课和后台管理工作台。</p>
        </div>
        <form class="needs-live-validation" action="${pageContext.request.contextPath}/auth" method="post" novalidate>
            <input type="hidden" name="action" value="login">
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <div class="mb-3">
                <label class="form-label" for="username">用户名</label>
                <input class="form-control" id="username" name="username" value="${rememberUsername}" required pattern="[A-Za-z0-9_]{3,20}">
                <div class="form-text live-feedback">3-20 位字母、数字或下划线</div>
            </div>
            <div class="mb-3">
                <label class="form-label" for="password">密码</label>
                <input class="form-control" id="password" name="password" type="password" required minlength="6" maxlength="32">
                <div class="form-text live-feedback">请输入 6-32 位密码</div>
            </div>
            <button class="btn btn-primary w-100" type="submit">登录</button>
        </form>
        <p class="mt-3 mb-0 text-center">没有账号？<a href="${pageContext.request.contextPath}/auth?action=register">立即注册</a></p>
        <div class="demo-account mt-4">
            <strong>测试账号</strong>
            <span>管理员：admin / 123456</span>
            <span>学生：student / 123456</span>
        </div>
    </div>
</section>
<%@ include file="common/footer.jspf" %>
