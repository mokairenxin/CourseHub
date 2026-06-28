# CourseHub 课程选课管理系统

CourseHub 是一个基于 JavaWeb 基础技术实现的 B/S 架构课程选课管理系统，覆盖 JSP、Servlet、JavaBean、JDBC、Filter、Session/Cookie、Ajax、文件上传下载、CSV 导出、分页、搜索、权限控制和事务管理。

## 技术栈

- 后端：Servlet 4.0、JSP、JSTL、JDBC、HikariCP、MySQL
- 前端：HTML、CSS、原生 JavaScript、Bootstrap 5
- 构建：Maven WAR 项目
- 安全：SHA-256 密码摘要、PreparedStatement、XSS 转义、CSRF Token

## 功能模块

- 用户注册、登录、注销，登录成功后用户信息保存到 Session
- Cookie 记住用户名
- 管理员和普通用户两种角色
- 登录拦截、管理员权限拦截、编码过滤和 CSRF 校验过滤
- 用户管理：管理员可分页搜索用户、修改角色、删除用户及关联数据
- 课程管理：课程增删改查、分页、条件搜索、排序、课程附件单文件上传
- 选课管理：选课增删改查、用户只能管理自己的选课，管理员可查看全部
- 多表 JOIN：选课列表关联 users、courses、enrollments
- 事务管理：选课扣减课程容量、退课恢复容量、删除用户/课程时清理关联数据
- 文件上传：头像上传、课程单文件上传、选课多文件上传
- 文件下载：选课附件下载
- 数据导出：选课记录导出 CSV
- Listener：在线会话数量统计
- Ajax：注册页用户名唯一性异步校验

## 目录结构

```text
src/main/java/com/hnit/coursehub
├── controller   Servlet 控制层
├── service      Service 接口
├── service/impl Service 实现
├── dao          DAO 接口
├── dao/impl     JDBC 实现
├── entity       JavaBean 实体
├── filter       过滤器
├── listener     监听器
└── util         工具类
```

## 数据库配置

1. 创建数据库并导入脚本：

```sql
source database/init.sql;
```

2. 修改 `src/main/resources/db.properties`：

```properties
jdbc.url=jdbc:mysql://localhost:3306/coursehub?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
jdbc.username=root
jdbc.password=123456
```

## 运行方式

1. 使用 Maven 打包：

```bash
mvn clean package
```

2. 将 `target/coursehub.war` 部署到 Tomcat 9 的 `webapps` 目录。
3. 浏览器访问：

```text
http://localhost:8080/coursehub
```

## 本机从头启动步骤

适用于当前本机路径：

- 项目目录：`E:\java web`
- Tomcat 目录：`D:\Dapache-tomcat\apache-tomcat-9.0.119`
- 数据库名：`coursehub`

1. 确认 MySQL 已启动，并且可以用 MySQL Workbench 连接 `root` 账号。

2. 如首次运行，先在 MySQL Workbench 中执行数据库脚本：

```text
E:\java web\database\init.sql
```

3. 检查数据库配置文件：

```text
E:\java web\src\main\resources\db.properties
```

确认账号密码正确：

```properties
jdbc.username=root
jdbc.password=123456
```



4. 在 VS Code 终端进入项目目录并打包：

```powershell
cd "E:\java web"
mvn clean package
```

看到 `BUILD SUCCESS` 后，会生成：

```text
E:\java web\target\coursehub.war
```

5. 复制 WAR 包到 Tomcat：

```powershell
copy "E:\java web\target\coursehub.war" "D:\Dapache-tomcat\apache-tomcat-9.0.119\webapps\coursehub.war"
```

6. 启动 Tomcat：

```powershell
D:\Dapache-tomcat\apache-tomcat-9.0.119\bin\startup.bat
```

7. 浏览器访问：

```text
http://localhost:8080/coursehub
```

8. 停止 Tomcat：

```powershell
D:\Dapache-tomcat\apache-tomcat-9.0.119\bin\shutdown.bat
```

如果代码没有修改、数据库也已经导入过，以后重新开机通常只需要启动 MySQL 和 Tomcat，然后访问 `http://localhost:8080/coursehub`。

## 测试账号

| 角色 | 用户名 | 密码 |
| --- | --- | --- |
| 管理员 | admin | 123456 |
| 普通用户 | student | 123456 |

## 本机账号密码

### MySQL 数据库

项目默认数据库连接配置位于：

```text
E:\java web\src\main\resources\db.properties
```

当前默认配置：

```properties
jdbc.url=jdbc:mysql://localhost:3306/coursehub?useUnicode=true&characterEncoding=UTF-8&useSSL=false&serverTimezone=Asia/Shanghai
jdbc.username=root
jdbc.password=123456
```

如果本机 MySQL 的 `root` 密码不是 `123456`，请把 `jdbc.password` 改成本机真实密码。

### 系统登录账号

数据库脚本 `database/init.sql` 默认创建以下登录账号：

| 角色 | 用户名 | 密码 | 说明 |
| --- | --- | --- | --- |
| 管理员 | admin | 123456 | 可进入后台、管理用户、课程和全部选课数据 |
| 普通用户 | student | 123456 | 可登录、选课、上传附件、管理自己的选课 |
| 普通用户 | lisi | 123456 | 测试普通用户账号 |

## 小组成员

| 学号 | 姓名 | 角色 | 工作量 |
| --- | --- | --- | --- |
| 请填写 | 请填写 | 组长/组员 | 100% |

## 说明

- JSP 页面全部使用 EL/JSTL，没有使用 scriptlet。
- 所有 SQL 操作使用 PreparedStatement。
- 数据库连接参数放在 `db.properties`，连接池使用 HikariCP。
- 上传文件默认保存到 Web 应用的 `uploads` 目录，仓库通过 `.gitignore` 忽略真实上传文件。
