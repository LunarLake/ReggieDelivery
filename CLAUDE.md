# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Reggie takeout/delivery system (瑞吉外卖) — a Spring Boot 3.5.5 web application with a dual-frontend architecture: a management backend for restaurant staff and a customer-facing storefront for ordering.

- **Group/Artifact**: `com.wyc` / `reggie`
- **Java**: 17
- **Package**: `com.wyc.reggie`

## Build & Run Commands

```bash
# Build (uses Maven Wrapper — no local Maven install needed)
./mvnw clean compile

# Run tests
./mvnw test

# Run a single test class
./mvnw test -Dtest="ReggieApplicationTests"

# Run a single test method
./mvnw test -Dtest="ReggieApplicationTests#contextLoads"

# Package as JAR
./mvnw clean package -DskipTests

# Run the application
./mvnw spring-boot:run
```

On Windows, use `./mvnw.cmd` instead of `./mvnw`.

## Architecture

### Backend (Spring Boot layered architecture)

```
com.wyc.reggie
├── ReggieApplication          # @SpringBootApplication entry point
├── controller/                # REST controllers
├── service/                   # Interface definitions (extend IService<T>)
│   └── impl/                  # Implementations (extend ServiceImpl<M, T>)
├── mapper/                    # MyBatis-Plus BaseMapper interfaces
├── entity/                    # Domain models (@Data, Serializable)
├── common/                    # R (response wrapper), WebConfig, future utilities
├── config/                    # Configuration classes (future)
└── filter/                    # Servlet filters (login check, etc.)
```

### MyBatis-Plus Patterns (already established)

All data-access follows these conventions:

- **Mapper**: Interface extends `BaseMapper<Entity>` — no XML needed for CRUD.
- **Service**: Interface extends `IService<Entity>`, impl extends `ServiceImpl<Mapper, Entity>`.
- **Query**: Use `LambdaQueryWrapper<Entity>` for type-safe queries (e.g., `wrapper.eq(Entity::getUsername, value)`).
- **Update**: Use `LambdaUpdateWrapper<Entity>` for partial updates (`wrapper.eq(Entity::getId, id).set(Entity::getName, newName)`).
- **ID generation**: Snowflake (`id-type=assign_id` in config). IDs are auto-generated on insert — check `entity.getId()` after `mapper.insert(entity)`.

### API Response Format

All endpoints return `R<T>`:

```java
// Success: code=1, data populated
R.success(employee)

// Error: code=0, msg populated
R.error("登录失败")
```

Frontend Axios response interceptor checks `res.data.code === 0` — on `NOTLOGIN` msg it redirects to the login page and clears localStorage.

### Dual Frontend (served as static resources)

Both frontends are **static files** served directly by Spring Boot via `WebConfig` resource handlers — no build step, no bundler.

| Frontend | URL Path | Directory | Audience |
|---|---|---|---|
| Management backend | `/backend/**` | `src/main/resources/backend/` | Restaurant staff |
| Customer storefront | `/front/**` | `src/main/resources/front/` | End customers |

The management backend (`/backend/index.html`) is an SPA shell using iframes for sub-pages:
- **Login**: `/backend/page/login/login.html` → calls `POST /employee/login`
- **Main shell**: Vue 2 app with sidebar menu (员工管理, 分类管理, 菜品管理, 套餐管理, 订单明细), each loads its page in an iframe
- Each sub-page has its own Vue instance and API module under `backend/api/`

The customer storefront (`/front/index.html`) is a mobile-oriented ordering interface with category browsing, dish/meal selection, cart management, and checkout flow.

**Frontend tech stack** (both frontends):
- Vue 2 (loaded via `<script>` tag, not a build tool)
- Element UI component library
- Axios (configured in `backend/js/request.js` — baseURL `/`, timeout 10s, response interceptor handles `NOTLOGIN` redirect)
- Vant UI (customer frontend only, for mobile components)

## Key Dependencies

| Dependency | Version | Purpose |
|---|---|---|
| `spring-boot-starter-web` | (managed) | REST API framework (Spring MVC) |
| `mysql-connector-j` | (managed) | MySQL JDBC driver (runtime) |
| `lombok` | (managed) | `@Data`, `@Slf4j`, etc. (compile-time) |
| `mybatis-plus-boot-starter` | 3.5.10.1 | ORM — `BaseMapper`, `LambdaQueryWrapper`, pagination |
| `druid-spring-boot-starter` | 1.2.23 | Alibaba Druid connection pool |
| `commons-lang3` | 3.17.0 | `StringUtils`, `ObjectUtils` utilities |
| `spring-boot-starter-test` | (managed) | JUnit 5 + MockMvc test support |

**(managed)** = version controlled by `spring-boot-starter-parent` BOM.

Note: `mybatis-spring` is pinned to `3.0.4` explicitly (excluded from the mybatis-plus starter due to version conflict with Spring Boot 3.5.x).

## Database Schema

The full schema is defined in `资料/db_reggie.sql`. The database `reggie` uses `utf8mb4` charset. Key conventions:

- **Primary keys**: `bigint(20)`, populated by Snowflake algorithm (MyBatis-Plus `assign_id`). Not AUTO_INCREMENT.
- **Audit fields**: Most tables have `create_time`, `update_time`, `create_user`, `update_user`.
- **Soft delete**: `is_deleted` (0/1) on: `address_book`, `dish`, `dish_flavor`, `setmeal`, `setmeal_dish`. **NOT** on `employee`, `category`, `orders`, `order_detail`, `shopping_cart`, `user`.
- **Price storage**: Prices are stored in **cents (分)** as `decimal(10,2)`. The frontend divides by 100 to display yuan. E.g., `7800.00` in DB = ¥78.00.
- **Password**: MD5 hash. The seed admin password is `e10adc3949ba59abbe56e057f20f883e` (MD5 of `123456`). The current login controller does plaintext comparison — **needs to be updated to MD5 + salt**.

### Tables

| Table | Entity (to create) | Description | Key fields |
|---|---|---|---|
| `employee` | ✅ `Employee.java` | Staff accounts | `username` (UNIQUE), `password` (MD5), `status` (0=禁用,1=正常), `id_number` |
| `category` | — | Dish & meal categories | `type` (1=菜品分类, 2=套餐分类), `name` (UNIQUE), `sort` |
| `dish` | — | Dishes/menu items | `category_id`, `price` (cents), `code`, `image`, `status` (0=停售,1=起售), `is_deleted` |
| `dish_flavor` | — | Flavor options per dish | `dish_id`, `name` (e.g. 辣度/温度/忌口), `value` (JSON array string like `["不辣","微辣","中辣","重辣"]`) |
| `setmeal` | — | Combo/set meals | `category_id`, `price` (cents), `status`, `image`, `is_deleted` |
| `setmeal_dish` | — | Dishes in a set meal | `setmeal_id`, `dish_id`, `name`, `price` (both redundant), `copies` (份数) |
| `orders` | — | Customer orders | `number` (order #), `status` (1=待付款,2=待派送,3=已派送,4=已完成,5=已取消), `user_id`, `address_book_id`, `amount` (cents), `pay_method` (1=微信,2=支付宝) |
| `order_detail` | — | Line items in an order | `order_id`, `dish_id` (nullable), `setmeal_id` (nullable), `dish_flavor`, `number`, `amount` (cents) |
| `shopping_cart` | — | Customer cart | `user_id`, `dish_id` (nullable), `setmeal_id` (nullable), `dish_flavor`, `number`, `amount` (cents) |
| `address_book` | — | Customer addresses | `user_id`, `consignee`, `phone`, province/city/district breakdown, `is_default` |
| `user` | — | Customer accounts | `phone`, `name`, `sex`, `id_number`, `avatar`, `status` |

### Seed Data

- **Admin user**: `username=admin`, `password=e10adc3949ba59abbe56e057f20f883e` (MD5 of `123456`), `status=1`
- **Categories**: 湘菜, 川菜, 粤菜, 饮品, 主食 (dish categories); 商务套餐, 儿童套餐 (meal categories)
- **Dishes**: ~20 dishes across categories with images, flavors, and descriptions
- **Set meals**: 1 combo ("儿童套餐A计划") containing 3 dishes
- **Addresses**: 2 sample addresses for test user

## Configuration

`src/main/resources/application.yml` configures:
- **DataSource**: Druid → `reggie` database on `localhost:3306` (credentials in the file)
- **MyBatis-Plus**: `map-underscore-to-camel-case: true`, `id-type: assign_id` (Snowflake)

## Implementation Status

Currently implemented:
- **Employee login**: `POST /employee/login` — plaintext password comparison (⚠️ DB stores MD5 hashes, controller needs updating), returns `R<Employee>` or error
- **Employee entity + mapper + service**: Full CRUD via MyBatis-Plus
- **Static resource serving**: Both frontends accessible via `WebConfig`

Not yet implemented (frontend pages exist, waiting for backend APIs):
- Employee CRUD endpoints (list/add/edit/status toggle)
- Category management (分类管理)
- Dish management (菜品管理) with flavor options
- Combo/meal management (套餐管理) with dish composition
- Order management (订单明细)
- Customer-facing APIs (category listing, dish listing, cart, address, order placement)
- Login check filter (session/token validation)
- Password hashing (MD5 → BCrypt or similar upgrade)
- File upload/download for images (`/common/upload`, `/common/download`)

## Testing

- **Framework**: JUnit 5 via `spring-boot-starter-test`
- **Test package**: mirrors main layout under `src/test/java/com/wyc/reggie/`
- Tests use `@SpringBootTest` for integration-level Spring context loading
- Existing tests (`TestEmp`, `TestEmpService`) demonstrate the CRUD testing pattern with `@Order` for sequential execution
- Use `MockMvc` for controller layer tests without starting a full server

## Git Workflow

- **Main branch**: `main` (remote default)
- **Working branch**: `master` (local)
- Commit format: `<type>: <description>` (types: feat, fix, refactor, docs, test, chore, perf, ci)
