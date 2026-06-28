<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:set var="pageTitle" value="仪表盘"/>
<%@ include file="common/header.jspf" %>
<div class="page-head">
    <div>
        <p class="eyebrow">欢迎回来</p>
        <h1>${sessionScope.loginUser.realName} 的工作台</h1>
    </div>
    <div class="head-actions">
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/enrollments?action=new">新增选课</a>
        <c:if test="${sessionScope.loginUser.admin}">
            <a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/courses?action=new">新增课程</a>
        </c:if>
    </div>
</div>

<div class="metric-grid">
    <div class="metric"><span>课程总数</span><strong>${coursePage.total}</strong></div>
    <div class="metric"><span>选课记录</span><strong>${enrollmentPage.total}</strong></div>
    <div class="metric"><span>在线会话</span><strong>${applicationScope.onlineCount == null ? 0 : applicationScope.onlineCount}</strong></div>
    <c:if test="${sessionScope.loginUser.admin}">
        <div class="metric"><span>用户总数</span><strong>${userPage.total}</strong></div>
    </c:if>
</div>

<div class="row g-4 mt-1">
    <div class="col-lg-6">
        <section class="panel">
            <div class="panel-title">
                <h2>最新课程</h2>
                <a href="${pageContext.request.contextPath}/courses">查看全部</a>
            </div>
            <div class="table-responsive">
                <table class="table align-middle">
                    <thead><tr><th>课程</th><th>教师</th><th>剩余名额</th></tr></thead>
                    <tbody>
                    <c:forEach var="course" items="${coursePage.records}">
                        <tr>
                            <td>${course.courseName}<div class="small text-muted">${course.courseCode}</div></td>
                            <td>${course.teacher}</td>
                            <td>${course.capacity}</td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </section>
    </div>
    <div class="col-lg-6">
        <section class="panel">
            <div class="panel-title">
                <h2>最近选课</h2>
                <a href="${pageContext.request.contextPath}/enrollments">查看全部</a>
            </div>
            <div class="table-responsive">
                <table class="table align-middle">
                    <thead><tr><th>学生</th><th>课程</th><th>状态</th></tr></thead>
                    <tbody>
                    <c:forEach var="item" items="${enrollmentPage.records}">
                        <tr>
                            <td>${item.realName}</td>
                            <td>${item.courseName}</td>
                            <td><span class="badge text-bg-success">${item.status}</span></td>
                        </tr>
                    </c:forEach>
                    </tbody>
                </table>
            </div>
        </section>
    </div>
</div>
<%@ include file="common/footer.jspf" %>
