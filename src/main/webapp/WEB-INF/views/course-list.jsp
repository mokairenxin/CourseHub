<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<c:set var="pageTitle" value="课程列表"/>
<%@ include file="common/header.jspf" %>
<div class="page-head">
    <div>
        <p class="eyebrow">课程管理</p>
        <h1>课程列表</h1>
    </div>
    <div class="head-actions">
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/enrollments?action=new">去选课</a>
        <c:if test="${sessionScope.loginUser.admin}">
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/courses?action=new">新增课程</a>
        </c:if>
    </div>
</div>
<section class="panel">
    <form class="row g-3 align-items-end mb-3" method="get" action="${pageContext.request.contextPath}/courses">
        <div class="col-md-5">
            <label class="form-label" for="keyword">关键词</label>
            <input class="form-control" id="keyword" name="keyword" value="${keyword}" placeholder="课程名、编号、教师">
        </div>
        <div class="col-md-3">
            <label class="form-label" for="category">分类</label>
            <input class="form-control" id="category" name="category" value="${category}" placeholder="如 计算机">
        </div>
        <div class="col-md-2">
            <button class="btn btn-primary w-100" type="submit">搜索</button>
        </div>
        <div class="col-md-2">
            <a class="btn btn-outline-secondary w-100" href="${pageContext.request.contextPath}/courses">重置</a>
        </div>
    </form>
    <c:set var="nextDir" value="${direction == 'asc' ? 'desc' : 'asc'}"/>
    <div class="table-responsive">
        <table class="table table-hover align-middle data-table">
            <thead>
            <tr>
                <th><a href="${pageContext.request.contextPath}/courses?keyword=${keyword}&category=${category}&sort=course_code&direction=${nextDir}">编号</a></th>
                <th><a href="${pageContext.request.contextPath}/courses?keyword=${keyword}&category=${category}&sort=course_name&direction=${nextDir}">课程</a></th>
                <th><a href="${pageContext.request.contextPath}/courses?keyword=${keyword}&category=${category}&sort=teacher&direction=${nextDir}">教师</a></th>
                <th><a href="${pageContext.request.contextPath}/courses?keyword=${keyword}&category=${category}&sort=category&direction=${nextDir}">分类</a></th>
                <th><a href="${pageContext.request.contextPath}/courses?keyword=${keyword}&category=${category}&sort=capacity&direction=${nextDir}">名额</a></th>
                <th>附件</th>
                <th class="text-end">操作</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="course" items="${pageBean.records}">
                <tr>
                    <td><span class="badge text-bg-light">${course.courseCode}</span></td>
                    <td>
                        <div class="course-cell">
                            <c:if test="${not empty course.coverPath}">
                                <img src="${pageContext.request.contextPath}${course.coverPath}" alt="课程图片">
                            </c:if>
                            <div>
                                <strong>${course.courseName}</strong>
                                <div class="small text-muted">${course.description}</div>
                            </div>
                        </div>
                    </td>
                    <td>${course.teacher}</td>
                    <td>${course.category}</td>
                    <td>${course.capacity}</td>
                    <td>
                        <c:if test="${not empty course.syllabusPath}">
                            <a href="${pageContext.request.contextPath}${course.syllabusPath}" target="_blank">查看课件</a>
                        </c:if>
                        <c:if test="${empty course.syllabusPath}">无</c:if>
                    </td>
                    <td class="text-end table-actions">
                        <c:if test="${sessionScope.loginUser.admin}">
                            <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/courses?action=edit&id=${course.id}">编辑</a>
                            <form class="d-inline confirm-form" action="${pageContext.request.contextPath}/courses" method="post" data-confirm="确定删除该课程及其选课记录？">
                                <input type="hidden" name="action" value="delete">
                                <input type="hidden" name="csrfToken" value="${csrfToken}">
                                <input type="hidden" name="id" value="${course.id}">
                                <button class="btn btn-sm btn-outline-danger" type="submit">删除</button>
                            </form>
                        </c:if>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty pageBean.records}">
                <tr><td colspan="7" class="empty-row">暂无课程</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>
    <c:set var="pageUrl" value="${pageContext.request.contextPath}/courses?keyword=${keyword}&category=${category}&sort=${sort}&direction=${direction}"/>
    <%@ include file="common/pager.jspf" %>
</section>
<%@ include file="common/footer.jspf" %>
