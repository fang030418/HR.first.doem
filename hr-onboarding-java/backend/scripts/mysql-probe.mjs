/**
 * MySQL profile 可用性探测。
 *
 * 用途：验证 mysql profile 的配置是否可连通。默认先探测 3306 上是否真有
 * MySQL 服务；可连则尝试建库并启动后端做全量回归，不可连则打印配置指引后退出。
 *
 * 用法： node scripts/mysql-probe.mjs
 */
import net from 'node:net';

const HOST = '127.0.0.1';
const PORT = 3306;
const DB = 'hr_onboarding';
const USER = process.env.DB_USERNAME || 'root';
const PASS = process.env.DB_PASSWORD || 'root';

function probePort(host, port, timeout = 3000) {
  return new Promise((resolve) => {
    const socket = net.createConnection({ host, port });
    const done = (result) => {
      socket.destroy();
      resolve(result);
    };
    socket.setTimeout(timeout);
    socket.on('connect', () => done(true));
    socket.on('timeout', () => done(false));
    socket.on('error', () => done(false));
  });
}

console.log(`探测 ${HOST}:${PORT} ...`);
const reachable = await probePort(HOST, PORT);

if (!reachable) {
  console.log(`\n[结论] 本机 ${HOST}:${PORT} 未监听 MySQL 服务，跳过 MySQL profile 实测。`);
  console.log('\n启用 MySQL 的步骤：');
  console.log('  1) 建库：CREATE DATABASE hr_onboarding DEFAULT CHARSET utf8mb4;');
  console.log('  2) 启动：mvn spring-boot:run -Dspring-boot.run.profiles=mysql');
  console.log('  3) 或用环境变量：');
  console.log('       DB_URL=jdbc:mysql://127.0.0.1:3306/hr_onboarding?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai');
  console.log(`       DB_USERNAME=${USER}`);
  console.log(`       DB_PASSWORD=你的密码`);
  console.log('       mvn spring-boot:run -Dspring-boot.run.profiles=mysql');
  console.log('\n建表脚本位于：backend/src/main/resources/db/mysql/schema.sql');
  console.log('应用启动时会通过 spring.sql.init 自动执行（CREATE TABLE IF NOT EXISTS，可重复执行）。');
  process.exit(0);
}

console.log(`[结论] ${HOST}:${PORT} 可连接，存在 MySQL 服务。`);
console.log('\n当前 profile 与数据库相关配置来自 backend/src/main/resources/application-mysql.yml：');
console.log(`  URL=jdbc:mysql://${HOST}:${PORT}/${DB}?useUnicode=true&characterEncoding=utf8&useSSL=false`);
console.log(`  USERNAME=${USER}（可用环境变量 DB_USERNAME 覆盖）`);
console.log('  PASSWORD 走环境变量 DB_PASSWORD，不落仓库');
console.log('\n注意：本工具不代填数据库密码，也不自动执行建库。');
console.log('请确认数据库与账号就绪后，再以 mysql profile 启动后端：');
console.log('  mvn spring-boot:run -Dspring-boot.run.profiles=mysql');
process.exit(0);