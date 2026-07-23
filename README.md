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

### 管理后台

| 模块     | 功能                                                                                                |
| -------- | --------------------------------------------------------------------------------------------------- |
| 员工管理 | 登录/退出、分页查询（姓名模糊搜索）、新增、编辑、启用/禁用、按 ID 查询                              |
| 分类管理 | 新增、分页、删除（关联校验）、修改、按类型条件查询                                                  |
| 菜品管理 | 新增（含口味）、分页（含分类名）、按 ID 查询（含口味）、编辑、批量删除（启售中禁止）、批量启售/停售 |
| 套餐管理 | 新增（含菜品关联）、分页（含分类名）、按 ID 查询（含菜品列表）、编辑、批量删除、批量启售/停售       |
| 订单明细 | 分页查询（按订单号/时间范围）、查看详情、派送/完成状态流转                                          |
| 文件服务 | 图片上传（UUID 重命名）、文件下载（防目录穿越、缺图降级占位图）                                     |

### 用户端

| 模块   | 功能                                                       |
| ------ | ---------------------------------------------------------- |
| 登录   | 手机号 + 验证码登录（新用户自动注册）、退出                |
| 点菜   | 分类浏览、菜品列表（含口味选择）、套餐列表、套餐内菜品查看 |
| 购物车 | 添加（同品合并）、减少、清空、口味区分                     |
| 地址簿 | 新增/编辑/删除地址、默认地址管理（每用户仅一个默认地址）   |
| 下单   | 从购物车生成订单 + 明细、自动清空购物车、再来一单          |
| 订单   | 分页历史订单（含订单明细）、再来一单                       |

### 基础设施

- 登录校验过滤器（员工 + 用户双 session）
- 全局异常处理
- 审计字段自动填充（ThreadLocal）
- 统一响应 `R<T>`

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
| 用户端首页   | http://localhost:8080/front/index.html              |
| 用户端登录   | http://localhost:8080/front/page/login.html         |

**用户端登录**：手机号 `13999999998`，验证码 `1234`（演示固定码）。  
默认端口以 Spring Boot 为准（未改时一般为 `8080`）。

## 项目结构

```
reggie/
├── pom.xml
├── mvnw / mvnw.cmd
├── 数据库种子/db_reggie.sql                # 建库与种子数据
├── src/main/java/com/wyc/reggie/
│   ├── ReggieApplication.java             # 启动类
│   ├── common/                            # 公共组件
│   │   ├── R.java                         # 统一响应封装
│   │   ├── AppException.java              # 业务异常
│   │   ├── GlobalExceptionHandler.java    # 全局异常处理
│   │   ├── JacksonObjectMapper.java       # JSON 序列化
│   │   ├── MyMetaObjectHandler.java       # 审计字段自动填充
│   │   ├── BaseContext.java               # ThreadLocal 存储当前用户 ID
│   │   └── WebUtils.java                  # 响应工具
│   ├── config/
│   │   ├── WebConfig.java                 # 静态资源映射 + Jackson
│   │   └── MybatisPlusConfig.java         # MyBatis-Plus 分页插件
│   ├── controller/
│   │   ├── EmployeeController.java        # /employee
│   │   ├── CategoryController.java        # /category
│   │   ├── DishController.java            # /dish
│   │   ├── SetmealController.java         # /setmeal
│   │   ├── CommonController.java          # /common
│   │   ├── UserController.java            # /user
│   │   ├── ShoppingCartController.java    # /shoppingCart
│   │   ├── AddressBookController.java     # /addressBook
│   │   └── OrderController.java           # /order + /orderDetail
│   ├── dto/
│   │   ├── DishDto.java                   # 菜品 DTO（口味列表 + 分类名）
│   │   └── SetmealDto.java                # 套餐 DTO（菜品列表 + 分类名）
│   ├── entity/
│   │   ├── Employee.java                  ├── ShoppingCart.java
│   │   ├── Category.java                  ├── AddressBook.java
│   │   ├── Dish.java                      ├── User.java
│   │   ├── DishFlavor.java                ├── Orders.java
│   │   ├── Setmeal.java                   └── OrderDetail.java
│   │   └── SetmealDish.java
│   ├── mapper/                            # MyBatis-Plus BaseMapper（每实体一个）
│   ├── service/ + impl/                   # 业务层（每实体一个接口 + 实现）
│   └── filter/
│       └── LoginCheckFilter.java          # 登录校验（@WebFilter "/*"）
└── src/main/resources/
    ├── application.yml                    # 公共配置
    ├── application-local.yml.example
    ├── backend/                           # 管理端静态页
    └── front/                             # 用户端静态页
```

## API 接口

### 员工管理 `/employee`

| 方法 | 路径               | 说明                                      |
| ---- | ------------------ | ----------------------------------------- |
| POST | `/employee/login`  | 登录（Session 存员工 ID，MD5 密码比对）   |
| POST | `/employee/logout` | 退出                                      |
| GET  | `/employee/page`   | 分页查询；参数 `page`、`pageSize`、`name` |
| POST | `/employee`        | 新增员工（初始密码 123456）               |
| PUT  | `/employee`        | 更新员工（启用/禁用）                     |
| GET  | `/employee/{id}`   | 按 ID 查询（密码脱敏）                    |

### 分类管理 `/category`

| 方法   | 路径             | 说明                                    |
| ------ | ---------------- | --------------------------------------- |
| POST   | `/category`      | 新增分类                                |
| GET    | `/category/page` | 分页查询                                |
| PUT    | `/category`      | 修改分类                                |
| DELETE | `/category`      | 删除（关联菜品/套餐时拒绝）             |
| GET    | `/category/list` | 条件查询；参数 `type`（1=菜品, 2=套餐） |

### 菜品管理 `/dish`

| 方法   | 路径                    | 说明                                                      |
| ------ | ----------------------- | --------------------------------------------------------- |
| POST   | `/dish`                 | 新增菜品（body: `DishDto`，含口味列表）                   |
| GET    | `/dish/page`            | 分页查询；返回含分类名                                    |
| GET    | `/dish/{id}`            | 按 ID 查询，返回口味列表                                  |
| PUT    | `/dish`                 | 更新菜品及口味                                            |
| DELETE | `/dish`                 | 批量删除；参数 `ids`（逗号分隔）；启售中拒绝              |
| POST   | `/dish/status/{status}` | 批量启售/停售                                             |
| GET    | `/dish/list`            | 菜品列表（仅启售，含口味数据）；可选 `categoryId`、`name` |

### 套餐管理 `/setmeal`

| 方法   | 路径                       | 说明                                                  |
| ------ | -------------------------- | ----------------------------------------------------- |
| POST   | `/setmeal`                 | 新增套餐（body: `SetmealDto`，含菜品关联列表）        |
| GET    | `/setmeal/page`            | 分页查询；返回含分类名                                |
| GET    | `/setmeal/{id}`            | 按 ID 查询，返回套餐菜品列表                          |
| PUT    | `/setmeal`                 | 更新套餐及菜品关联                                    |
| DELETE | `/setmeal`                 | 批量删除；参数 `ids`；启售中拒绝                      |
| POST   | `/setmeal/status/{status}` | 批量启售/停售                                         |
| GET    | `/setmeal/list`            | 用户端：按分类查套餐列表；可选 `categoryId`、`status` |
| GET    | `/setmeal/dish/{id}`       | 用户端：查询套餐内菜品详情                            |

### 用户登录 `/user`

| 方法 | 路径             | 说明                                |
| ---- | ---------------- | ----------------------------------- |
| POST | `/user/sendMsg`  | 发送验证码（演示版固定 `1234`）     |
| POST | `/user/login`    | 手机号+验证码登录（新用户自动注册） |
| POST | `/user/loginout` | 退出登录                            |

### 购物车 `/shoppingCart`

| 方法   | 路径                  | 说明                                                |
| ------ | --------------------- | --------------------------------------------------- |
| GET    | `/shoppingCart/list`  | 查看当前用户购物车                                  |
| POST   | `/shoppingCart/add`   | 添加商品（同品合并数量，口味区分）；返回含 `number` |
| POST   | `/shoppingCart/sub`   | 减少商品（归零自动删除）；返回 `{number: 新数量}`   |
| DELETE | `/shoppingCart/clean` | 清空购物车                                          |

### 地址簿 `/addressBook`

| 方法   | 路径                      | 说明                             |
| ------ | ------------------------- | -------------------------------- |
| GET    | `/addressBook/list`       | 当前用户所有地址（默认地址优先） |
| GET    | `/addressBook/lastUpdate` | 最近更新的地址                   |
| POST   | `/addressBook`            | 新增地址（首个地址自动设为默认） |
| PUT    | `/addressBook`            | 修改地址                         |
| DELETE | `/addressBook`            | 删除地址；参数 `ids`             |
| GET    | `/addressBook/{id}`       | 查询单个地址                     |
| PUT    | `/addressBook/default`    | 设为默认地址（先清除其他默认）   |
| GET    | `/addressBook/default`    | 获取默认地址                     |

### 订单管理 `/order`

| 方法 | 路径                | 说明                                                    |
| ---- | ------------------- | ------------------------------------------------------- |
| GET  | `/order/page`       | 管理端：分页查询；可选 `number`、`beginTime`、`endTime` |
| GET  | `/orderDetail/{id}` | 查询订单详情（含订单明细）                              |
| PUT  | `/order`            | 管理端：修改订单状态（body: `{id, status}`）            |
| POST | `/order/submit`     | 用户端：提交订单（从购物车生成订单+明细，清空购物车）   |
| GET  | `/order/list`       | 用户端：查询所有订单                                    |
| GET  | `/order/userPage`   | 用户端：分页订单历史（含订单明细）                      |
| POST | `/order/again`      | 用户端：再来一单（将历史订单明细重新加入购物车）        |

### 文件服务 `/common`

| 方法 | 路径               | 说明                                   |
| ---- | ------------------ | -------------------------------------- |
| POST | `/common/upload`   | 上传文件（MultipartFile），UUID 重命名 |
| GET  | `/common/download` | 下载文件；参数 `name`；防目录穿越      |

## 分层约定

- **Mapper**：`@Mapper` 接口 `extends BaseMapper<Entity>`
- **Service**：接口 `extends IService<Entity>`，实现 `extends ServiceImpl<Mapper, Entity>`
- **查询 / 更新**：优先 `LambdaQueryWrapper` / `LambdaUpdateWrapper`
- **主键**：Snowflake（`id-type: assign_id`），表主键 `bigint`，非数据库自增
- **DTO**：跨表查询场景使用 DTO（如 `DishDto` 携带口味和分类名，`SetmealDto` 携带菜品列表）

## 统一响应 `R<T>`

```java
R.success(data);   // code = 1
R.error("消息");    // code = 0
```

前端 Axios 拦截器：`code === 0` 且消息为 `NOTLOGIN` → 跳转登录页并清理本地缓存。

## 数据库要点

- 库名：`reggie`，charset：`utf8mb4`
- 价格字段按 **分** 存储（`decimal(10,2)`），前端展示除以 100 为元
- 审计字段（`create_time`/`update_time`/`create_user`/`update_user`）：大部分表有，由 `MyMetaObjectHandler` 自动填充；以下表**无审计字段**：`orders`、`order_detail`、`user`
- 软删除表（`is_deleted` 0/1）：`dish`、`dish_flavor`、`setmeal`、`setmeal_dish`、`address_book`
- 订单状态码：1 待付款、2 待派送、3 已派送、4 已完成、5 已取消
- 完整表结构与示例数据见 `数据库种子/db_reggie.sql`

## 登录校验

`LoginCheckFilter`（`@WebFilter "/*"`）拦截所有请求，放行白名单：

- `/employee/login`、`/employee/logout`
- `/backend/**`、`/front/**`（静态资源）
- `/common/**`（文件上传下载）
- `/user/sendMsg`、`/user/login`、`/user/loginout`

认证逻辑：先检查 `employee` session（管理后台），再检查 `user` session（移动端用户），均未登录返回 `R.error("NOTLOGIN")`。

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
