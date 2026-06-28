<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="编辑选课"/>
<%@ include file="common/header.jspf" %>
<div class="page-head">
    <div>
        <p class="eyebrow">选课记录</p>
        <h1>编辑 ${enrollment.courseName}</h1>
    </div>
    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/enrollments">返回列表</a>
</div>
<section class="panel">
    <form class="needs-live-validation" action="${pageContext.request.contextPath}/enrollments" method="post" novalidate>
        <input type="hidden" name="action" value="update">
        <input type="hidden" name="csrfToken" value="${csrfToken}">
        <input type="hidden" name="id" value="${enrollment.id}">
        <div class="row g-3">
            <div class="col-md-6">
                <label class="form-label">学生</label>
                <input class="form-control" value="${enrollment.realName} (${enrollment.username})" disabled>
            </div>
            <div class="col-md-6">
                <label class="form-label">课程</label>
                <input class="form-control" value="${enrollment.courseName}" disabled>
            </div>
            <c:if test="${sessionScope.loginUser.admin}">
                <div class="col-md-4">
                    <label class="form-label" for="status">状态</label>
                    <select class="form-select" id="status" name="status">
                        <option value="ENROLLED" ${enrollment.status == 'ENROLLED' ? 'selected' : ''}>已选</option>
                        <option value="APPROVED" ${enrollment.status == 'APPROVED' ? 'selected' : ''}>已通过</option>
                        <option value="REJECTED" ${enrollment.status == 'REJECTED' ? 'selected' : ''}>已驳回</option>
                    </select>
                </div>
            </c:if>
            <div class="col-12">
                <label class="form-label" for="note">备注</label>
                <textarea class="form-control" id="note" name="note" rows="4" maxlength="500">${enrollment.note}</textarea>
            </div>
        </div>
        <div class="form-actions">
            <button class="btn btn-primary" type="submit">保存</button>
            <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/enrollments">取消</a>
        </div>
    </form>
</section>
<%@ include file="common/footer.jspf" %>
