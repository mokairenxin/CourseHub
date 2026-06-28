<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="注册"/>
<%@ include file="common/header.jspf" %>
<section class="auth-shell">
    <div class="auth-panel">
        <div class="mb-4">
            <p class="eyebrow">新用户注册</p>
            <h1>创建学生账号</h1>
            <p class="text-muted mb-0">注册后可以选课、上传附件并管理个人数据。</p>
        </div>
        <form class="needs-live-validation" action="${pageContext.request.contextPath}/auth" method="post" novalidate>
            <input type="hidden" name="action" value="register">
            <input type="hidden" name="csrfToken" value="${csrfToken}">
            <div class="mb-3">
                <label class="form-label" for="username">用户名</label>
                <input class="form-control ajax-username" id="username" name="username" required pattern="[A-Za-z0-9_]{3,20}" data-check-url="${pageContext.request.contextPath}/auth?action=checkUsername">
                <div class="form-text live-feedback">输入后自动检查是否可用</div>
            </div>
            <div class="row g-3">
                <div class="col-md-6">
                    <label class="form-label" for="realName">姓名</label>
                    <input class="form-control" id="realName" name="realName" required maxlength="30">
                    <div class="form-text live-feedback">姓名不能为空</div>
                </div>
                <div class="col-md-6">
                    <label class="form-label" for="email">邮箱</label>
                    <input class="form-control" id="email" name="email" type="email" required>
                    <div class="form-text live-feedback">请输入有效邮箱</div>
                </div>
            </div>
            <div class="mt-3 mb-3">
                <label class="form-label" for="password">密码</label>
                <input class="form-control" id="password" name="password" type="password" required minlength="6" maxlength="32">
                <div class="form-text live-feedback">6-32 位密码</div>
            </div>
            <button class="btn btn-primary w-100" type="submit">注册</button>
        </form>
        <p class="mt-3 mb-0 text-center">已有账号？<a href="${pageContext.request.contextPath}/auth?action=login">返回登录</a></p>
    </div>
</section>
<%@ include file="common/footer.jspf" %>
