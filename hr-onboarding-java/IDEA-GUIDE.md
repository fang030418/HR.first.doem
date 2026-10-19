# 在 IntelliJ IDEA 中打开本项目

## 一、导入

**方式一：直接打开整个目录（推荐）**

IDEA → `File` → `Open` → 选中解压后的 `hr-onboarding-java` 目录 → OK。

IDEA 会自动识别出两个模块：

- `backend` —— Maven 项目（Spring Boot）
- `frontend` —— Node.js 项目（package.json）

**方式二：先打开后端，前端单独用 VS Code / WebStorm**

`File` → `Open` → 选 `backend` 目录。

## 二、后端准备

### 1. 确认 JDK

`File` → `Project Structure` → `Project`：

- SDK 选 **17 或更高**（推荐 21）
- Language level 选 17+

> 本项目使用 Spring Boot 3.2.5，最低要求 JDK 17。

### 2. 等待 Maven 导入

首次打开时右下角会出现进度条「Loading Maven projects」，等待完成即可。

若未自动导入：右键 `backend/pom.xml` → `Add as Maven Project`。

依赖已在 `pom.xml` 中声明，首次导入需联网下载。

### 3. 配置 Maven

`Settings` → `Build, Execution, Deployment` → `Build Tools` → `Maven`：

- `Maven home path`: 用 `Bundled Maven` 即可，或指向本地 `E:\develop\apache-maven-3.9.9`

> 国内网络建议在 `settings.xml` 的 `<mirrors>` 里配置阿里云镜像加速。

### 4. ⚠️ 端口被占用的坑

本机若存在环境变量 `SERVER__PORT`，Spring Boot 的宽松绑定会把它读成 `server.port`，导致用 65148 等被占用端口启动失败。

**解决**：在 `backend/src/main/resources/application.yml` 里把 `server.port` 改成 `9090`（已是此值），
或用 VM options / Program arguments 显式指定：`--server.port=9090`。

推荐做法：直接改用 IDEA 的运行配置（见下节），不动代码。

## 三、运行后端

### 方式 A：Spring Boot 运行配置（最推荐）

打开 `backend/src/main/java/com/company/hr/HrOnboardingApplication.java`，
点击 `main` 方法左侧的绿色三角 → `Run 'HrOnboardingApplication'`。

**默认使用 SQLite**，无需安装数据库。

若你的机器存在 `SERVER__PORT` 环境变量导致端口冲突，
在 `Run` → `Edit Configurations` → 选中该配置 → `Modify options` → 勾选 `Add program arguments`，
在 `Program arguments` 填：`--server.port=9090`

### 方式 B：Maven 运行配置

`Run` → `Add Configuration` → `+` → `Maven`，按下表填写：

| 配置项 | 填什么 |
| --- | --- |
| Name | `backend (sqlite)` |
| Working directory | `$PROJECT_DIR$/../backend` |
| Command line | `spring-boot:run` |

> ⚠️ **常见错误：不要在 Command line 里写 `cd backend && spring-boot:run`**
>
> IDEA 的 Maven 运行配置**工作目录已经是 backend 模块**了，若命令里再写一次 `cd backend`，
> 会变成进`backend\backend` 这个不存在的目录，导致：
>
> ```
> cd: 找不到路径"E:\...\backend\backend"。因为该路径不存在。
> [ERROR] Failed to execute goal ...spring-boot:run... exit code: 1
> ```
>
> 后面那一堆 `[ERROR]` 都是 `cd` 失败后 Maven 根本没执行时打出来的空壳报错，
> **真正的原因只有第一行 `cd` 失败**。

> 如果你的 Working directory 已填 `$PROJECT_DIR$/../backend` 但仍报 `backend\backend`，
> 说明你解压时多套了一层目录。检查项目路径应该形如：
> `E:\idea\hr-onboarding-java\backend`（含 pom.xml 那一层），
> 而不是 `E:\idea\hr-onboarding-java\hr-onboarding-java\backend`。

### 方式 C：切换到 MySQL

先准备凭证文件：

```bash
cd backend
cp .env.local.example .env.local
```

编辑 `.env.local` 填入你的 MySQL 账号密码，然后：

1. `Run` → `Edit Configurations` → 在 `HrOnboardingApplication` 的
   `Active profiles` 填 `mysql`
2. 或在 VM options 填入凭证（推荐，避免密码进仓库）：

```
-DDB_USERNAME=root -DDB_PASSWORD=你的密码
```

3. 确保数据库已创建：

```sql
CREATE DATABASE hr_onboarding DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
```

也可以直接用脚本启动（读 `.env.local`，凭证不进命令行）：

```bash
cd backend && bash scripts/run-mysql.sh
```

## 四、前端准备

### 1. 用 IDEA 的 Node.js 支持打开

IDEA 社区版/旗舰版均支持 Node.js。若没有对应插件，可：

- 用 `File` → `Open` 单独打开 `frontend` 目录
- 或用 VS Code / WebStorm 打开 `frontend`

### 2. 设置 Node 解释器

`Settings` → `Languages & Frameworks` → `Node.js`：

- Node interpreter:选本机 Node（建议 18+）

### 3. 安装依赖

在 IDEA 的 Terminal 里执行：

```bash
cd frontend
npm install
```

### 4. 启动 devServer

右键 `frontend/package.json` → `Scripts` → 双击 `dev`。

或在 Terminal：

```bash
npm run dev
```

访问 <http://127.0.0.1:5173>

默认管理员：`admin` / `admin123`

## 五、后端指向哪个数据库

`frontend/vite.config.js` 中：

```js
target: process.env.VITE_API_TARGET || 'http://127.0.0.1:9090'
```

- 默认代理到 SQLite 实例 `9090`
- 后端跑 MySQL（端口 9091）时，用环境变量切换：

```bash
VITE_API_TARGET=http://127.0.0.1:9091 npm run dev
```

或在 IDEA 的 npm 运行配置里加环境变量 `VITE_API_TARGET=http://127.0.0.1:9091`。

## 六、可选：一次启动前后端

`Run` → `Edit Configurations` → `+` → `Compound`，勾选前后端两个配置，
以后点一次 Compound 就能同时启动前后端。

## 七、常见问题

| 现象 | 原因与解决 |
| --- | --- |
| 启动报 `Port 65148 was already in use` | 环境变量 `SERVER__PORT` 被 Spring 读成端口。用方式 A/B 的运行配置显式指定 `--server.port=9090` |
| `Failed to obtain JDBC Connection`（MySQL） | 检查 `.env.local` 凭证、数据库是否已创建、profile 是否为 mysql |
| 前端 5173 访问不了 | devServer 未启动，或端口被占；检查 `npm run dev` 输出 |
| 前端接口 502/401 | 后端未启动，或 token 失效；先访问后端 `/api/auth/me` 确认返回 401 而非连不上 |
| Maven 依赖下载慢 | 配置阿里云镜像：`https://maven.aliyun.com/repository/public` |
| 中文显示乱码 | 确认 `application.yml` 的 `encoding: UTF-8`，IDEA 文件编码设为 UTF-8 |

## 八、验证脚本

项目自带三个验证脚本，可用来确认环境是否正常：

```bash
# 后端接口冒烟 44 项
cd backend && node scripts/smoke-test.mjs

# 前后端联调 15 项（需前后端都已启动）
cd frontend && node scripts/proxy-check.mjs

# MySQL 连通性探测
cd backend && node scripts/mysql-probe.mjs
```

`backend/scripts/smoke-test.mjs` 默认打 `9090`（SQLite），
打 MySQL 实例用：`node scripts/smoke-test.mjs http://127.0.0.1:9091`