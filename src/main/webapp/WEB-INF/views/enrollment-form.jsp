<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="新增选课"/>
<%@ include file="common/header.jspf" %>
<div class="page-head">
    <div>
        <p class="eyebrow">选课申请</p>
        <h1>新增选课</h1>
    </div>
    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/enrollments">返回列表</a>
</div>
<section class="panel">
    <form class="needs-live-validation" action="${pageContext.request.contextPath}/enrollments" method="post" enctype="multipart/form-data" novalidate>
        <input type="hidden" name="action" value="create">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <div class="mb-3">
            <label class="form-label" for="courseId">选择课程</label>
            <select class="form-select" id="courseId" name="courseId" required>
                <option value="">请选择</option>
                <c:forEach var="course" items="${courses}">
                    <option value="${course.id}">${course.courseName} / ${course.teacher} / 剩余 ${course.capacity}</option>
                </c:forEach>
            </select>
            <div class="form-text live-feedback">请选择一门课程</div>
        </div>
        <div class="mb-3">
            <label class="form-label" for="note">备注</label>
            <textarea class="form-control" id="note" name="note" rows="4" maxlength="500" placeholder="可填写选课说明"></textarea>
        </div>
        <div class="mb-3">
            <label class="form-label" for="attachments">附件（多文件上传）</label>
            <input class="form-control" id="attachments" name="attachments" type="file" multiple accept=".pdf,.doc,.docx,.xls,.xlsx,.txt,.zip,.jpg,.jpeg,.png">
            <div class="form-text">可同时选择多个附件，单个文件不超过 10MB</div>
        </div>
        <div class="form-actions">
            <button class="btn btn-primary" type="submit">提交选课</button>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/enrollments">取消</a>
        </div>
    </form>
</section>
<%@ include file="common/footer.jspf" %>
