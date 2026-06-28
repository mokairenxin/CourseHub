<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="用户管理"/>
<%@ include file="../common/header.jspf" %>
<div class="page-head">
    <div>
        <p class="eyebrow">管理员后台</p>
        <h1>用户管理</h1>
    </div>
    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/dashboard">返回仪表盘</a>
</div>
<section class="panel">
    <form class="row g-3 align-items-end mb-3" method="get" action="${pageContext.request.contextPath}/admin/users">
        <div class="col-md-6">
            <label class="form-label" for="keyword">关键词</label>
            <input class="form-control" id="keyword" name="keyword" value="${keyword}" placeholder="账号、姓名、邮箱">
        </div>
        <div class="col-md-3">
            <label class="form-label" for="role">角色</label>
            <select class="form-select" id="role" name="role">
                <option value="">全部</option>
                <option value="0" ${role == 0 ? 'selected' : ''}>普通用户</option>
                <option value="1" ${role == 1 ? 'selected' : ''}>管理员</option>
            </select>
        </div>
        <div class="col-md-3">
            <button class="btn btn-primary w-100" type="submit">搜索</button>
        </div>
    </form>
    <c:set var="nextDir" value="${direction == 'asc' ? 'desc' : 'asc'}"/>
    <div class="table-responsive">
        <table class="table table-hover align-middle data-table">
            <thead>
            <tr>
                <th><a href="${pageContext.request.contextPath}/admin/users?keyword=${keyword}&role=${role}&sort=username&direction=${nextDir}">账号</a></th>
                <th><a href="${pageContext.request.contextPath}/admin/users?keyword=${keyword}&role=${role}&sort=real_name&direction=${nextDir}">姓名</a></th>
                <th>邮箱</th>
                <th><a href="${pageContext.request.contextPath}/admin/users?keyword=${keyword}&role=${role}&sort=role&direction=${nextDir}">角色</a></th>
                <th><a href="${pageContext.request.contextPath}/admin/users?keyword=${keyword}&role=${role}&sort=created_at&direction=${nextDir}">创建时间</a></th>
                <th class="text-end">操作</th>
            </tr>
            </thead>
            <tbody>
            <c:forEach var="user" items="${pageBean.records}">
                <tr>
                    <td>
                        <img class="avatar-xs" src="${pageContext.request.contextPath}${user.avatarPath}" alt="头像">
                        ${user.username}
                    </td>
                    <td><input class="form-control form-control-sm" form="edit-user-${user.id}" name="realName" value="${user.realName}" required></td>
                    <td><input class="form-control form-control-sm" form="edit-user-${user.id}" name="email" value="${user.email}" type="email" required></td>
                    <td>
                        <select class="form-select form-select-sm" form="edit-user-${user.id}" name="role">
                            <option value="0" ${user.role == 0 ? 'selected' : ''}>普通用户</option>
                            <option value="1" ${user.role == 1 ? 'selected' : ''}>管理员</option>
                        </select>
                    </td>
                    <td>${user.createdAt}</td>
                    <td class="text-end table-actions">
                        <form id="edit-user-${user.id}" action="${pageContext.request.contextPath}/admin/users" method="post" class="d-inline">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <input type="hidden" name="id" value="${user.id}">
                            <button class="btn btn-sm btn-outline-primary" type="submit">保存</button>
                        </form>
                        <form class="d-inline confirm-form" action="${pageContext.request.contextPath}/admin/users" method="post" data-confirm="确定删除该用户及其关联数据？">
                            <input type="hidden" name="action" value="delete">
                            <input type="hidden" name="csrfToken" value="${csrfToken}">
                            <input type="hidden" name="id" value="${user.id}">
                            <button class="btn btn-sm btn-outline-danger" type="submit">删除</button>
                        </form>
                    </td>
                </tr>
            </c:forEach>
            <c:if test="${empty pageBean.records}">
                <tr><td colspan="6" class="empty-row">暂无用户</td></tr>
            </c:if>
            </tbody>
        </table>
    </div>
    <c:set var="pageUrl" value="${pageContext.request.contextPath}/admin/users?keyword=${keyword}&role=${role}&sort=${sort}&direction=${direction}"/>
    <%@ include file="../common/pager.jspf" %>
</section>
<%@ include file="../common/footer.jspf" %>
