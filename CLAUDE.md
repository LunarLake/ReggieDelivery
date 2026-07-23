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
│   ├── EmployeeController.java     # /employee — 登录/登出/CRUD/分页
│   ├── CategoryController.java     # /category — 分类CRUD
│   ├── DishController.java         # /dish — 菜品CRUD（含口味）
│   ├── SetmealController.java      # /setmeal — 套餐CRUD（含菜品关联）
│   ├── CommonController.java       # /common — 文件上传/下载
│   ├── UserController.java         # /user — 手机号+验证码登录
│   ├── ShoppingCartController.java # /shoppingCart — 购物车增删查
│   ├── AddressBookController.java  # /addressBook — 地址簿CRUD+默认地址
│   └── OrderController.java        # /order — 订单提交/分页/状态/再来一单
├── service/        # extends IService<T>
│   └── impl/       # extends ServiceImpl<M, T>
├── mapper/         # extends BaseMapper<T>（无 XML）
├── entity/         # @Data + Serializable（共 12 个实体）
├── dto/            # DishDto, SetmealDto
├── common/         # R, AppException, GlobalExceptionHandler, JacksonObjectMapper,
│                   # MyMetaObjectHandler, BaseContext, WebUtils
├── config/         # WebConfig（静态资源+Jackson）, MybatisPlusConfig（分页插件）
└── filter/         # LoginCheckFilter（员工+用户双session）
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
- 更新: `LambdaUpdateWrapper<Entity>` + `.set(Entity::getXxx, val)` 或 `updateById`
- ID: Snowflake（`id-type=assign_id`），insert 后 `entity.getId()` 即获取
- 分页: `new Page<>(page, pageSize)` + `service.page(pageInfo, wrapper)`

### 数据库

- 库名: `reggie`，charset: `utf8mb4`
- 主键: `bigint(20)`，由 Snowflake 生成，**非 AUTO_INCREMENT**
- **价格存分（cent）**：`decimal(10,2)`，如 `7800.00` = ¥78.00。前端展示除以 100
- **密码**: MD5（`DigestUtils.md5DigestAsHex`），无盐，默认密码 `123456`
- **审计字段**: `create_time`/`update_time`/`create_user`/`update_user`，由 `MyMetaObjectHandler` 自动填充，user 取自 `BaseContext`（ThreadLocal）
  - **例外**：`orders`、`order_detail`、`user` 表无审计字段
- **软删除**: `is_deleted` (0/1) 仅部分表（dish, dish_flavor, setmeal, setmeal_dish, address_book）
- **订单状态**: 1待付款、2待派送、3已派送、4已完成、5已取消

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

### 购物车

- 菜品按 `userId + dishId + dishFlavor` 区分条目（同菜品不同口味 = 不同条目）
- 套餐按 `userId + setmealId` 区分
- 已存在则数量+1，不存在则新增

### 订单提交流程

1. 获取当前用户购物车数据
2. 获取收货地址信息
3. 计算总金额，生成订单号（时间戳）
4. 插入 Orders 记录（status=1）
5. 遍历购物车→插入 OrderDetail 记录
6. 清空购物车
7. `@Transactional` 保证原子性

### 登录校验 Filter

`LoginCheckFilter` 双 session 检查：

1. 先查 `employee` session → 管理后台认证
2. 再查 `user` session → 移动端用户认证
3. 均无 → 返回 `NOTLOGIN`

白名单: `/employee/login`、`/employee/logout`、`/backend/**`、`/front/**`、`/common/**`、`/user/sendMsg`、`/user/login`、`/user/loginout`

## 测试

JUnit 5 + `@SpringBootTest`，MockMvc 用于 controller 层。

## Git

- 分支: `master`
- 提交格式: `<type>: <description>`（feat/fix/refactor/docs/test/chore/perf/ci）
- 提交消息使用中文
