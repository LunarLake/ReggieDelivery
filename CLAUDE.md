# CLAUDE.md — 瑞吉外卖 (Reggie Takeout)

Spring Boot 3.5.5 + MyBatis-Plus 外卖系统，双前端架构（管理后台 + 用户端）。

- **Group/Artifact**: `com.wyc` / `reggie`
- **Java**: 17

## 构建命令

```bash
./mvnw clean compile          # Windows: ./mvnw.cmd
./mvnw test -Dtest="XxxTest"
./mvnw clean package -DskipTests
./mvnw spring-boot:run
```

## 架构

```
com.wyc.reggie
├── controller/     # @RestController, 返回 R<T>
├── service/        # extends IService<T>
│   └── impl/       # extends ServiceImpl<M, T>
├── mapper/         # extends BaseMapper<T>（无 XML）
├── entity/         # @Data + Serializable
├── common/         # R, AppException, GlobalExceptionHandler, JacksonObjectMapper, MyMetaObjectHandler, BaseContext
├── config/         # WebConfig（静态资源 + Jackson）, MybatisPlusConfig（分页插件）
└── filter/         # LoginCheckFilter
```

## 关键约定

### API 响应：`R<T>`

```java
R.success(data)   // code=1
R.error("msg")    // code=0
```

前端 Axios 拦截器检查 `res.data.code === 0`，`NOTLOGIN` 时跳转登录页。

### MyBatis-Plus

- Mapper: `@Mapper` 接口 extends `BaseMapper<Entity>`
- 查询: `LambdaQueryWrapper<Entity>`（如 `wrapper.eq(Entity::getName, val)`）
- 更新: `LambdaUpdateWrapper<Entity>` + `.set(Entity::getXxx, val)`
- ID: Snowflake（`id-type=assign_id`），insert 后 `entity.getId()` 即获取
- 分页: `new Page<>(page, pageSize)` + `service.page(pageInfo, wrapper)`

### 数据库

- 库名: `reggie`，charset: `utf8mb4`
- 主键: `bigint(20)`，由 Snowflake 生成，**非 AUTO_INCREMENT**
- **价格存分（cent）**：`decimal(10,2)`，如 `7800.00` = ¥78.00。前端展示除以 100
- **密码**: MD5（`DigestUtils.md5DigestAsHex`），无盐，默认密码 `123456`
- **软删除**: `is_deleted` (0/1) 仅部分表（address_book, dish, dish_flavor, setmeal, setmeal_dish）
- 审计字段: `create_time`/`update_time`/`create_user`/`update_user`，由 `MyMetaObjectHandler` 自动填充，user 取自 `BaseContext`（ThreadLocal）

### 前台

| 前台     | URL           | 目录                          | 技术                       |
| -------- | ------------- | ----------------------------- | -------------------------- |
| 管理后台 | `/backend/**` | `src/main/resources/backend/` | Vue 2 + Element UI + Axios |
| 用户端   | `/front/**`   | `src/main/resources/front/`   | Vue 2 + Vant UI + Axios    |

Axios: `baseURL=/`，timeout 10s，`/backend/js/request.js`

### 异常处理

- 业务异常: `throw new AppException("msg")` → `GlobalExceptionHandler` 捕获 → `R.error(msg)`
- 兜底: `Exception` → 打日志 → `R.error("服务器异常")`

### JSON 序列化

`JacksonObjectMapper`：Long → String（防 JS 精度丢失），时间 → `yyyy-MM-dd HH:mm:ss`

## 测试

JUnit 5 + `@SpringBootTest`，MockMvc 用于 controller 层。

## 工作模式

- **优先 inline 探索**：自己直接 Read/Grep/Glob 读代码，尽量少分发 Explore/Plan 子代理。减少独立 API 请求以提升 DeepSeek 缓存命中率。
- 仅在任务范围广、确实需要并行探索多个子系统时才用 Agent 分发。

## Git

- 分支: `master`（本地）
- 提交格式: `<type>: <description>`（feat/fix/refactor/docs/test/chore/perf/ci）
