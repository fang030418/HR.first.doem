/**
 * 前后端联调验证：通过前端 devServer(5173) 的 /api 代理访问后端(9090)，
 * 确认代理链路、CORS 与鉴权在真实前端路径下均工作。
 * 用法： node scripts/proxy-check.mjs
 */
const WEB = 'http://127.0.0.1:5173';

let pass = 0, fail = 0;
const check = (label, actual, expected) => {
  const ok = String(actual) === String(expected);
  console.log(`  ${ok ? 'PASS' : 'FAIL'}  ${label}${ok ? '' : `  期望 ${expected}，实际 ${actual}`}`);
  ok ? pass++ : fail++;
};

async function call(path, { method = 'GET', body, auth } = {}) {
  const headers = { 'Content-Type': 'application/json' };
  if (auth) headers['Authorization'] = `Bearer ${auth}`;
  const res = await fetch(`${WEB}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });
  let payload = null;
  try { payload = await res.json(); } catch { /* 非 JSON */ }
  return { status: res.status, payload };
}

const RUN = Date.now().toString(36).slice(-6);

async function main() {
  console.log(`经前端代理访问：${WEB}/api\n`);

  console.log('=== 1. 前端静态资源 ===');
  const home = await fetch(`${WEB}/`);
  check('GET / 返回 200', home.status, 200);
  const html = await home.text();
  check('首页挂载 Vue 应用容器', html.includes('id="app"'), 'true');

  console.log('\n=== 2. 代理链路 ===');
  const login = await call('/api/auth/login', {
    method: 'POST',
    body: { username: 'admin', password: 'admin123', remember: false },
  });
  check('经代理登录返回 200', login.status, 200);
  check('登录 code=0', login.payload?.code, 0);
  const jwt = login.payload?.data?.token;
  check('拿到 JWT', Boolean(jwt), 'true');

  const bad = await call('/api/auth/login', {
    method: 'POST',
    body: { username: 'admin', password: 'nope' },
  });
  check('经代理错误密码返回 401', bad.status, 401);

  console.log('\n=== 3. 鉴权经代理生效 ===');
  const noAuth = await call('/api/employees');
  check('无凭证访问列表返回 401', noAuth.status, 401);
  const badAuth = await call('/api/employees', { auth: 'forged.value.here' });
  check('伪造凭证返回 401', badAuth.status, 401);

  console.log('\n=== 4. 经代理建号（POST）===');
  const account = `proxy${RUN}`.slice(0, 20);
  const created = await call('/api/employees', {
    method: 'POST',
    auth: jwt,
    body: {
      loginAccount: account,
      password: 'proxy123',
      name: '代理测试',
      gender: '女',
      phone: '13700001234',
      email: `proxy${RUN}@company.com`,
    },
  });
  check('建号返回 201', created.status, 201);
  check('回执姓名正确', created.payload?.data?.name, '代理测试');

  const invalid = await call('/api/employees', {
    method: 'POST',
    auth: jwt,
    body: {
      loginAccount: `bad${RUN}`, password: 'proxy123', name: '校验',
      gender: '男', phone: '123', email: 'bad',
    },
  });
  check('非法手机号/Email 返回 422', invalid.status, 422);
  check('返回字段级错误', Boolean(invalid.payload?.errors?.phone && invalid.payload?.errors?.email), 'true');

  console.log('\n=== 5. 经代理查列表（GET）===');
  const list = await call(`/api/employees?page=1&pageSize=10&keyword=${account}`, { auth: jwt });
  check('列表返回 200', list.status, 200);
  check('命中新建账号', list.payload?.data?.list?.[0]?.loginAccount, account);
  const row = list.payload?.data?.list?.[0] || {};
  check('列表仅账号/姓名/id 三字段', Object.keys(row).sort().join(','), 'id,loginAccount,name');

  console.log(`\n────────────────────────────`);
  console.log(`通过 ${pass} 项，失败 ${fail} 项`);
  console.log(`────────────────────────────`);
  process.exit(fail > 0 ? 1 : 0);
}

main().catch((e) => { console.error('异常：', e); process.exit(1); });