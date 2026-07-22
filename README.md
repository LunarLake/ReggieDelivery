# 瑞吉外卖 (Reggie)

基于 **Spring Boot 3.5.5** 的外卖/到店点餐系统，包含管理后台与用户端双前端。

| 项               | 说明                 |
| ---------------- | -------------------- |
| Group / Artifact | `com.wyc` / `reggie` |
| Java             | 17                   |
| 数据库           | MySQL（`utf8mb4`）   |

## 技术栈

| 层级     | 技术                                         |
| -------- | -------------------------------------------- |
| 后端框架 | Spring Boot 3.5.5（Web）                     |
| ORM      | MyBatis-Plus 3.5.x + Druid 连接池            |
| 工具库   | Lombok、commons-lang3                        |
| JSON     | Jackson（自定义 Long→String 防 JS 精度丢失） |
| 管理后台 | `/backend/**`：Vue 2 + Element UI + Axios    |
| 用户端   | `/front/**`：Vue 2 + Vant + Axios            |

前后端均无独立 Node 构建：页面与脚本直接放在 `src/main/resources`，由 Spring Boot 静态资源映射提供。

## 功能概览

### 已实现

| 模块     | 功能                                                                                                |
| -------- | --------------------------------------------------------------------------------------------------- |
| 员工管理 | 登录/退出、分页查询（姓名模糊搜索）、新增、编辑、启用/禁用、按 ID 查询                              |
| 分类管理 | 新增、分页、删除（关联校验）、修改、按类型条件查询                                                  |
| 菜品管理 | 新增（含口味）、分页（含分类名）、按 ID 查询（含口味）、编辑、批量删除（启售中禁止）、批量启售/停售 |
| 文件服务 | 图片上传（UUID 重命名）、文件下载（防目录穿越、缺图降级占位图）                                     |
| 基础设施 | 登录校验过滤器、全局异常处理、审计字段自动填充（ThreadLocal）、统一响应 `R<T>`                      |

### 待实现

- 套餐管理（Setmeal CRUD，实体已就绪）
- 订单明细
- 用户端：分类与菜品浏览、购物车、地址、下单
- 密码策略升级（如 BCrypt）

## 环境要求

- JDK 17+
- MySQL 5.7+ / 8.x
- 无需单独安装 Maven（项目自带 Wrapper：`mvnw` / `mvnw.cmd`）

## 快速开始

### 1. 初始化数据库

```bash
mysql -u root -p < 数据库种子/db_reggie.sql
```

脚本会创建数据库 `reggie`、表结构及示例数据。

**默认管理员**

| 字段   | 值                                                           |
| ------ | ------------------------------------------------------------ |
| 用户名 | `admin`                                                      |
| 密码   | `123456`（库中存储 MD5：`e10adc3949ba59abbe56e057f20f883e`） |

### 2. 配置本机数据库账号

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

常用命令：

```bash
./mvnw.cmd clean compile                 # 编译
./mvnw.cmd test                          # 测试
./mvnw.cmd clean package -DskipTests     # 打包 JAR
```

### 4. 访问入口

| 端           | 地址                                                |
| ------------ | --------------------------------------------------- |
| 管理后台登录 | http://localhost:8080/backend/page/login/login.html |
| 管理后台首页 | http://localhost:8080/backend/index.html            |
| 用户端       | http://localhost:8080/front/index.html              |

默认端口以 Spring Boot 为准（未改时一般为 `8080`）。

## 项目结构

```
reggie/
├── pom.xml
├── mvnw / mvnw.cmd
├── 数据库种子/db_reggie.sql             # 建库与种子数据
├── src/main/java/com/wyc/reggie/
│   ├── ReggieApplication.java          # 启动类
│   ├── common/                         # 公共组件
│   │   ├── R.java                      # 统一响应封装
│   │   ├── AppException.java           # 业务异常
│   │   ├── GlobalExceptionHandler.java # 全局异常处理（@ControllerAdvice）
│   │   ├── JacksonObjectMapper.java    # JSON 序列化（Long→String、时间格式化）
│   │   ├── MyMetaObjectHandler.java    # 审计字段自动填充（MetaObjectHandler）
│   │   ├── BaseContext.java            # ThreadLocal 存储当前登录用户 ID
│   │   └── WebUtils.java              # 响应工具
│   ├── config/                         # 配置
│   │   ├── WebConfig.java              # 静态资源映射 + Jackson 扩展
│   │   └── MybatisPlusConfig.java      # MyBatis-Plus 分页插件
│   ├── controller/                     # REST 接口
│   │   ├── EmployeeController.java     # /employee
│   │   ├── CategoryController.java     # /category
│   │   ├── DishController.java         # /dish
│   │   └── CommonController.java       # /common（上传下载）
│   ├── dto/
│   │   └── DishDto.java                # 菜品数据传输对象（携带口味、分类名）
│   ├── entity/                         # 数据库实体
│   │   ├── Employee.java
│   │   ├── Category.java
│   │   ├── Dish.java
│   │   ├── DishFlavor.java
│   │   └── Setmeal.java
│   ├── mapper/                         # MyBatis-Plus BaseMapper
│   ├── service/ + impl/                # 业务层
│   └── filter/                         # 过滤器
│       └── LoginCheckFilter.java       # 登录校验（@WebFilter "/*"）
└── src/main/resources/
    ├── application.yml                 # 公共配置（无密码）
    ├── application-local.yml.example
    ├── backend/                        # 管理端静态页
    └── front/                          # 用户端静态页
```

## API 接口

### 员工管理 `/employee`

| 方法 | 路径               | 说明                                           |
| ---- | ------------------ | ---------------------------------------------- |
| POST | `/employee/login`  | 登录（Session 存员工 ID，MD5 密码比对）        |
| POST | `/employee/logout` | 退出（Session 失效）                           |
| GET  | `/employee/page`   | 分页查询；参数 `page`、`pageSize`、可选 `name` |
| POST | `/employee`        | 新增员工（初始密码 123456，校验用户名唯一）    |
| PUT  | `/employee`        | 更新员工信息（启用/禁用）                      |
| GET  | `/employee/{id}`   | 按 ID 查询（密码字段脱敏）                     |

### 分类管理 `/category`

| 方法   | 路径             | 说明                                            |
| ------ | ---------------- | ----------------------------------------------- |
| POST   | `/category`      | 新增分类                                        |
| GET    | `/category/page` | 分页查询；参数 `page`、`pageSize`               |
| PUT    | `/category`      | 修改分类                                        |
| DELETE | `/category`      | 删除分类（参数 `id`；关联菜品/套餐时拒绝）      |
| GET    | `/category/list` | 条件查询；参数 `type`（1=菜品分类, 2=套餐分类） |

### 菜品管理 `/dish`

| 方法   | 路径                    | 说明                                                         |
| ------ | ----------------------- | ------------------------------------------------------------ |
| POST   | `/dish`                 | 新增菜品（JSON `DishDto`，含口味列表）                       |
| GET    | `/dish/page`            | 分页查询；参数 `page`、`pageSize`、可选 `name`；返回含分类名 |
| GET    | `/dish/{id}`            | 按 ID 查询，返回口味列表                                     |
| PUT    | `/dish`                 | 更新菜品及口味                                               |
| DELETE | `/dish`                 | 批量删除；参数 `ids`（逗号分隔）；启售中菜品拒绝删除         |
| POST   | `/dish/status/{status}` | 批量启售/停售；`{status}` 为 0 或 1                          |
| GET    | `/dish/list`            | 菜品列表（仅启售，供套餐弹窗用）；可选 `categoryId`、`name`  |

### 文件服务 `/common`

| 方法 | 路径               | 说明                                               |
| ---- | ------------------ | -------------------------------------------------- |
| POST | `/common/upload`   | 上传文件（MultipartFile），UUID 重命名，返回文件名 |
| GET  | `/common/download` | 下载文件；参数 `name`；防目录穿越，缺图返回占位图  |

## 分层约定

- **Mapper**：`@Mapper` 接口 `extends BaseMapper<Entity>`
- **Service**：接口 `extends IService<Entity>`，实现 `extends ServiceImpl<Mapper, Entity>`
- **查询 / 更新**：优先 `LambdaQueryWrapper` / `LambdaUpdateWrapper`
- **主键**：Snowflake（`id-type: assign_id`），表主键 `bigint`，非数据库自增
- **DTO**：跨表查询场景使用 DTO（如 `DishDto` 携带口味列表和分类名），避免循环依赖

## 统一响应 `R<T>`

```java
R.success(data);   // code = 1
R.error("消息");    // code = 0
```

前端 Axios 拦截器：`code === 0` 且消息为 `NOTLOGIN` → 跳转登录页并清理本地缓存。

## 数据库要点

- 库名：`reggie`，charset：`utf8mb4`
- 价格字段按 **分** 存储（`decimal(10,2)`），前端展示除以 100 为元
- 软删除表（`is_deleted` 0/1）：`dish`、`dish_flavor`、`setmeal`、`setmeal_dish`、`address_book`
- 审计字段（`create_time`/`update_time`/`create_user`/`update_user`）由 `MyMetaObjectHandler` 自动填充
- 完整表结构与示例数据见 `数据库种子/db_reggie.sql`

## 登录校验

`LoginCheckFilter`（`@WebFilter "/*"`）拦截所有请求，放行白名单：

- `/employee/login`、`/employee/logout`
- `/backend/**`、`/front/**`（静态资源）
- `/common/**`（文件上传下载）
- `/user/sendMsg`、`/user/login`、`/user/loginout`（用户端预留）

未登录请求返回 `R.error("NOTLOGIN")`，前端拦截器触发跳转。

## 配置与安全

| 文件                            | 是否提交            | 说明                                |
| ------------------------------- | ------------------- | ----------------------------------- |
| `application.yml`               | 是                  | 应用名、数据源类型、MyBatis-Plus 等 |
| `application-local.yml.example` | 是                  | 本机配置模板                        |
| `application-local.yml`         | **否**（gitignore） | 本机数据库账号密码                  |
| 上传目录                        | **否**（gitignore） | 由 `reggie.upload.path` 配置        |

请勿将真实数据库密码、`.env` 或本机密钥提交到仓库。

## 测试

```bash
./mvnw.cmd test
./mvnw.cmd test -Dtest="ReggieApplicationTests"
```

测试位于 `src/test/java/com/wyc/reggie/`，使用 JUnit 5 与 `@SpringBootTest`。

## 许可证

学习/练习项目，按需使用。
