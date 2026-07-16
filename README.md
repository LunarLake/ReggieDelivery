# 瑞吉外卖 (Reggie)

基于 **Spring Boot 3** 的外卖/到店点餐系统，包含管理后台与用户端双前端。当前后端以员工模块为主，分类、菜品、套餐、订单等前端页面已就绪，对应 API 逐步完善中。

| 项 | 说明 |
|---|---|
| Group / Artifact | `com.wyc` / `reggie` |
| Java | 17 |
| Spring Boot | 3.5.5 |
| 包名 | `com.wyc.reggie` |

## 技术栈

| 层级 | 技术 |
|---|---|
| 后端 | Spring Boot Web、MyBatis-Plus、Druid、Lombok、commons-lang3 |
| 数据库 | MySQL（`utf8mb4`） |
| 管理后台 | 静态资源 `/backend/**`：Vue 2 + Element UI + Axios |
| 用户端 | 静态资源 `/front/**`：Vue 2 + Vant + Axios |

前后端均无独立 Node 构建：页面与脚本直接放在 `src/main/resources`，由 Spring Boot 静态资源映射提供。

## 功能与进度

**已实现**

- 员工登录 / 退出（`POST /employee/login`、`/employee/logout`，密码 MD5 比对）
- 员工分页查询（`GET /employee/page`，支持姓名模糊搜索，密码字段脱敏）
- 员工实体、Mapper、Service（MyBatis-Plus 标准分层）
- MyBatis-Plus 分页插件、统一响应 `R<T>`、前后端静态资源托管

**待实现（前端页面已有，后端 API 待补）**

- 员工增删改、状态启用/禁用
- 分类 / 菜品（含口味）/ 套餐管理
- 订单明细、文件上传下载
- 用户端：分类与菜品浏览、购物车、地址、下单
- 登录校验过滤器、密码策略升级（如 BCrypt）

## 环境要求

- JDK 17+
- MySQL 5.7+ / 8.x（本机可连 `localhost:3306`）
- 无需单独安装 Maven（使用项目自带 Wrapper：`mvnw` / `mvnw.cmd`）

## 快速开始

### 1. 初始化数据库

```bash
# 在 MySQL 中执行项目附带的建库脚本
mysql -u root -p < 资料/db_reggie.sql
```

脚本会创建数据库 `reggie`、表结构及示例数据。

**默认管理员**

| 字段 | 值 |
|---|---|
| 用户名 | `admin` |
| 密码 | `123456`（库中为 MD5：`e10adc3949ba59abbe56e057f20f883e`） |

### 2. 配置本机数据库账号

敏感信息写在本地配置，**不会**提交到 Git：

```bash
# Windows (PowerShell)
Copy-Item src\main\resources\application-local.yml.example src\main\resources\application-local.yml

# Linux / macOS
cp src/main/resources/application-local.yml.example src/main/resources/application-local.yml
```

编辑 `application-local.yml`，填写实际 `url` / `username` / `password`。  
主配置 `application.yml` 已启用 `spring.profiles.active: local`。

### 3. 启动应用

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / macOS
./mvnw spring-boot:run
```

其他常用命令：

```bash
./mvnw.cmd clean compile          # 编译
./mvnw.cmd test                   # 测试
./mvnw.cmd clean package -DskipTests   # 打包 JAR
```

### 4. 访问入口

| 端 | 地址 |
|---|---|
| 管理后台登录 | http://localhost:8080/backend/page/login/login.html |
| 管理后台首页 | http://localhost:8080/backend/index.html |
| 用户端 | http://localhost:8080/front/index.html |

默认端口以 Spring Boot 为准（未改时一般为 `8080`）。

## 项目结构

```
reggie/
├── pom.xml
├── mvnw / mvnw.cmd
├── 资料/db_reggie.sql              # 建库与种子数据
├── src/main/java/com/wyc/reggie/
│   ├── ReggieApplication.java      # 启动类
│   ├── common/                     # 统一响应 R 等
│   ├── config/                     # WebConfig、MyBatisPlusConfig
│   ├── controller/                 # REST 接口
│   ├── entity/                     # 实体
│   ├── mapper/                     # MyBatis-Plus BaseMapper
│   ├── service/ + impl/            # 业务层
│   └── filter/                     # 过滤器（规划中）
└── src/main/resources/
    ├── application.yml             # 公共配置（无密码）
    ├── application-local.yml.example
    ├── backend/                    # 管理端静态页
    └── front/                      # 用户端静态页
```

### 分层约定

- **Mapper**：`extends BaseMapper<Entity>`
- **Service**：接口 `extends IService<Entity>`，实现 `extends ServiceImpl<Mapper, Entity>`
- **查询 / 更新**：优先 `LambdaQueryWrapper` / `LambdaUpdateWrapper`
- **主键**：Snowflake（`id-type: assign_id`），表主键为 `bigint`，非数据库自增

### 统一响应

```java
R.success(data);   // code = 1
R.error("消息");    // code = 0
```

前端 Axios 拦截器在 `code === 0` 且消息为 `NOTLOGIN` 时会跳转登录并清理本地缓存。

## 已有接口（员工）

| 方法 | 路径 | 说明 |
|---|---|---|
| POST | `/employee/login` | 登录；Session 写入员工 id |
| POST | `/employee/logout` | 退出；使 Session 失效 |
| GET | `/employee/page` | 分页；参数 `page`、`pageSize`、可选 `name` |

## 数据库要点

- 价格字段按 **分** 存储（`decimal`），前端展示时除以 100 为元
- 部分业务表使用软删除字段 `is_deleted`（如菜品、套餐）；`employee` 等无此字段
- 完整表结构与示例数据见 `资料/db_reggie.sql`

## 配置与安全说明

| 文件 | 是否提交 | 说明 |
|---|---|---|
| `application.yml` | 是 | 应用名、数据源类型、MyBatis-Plus 等 |
| `application-local.yml.example` | 是 | 本机配置模板 |
| `application-local.yml` | **否**（已 gitignore） | 本机数据库账号密码 |

请勿将真实数据库密码、`.env` 或本机密钥提交到仓库。图片上传目录规划为项目根下 `/upload/`，同样被忽略。

## 测试

```bash
./mvnw.cmd test
./mvnw.cmd test -Dtest="ReggieApplicationTests"
```

测试位于 `src/test/java/com/wyc/reggie/`，使用 JUnit 5 与 `@SpringBootTest`。

## 许可证

学习 / 练习项目，按需使用。
