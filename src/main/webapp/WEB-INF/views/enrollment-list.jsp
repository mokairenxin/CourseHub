<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="选课记录"/>
<%@ include file="common/header.jspf" %>
<div class="page-head">
    <div>
        <p class="eyebrow">JOIN 查询</p>
        <h1>选课记录</h1>
    </div>
    <div class="head-actions">
        <a class="btn btn-outline-success" href="${pageContext.request.contextPath}/enrollments?action=export">导出 CSV</a>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/enrollments?action=new">新增选课</a>
    </div>
</div>
<section class="panel">
    <form class="row g-3 align-items-end mb-3" method="get" action="${pageContext.request.contextPath}/enrollments">
        <div class="col-md-5">
            <label class="form-label" for="keyword">关键词</label>
            <input class="form-control" id="keyword" name="keyword" value="${keyword}" placeholder="学生、课程、编号">
        </div>
        <div class="col-md-3">
            <label class="form-label" for="status">状态</label>
            <select class="form-select" id="status" name="status">
                <option value="">全部</option>
                <option value="ENROLLED" ${status == 'ENROLLED' ? 'selected' : ''}>已选</option>
                <option value="APPROVED" ${status == 'APPROVED' ? 'selected' : ''}>已通过</option>
                <option value="REJECTED" ${status == 'REJECTED' ? 'selected' : ''}>已驳回</option>
            </select>
        </div>
        <div class="col-md-2"><button class="btn btn-primary w-100" type="submit">搜索</button></div>
        <div class="col-md-2"><a class="btn btn-outline-secondary w-100" href="${pageContext.request.contextPath}/enrollments">重置</a></div>
    </form>
    <c:set var="nextDir" value="${direction == 'asc' ? 'desc' : 'asc'}"/>
    <div class="table-responsive">
        <table class="table table-hover align-middle data-table">
            <thead>
            <tr>
                <th><a href="${pageContext.request.contextPath}/enrollments?keyword=${keyword}&status=${status}&sort=username&direction=${nextDir}">学生</a></th>
                <th><a href="${pageContext.request.contextPath}/enrollments?keyword=${keyword}&status=${status}&sort=course_name&direction=${nextDir}">课程</a></th>
                <th>教师</th>
                <th><a href="${pageContext.request.contextPath}/enrollments?keyword=${keyword}&status=${status}&sort=status&direction=${nextDir}">状态</a></th>
                <th>附件</th>
                <th><a href="${pageContext.request.contextPath}/enrollments?keyword=${keyword}&status=${status}&sort=created_at&direction=${nextDir}">创建时间</a></th>
                <th class="text-end">操作</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="item" items="${pageBean.records}">
                <tr>
                    <td>${item.realName}<div class="small text-muted">${item.username}</div></td>
                    <td>${item.courseName}<div class="small text-muted">${item.courseCode} / ${item.category}</div></td>
                    <td>${item.teacher}</td>
                    <td><span class="badge text-bg-info">${item.status}</span></td>
                    <td>
                        <c:forEach var="file" items="${item.files}">
                            <a class="file-link" href="${pageContext.request.contextPath}/download?fileId=${file.id}">${file.originalName}</a>
                        </c:forEach>
                        <c:if test="${empty item.files}">无</c:if>
                    </td>
                    <td>${item.createdAt}</td>
                    <td class="text-end table-actions">
                        <a class="btn btn-sm btn-outline-primary" href="${pageContext.request.contextPath}/enrollments?action=edit&id=${item.id}">编辑</a>
                        <form class="d-inline confirm-form" action="${pageContext.request.contextPath}/enrollments" method="post" data-confirm="确定退选这门课程？">
                            <input type="hidden" name="action" value="drop">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <input type="hidden" name="id" value="${item.id}">
                            <button class="btn btn-sm btn-outline-danger" type="submit">退课</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty pageBean.records}">
                <tr><td colspan="7" class="empty-row">暂无选课记录</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>
    <c:set var="pageUrl" value="${pageContext.request.contextPath}/enrollments?keyword=${keyword}&status=${status}&sort=${sort}&direction=${direction}"/>
    <%@ include file="common/pager.jspf" %>
</section>
<%@ include file="common/footer.jspf" %>
