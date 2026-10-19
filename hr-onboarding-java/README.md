# 新员工入职管理系统（Java + Vue3 前后端分离）

管理员登录 → 新员工入职建号 → 新员工列表。后端 Spring Boot 3，前端 Vue3 + Vite，数据库支持 **MySQL 与 SQLite** 双引擎。

## 技术栈

| 层 | 选型 |
| --- | --- |
| 后端 | Spring Boot 3.2.5 / Java 17 / Maven |
| 持久层 | MyBatis-Plus 3.5.6 |
| 数据库 | MySQL 8 / SQLite 3（Profile 切换，同一套实体与Mapper） |
| 鉴权 | JWT（jjwt 0.12.5）+ BCrypt 密码哈希 |
| 前端 | Vue 3.5（Composition API）/ Vue Router 4 / Axios / Vite 5 |

## 目录结构

```
hr-onboarding-java/
├── backend/                # Spring Boot 后端
│   ├── pom.xml
│   ├── data/               # SQLite 数据文件（运行时生成）
│   ├── scripts/            # 冒烟测试与探测脚本
│   └── src/main/
│       ├── java/com/company/hr/
│       │   ├── config/     # JWT / 拦截器 / MyBatis-Plus / 双库 DatabaseIdProvider / Web 跨域
│       │   ├── controller/ # AuthController、EmployeeController
│       │   ├── service/    # AuthService、EmployeeService
│       │   ├── mapper/     # AdminMapper、EmployeeMapper（@Select 内嵌双库方言分派）
│       │   ├── entity/     # Admin、Employee
│       │   ├── dto/        # 请求/响应/VO
│       │   ├── common/     # Result 统一响应、全局异常
│       │   └── init/       # 管理员账号初始化
│       └── resources/
│           ├── application.yml          # 公共配置，默认 profile=sqlite
│           ├── application-sqlite.yml   # SQLite 数据源与建表脚本
│           ├── application-mysql.yml    # MySQL 数据源（账号密码走环境变量）
│           └── db/{mysql,sqlite}/schema.sql
└── frontend/               # Vue3 前端
    ├── vite.config.js      # devServer 代理 /api → 9090
    └── src/
        ├── main.js / App.vue
        ├── router/         # 路由 + 登录守卫
        ├── utils/          # http.js（axios 封装）、validate.js（同源校验规则）
        ├── views/          # LoginView、EmployeeCreateView、EmployeeListView
        └── styles/main.css
```

## 快速启动

### 1. 启动后端（默认 SQLite，无需装数据库）

**在 IDEA 里**：打开 `backend/src/main/java/com/company/hr/HrOnboardingApplication.java`，
点 `main` 方法左侧绿色三角 → Run。详见 [IDEA-GUIDE.md](./IDEA-GUIDE.md)。

**在命令行终端里**（先 `cd` 到项目根目录再执行）：

```bash
# macOS / Linux
cd backend && mvn spring-boot:run

# Windows CMD
cd backend && mvn spring-boot:run
```

> ⚠️ **不要在 IDEA 的 Maven 运行配置里写 `cd backend && spring-boot:run`**
> IDEA 的 Maven 配置工作目录已经是 `backend` 模块，再 `cd backend` 会进到
> `backend\backend` 这个不存在的目录，报 `cd: 找不到路径` + 一堆 `[ERROR]`。
> 那种场景请直接填 `Working directory: $PROJECT_DIR$/../backend`、`Command line: spring-boot:run`。

> **端口注意**：若本机存在 `SERVER__PORT` 之类环境变量，Spring 的宽松绑定会覆盖 `server.port`。显式指定更稳妥：
> `mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=9090`

后端地址 <http://127.0.0.1:9090>

### 2. 启动前端

```bash
cd frontend
npm install
npm run dev
```

访问 <http://127.0.0.1:5173>

默认管理员：`admin` / `admin123`（首次启动自动初始化，密码 BCrypt 存储）

## 切换到MySQL

### 1. 建库

驱动参数 `createDatabaseIfNotExist=true` 在部分版本/权限下不生效，推荐手工建库：

```sql
CREATE DATABASE hr_onboarding DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
```

也可直接用附带的一次性工具（凭证从环境变量读取，不落源码）：

```bash
java -cp <mysql-connector-j.jar> scripts/CreateDatabase.java
```

### 2. 配置凭证

复制模板并填写（`.env.local` 已被 `.gitignore` 排除）：

```bash
cp .env.local.example .env.local
```

然后用脚本启动，凭证不进入命令行：

```bash
bash scripts/run-mysql.sh    # 读 .env.local，以 mysql profile 启动，端口 9091
```

若想手动启动：

```bash
export DB_URL='jdbc:mysql://127.0.0.1:3306/hr_onboarding?useUnicode=true&characterEncoding=utf8&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai'
export DB_USERNAME=root
export DB_PASSWORD='你的密码'
mvn spring-boot:run -Dspring-boot.run.profiles=mysql
```

### 3. 前端指向 MySQL 实例

```bash
VITE_API_TARGET=http://127.0.0.1:9091 npm run dev
```

建表由 `spring.sql.init` 自动执行 `db/mysql/schema.sql`（全部 `CREATE TABLE IF NOT EXISTS`，可重复执行）。

> ⚠️ 密码不要写进 `application-mysql.yml` 提交到仓库。该文件里密码位是 `${DB_PASSWORD:root}`，仅作本地默认值。

## 验证结果

| 数据库 | 后端接口冒烟 | 前后端联调 | 数据核验 |
| --- | --- | --- | --- |
| SQLite | 44/44 通过 | 15/15 通过 | 建号 7 条、搜索/分页/边界全通过 |
| MySQL 8.0.40 | 44/44 通过 | 15/15 通过 | 直连查库确认 5 条中文记录、密码为 BCrypt 哈希 |

MySQL 版额外验证了关键方言分支：搜索功能走 `CONCAT` 分支（SQLite 走 `||` 分支），两库结果一致。

## 双数据库适配方案

同一套实体与 Mapper 同时服务 MySQL 与 SQLite，关键处理：

| 差异点 | 处理方式 |
| --- | --- |
| 自增主键语法 | 统一用 MyBatis-Plus `ASSIGN_ID`（雪花算法）由应用层生成，两库 DDL 完全一致，无需方言分支 |
| 字符串拼接 | MySQL 用 `CONCAT('%',?,'%')`，SQLite 用 `'%' \|\| ? \|\| '%'`。通过 `DatabaseIdProvider` 注入的 `_databaseId` 在 `@Select` 的 `<script>` 内用 `<choose>` 分派 |
| 类型映射 | `map-underscore-to-camel-case` + 显式列名，两库通用 |
| 库名/连接串 | 各自 profile 独立配置 |

## API

统一响应：`{ code, message, data, errors? }`，`code === 0` 成功。鉴权头 `Authorization: Bearer <token>`。

| 方法 | 路径 | 说明 | 鉴权 |
| --- | --- | --- | --- |
| POST | `/api/auth/login` | 管理员登录，body `{username, password, remember}` | 否 |
| GET | `/api/auth/me` | 会话校验 | 是 |
| POST | `/api/auth/logout` | 退出登录 | 否 |
| POST | `/api/employees` | **入职建号**，JSON body 六字段 | 是 |
| GET | `/api/employees?page=&pageSize=&keyword=` | **员工列表**，仅返回账号与姓名 | 是 |

状态码：`401` 未登录或账号密码错误、`422` 表单校验失败（带字段级 `errors`）、`409` 账号重复、`405` 方法不允许、`400` 请求体非法。

## 字段规则

| 字段 | 控件 | 规则 |
| --- | --- | --- |
| 登录账号 | text | 必填，字母开头，仅字母/数字/下划线/点/短横，4-20 位，大小写不敏感唯一 |
| 密码 | password | 必填，6-32 位且须同时含字母和数字，BCrypt 存储 |
| 姓名 | text | 必填，≤20 字符 |
| 性别 | **radio**（男/女） | 必选 |
| 手机号 | text | 必填，`^1[3-9]\d{9}$` |
| Email | text | 必填，`name@domain.tld` |

前端 `utils/validate.js` 与后端 DTO 注解各持一份同源规则：前端负责体验（失焦即校验、边输入边消错），后端是最终防线，字段级错误回填到对应控件。

## 验证脚本

```bash
# 后端 44 项接口冒烟（不指定地址则打SQLite 实例）
cd backend && node scripts/smoke-test.mjs
cd backend && node scripts/smoke-test.mjs http://127.0.0.1:9091   # 打 MySQL 实例

# 前后端联调 15 项（经 devServer 代理访问）
cd frontend && node scripts/proxy-check.mjs

# MySQL 连通性探测与配置指引
cd backend && node scripts/mysql-probe.mjs

# 直连 MySQL 核验落库数据（含中文与密码哈希检查）
cd backend && source .env.local && java -cp <mysql-connector-j.jar> scripts/VerifyData.java
```

## 安全说明

- 密码 BCrypt（随机盐）存储，`spring-security-crypto` 单件引入，不引整套 Security
- JWT 密钥从 `hr.jwt.secret` 读取，生产必须用 `HR_JWT_SECRET` 环境变量覆盖（HS256 要求 ≥32 字节）
- 登录失败不区分「账号不存在」与「密码错误」，防账号枚举
- 登录接口的 401 属业务失败，前端 axios 拦截器已对该路径豁免，不会误跳登录页
- 列表 SQL 只 SELECT `id, login_account, name`，不把手机号/性别/密码暴露到前端
- 所有过滤条件下推到 SQL，不做「全表捞出后在应用层过滤」
- Vue 模板插值默认转义，天然防 XSS
- CORS 当前 `allowedOriginPatterns("*")`，生产同域部署应收紧

## 已知限制

- 新员工账号暂不可登录（仅管理员角色）。扩展 `/api/auth/login` 加角色分支即可
- JWT 无状态，退出登录仅清除前端 token；如需强制失效需引入服务端黑名单（如 Redis）
- SQLite 适合单机小规模；多实例部署应切 MySQL
- 背景图为 AI 生成，替换 `frontend/src/assets/campus-bg.png` 即可