<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="${empty course ? '新增课程' : '编辑课程'}"/>
<%@ include file="common/header.jspf" %>
<div class="page-head">
    <div>
        <p class="eyebrow">课程表单</p>
        <h1>${empty course ? '新增课程' : '编辑课程'}</h1>
    </div>
    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/courses">返回列表</a>
</div>
<section class="panel">
    <form class="needs-live-validation" action="${pageContext.request.contextPath}/courses" method="post" enctype="multipart/form-data" novalidate>
        <input type="hidden" name="action" value="save">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="id" value="${course.id}">
        <input type="hidden" name="oldSyllabusPath" value="${course.syllabusPath}">
        <input type="hidden" name="oldCoverPath" value="${course.coverPath}">
        <div class="row g-3">
            <div class="col-md-4">
                <label class="form-label" for="courseCode">课程编号</label>
                <input class="form-control" id="courseCode" name="courseCode" value="${course.courseCode}" required pattern="[A-Za-z0-9-]{3,20}">
                <div class="form-text live-feedback">3-20 位字母、数字或短横线</div>
            </div>
            <div class="col-md-8">
                <label class="form-label" for="courseName">课程名称</label>
                <input class="form-control" id="courseName" name="courseName" value="${course.courseName}" required maxlength="80">
                <div class="form-text live-feedback">课程名称不能为空</div>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="teacher">授课教师</label>
                <input class="form-control" id="teacher" name="teacher" value="${course.teacher}" required maxlength="30">
                <div class="form-text live-feedback">教师不能为空</div>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="category">分类</label>
                <input class="form-control" id="category" name="category" value="${course.category}" required maxlength="30">
                <div class="form-text live-feedback">分类不能为空</div>
            </div>
            <div class="col-md-4">
                <label class="form-label" for="capacity">剩余名额</label>
                <input class="form-control" id="capacity" name="capacity" type="number" min="0" max="999" value="${empty course ? 30 : course.capacity}" required>
                <div class="form-text live-feedback">0-999</div>
            </div>
            <div class="col-md-6">
                <label class="form-label" for="coverFile">课程图片（单文件上传）</label>
                <input class="form-control" id="coverFile" name="coverFile" type="file" accept=".jpg,.jpeg,.png,.gif">
            </div>
            <div class="col-md-6">
                <label class="form-label" for="syllabusFile">课程附件（单文件上传）</label>
                <input class="form-control" id="syllabusFile" name="syllabusFile" type="file" accept=".pdf,.doc,.docx,.xls,.xlsx,.txt,.zip">
            </div>
            <div class="col-12">
                <label class="form-label" for="description">课程简介</label>
                <textarea class="form-control" id="description" name="description" rows="4" maxlength="500">${course.description}</textarea>
            </div>
        </div>
        <div class="form-actions">
            <button class="btn btn-primary" type="submit">保存课程</button>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/courses">取消</a>
        </div>
    </form>
</section>
<%@ include file="common/footer.jspf" %>
