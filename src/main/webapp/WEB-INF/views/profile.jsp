<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="个人资料"/>
<%@ include file="common/header.jspf" %>
<div class="page-head">
    <div>
        <p class="eyebrow">Session 用户</p>
        <h1>个人资料</h1>
    </div>
    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/dashboard">返回仪表盘</a>
</div>
<section class="panel profile-panel">
    <div class="profile-aside">
        <img class="avatar-lg" src="${pageContext.request.contextPath}${sessionScope.loginUser.avatarPath}" alt="头像">
        <strong>${sessionScope.loginUser.username}</strong>
        <span>${sessionScope.loginUser.admin ? '管理员' : '普通用户'}</span>
    </div>
    <form class="needs-live-validation flex-fill" action="${pageContext.request.contextPath}/users" method="post" enctype="multipart/form-data" novalidate>
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <div class="row g-3">
            <div class="col-md-6">
                <label class="form-label" for="realName">姓名</label>
                <input class="form-control" id="realName" name="realName" value="${sessionScope.loginUser.realName}" required maxlength="30">
                <div class="form-text live-feedback">姓名不能为空</div>
            </div>
            <div class="col-md-6">
                <label class="form-label" for="email">邮箱</label>
                <input class="form-control" id="email" name="email" type="email" value="${sessionScope.loginUser.email}" required>
                <div class="form-text live-feedback">请输入有效邮箱</div>
            </div>
            <div class="col-12">
                <label class="form-label" for="avatar">头像上传</label>
                <input class="form-control" id="avatar" name="avatar" type="file" accept=".jpg,.jpeg,.png,.gif">
                <div class="form-text">支持 jpg/png/gif，最大 2MB</div>
            </div>
        </div>
        <div class="form-actions">
            <button class="btn btn-primary" type="submit">保存资料</button>
        </div>
    </form>
</section>
<%@ include file="common/footer.jspf" %>
