/**
 * 后端接口冒烟测试（Node 原生 fetch，无第三方依赖）
 * 用法： node scripts/smoke-test.mjs [BASE_URL]
 *
 * 注意：本脚本直连 127.0.0.1，不走系统代理，避免环境代理干扰返回 502。
 */
const BASE = process.argv[2] || 'http://127.0.0.1:9090';

// 每次运行使用唯一账号前缀，保证脚本可重复执行而不受历史数据影响
const RUN = Date.now().toString(36).slice(-6);
const acct = (name) => `${name}${RUN}`.slice(0, 20);

let pass = 0;
let fail = 0;

function check(label, actual, expected) {
  const ok = String(actual) === String(expected);
  if (ok) { pass += 1; console.log(`  PASS  ${label}`); }
  else { fail += 1; console.log(`  FAIL  ${label}  期望 ${expected}，实际 ${actual}`); }
}

function section(title) {
  console.log(`\n=== ${title} ===`);
}

async function call(method, path, { body, token } = {}) {
  const headers = { 'Content-Type': 'application/json' };
  if (token) headers.Authorization = `Bearer ${token}`;
  const res = await fetch(`${BASE}${path}`, {
    method,
    headers,
    body: body ? JSON.stringify(body) : undefined,
  });
  let payload = null;
  try { payload = await res.json(); } catch { /* 非 JSON 响应 */ }
  return { status: res.status, payload };
}

const validEmployee = (over = {}) => ({
  loginAccount: acct('zhangsan'),
  password: 'abc123',
  name: '张三',
  gender: '男',
  phone: '13800138000',
  email: 'zhangsan@example.com',
  ...over,
});

async function main() {
  console.log(`目标服务：${BASE}`);

  section('1. 管理员登录');
  const bad = await call('POST', '/api/auth/login', { body: { username: 'admin', password: 'wrong' } });
  check('错误密码返回 401', bad.status, 401);
  check('错误密码提示不区分账号/密码', bad.payload.message, '账号或密码错误');

  const login = await call('POST', '/api/auth/login', {
    body: { username: 'admin', password: 'admin123', remember: true },
  });
  check('正确登录返回 200', login.status, 200);
  check('正确登录 code=0', login.payload.code, 0);
  const token = login.payload?.data?.token;
  check('返回了 JWT token', Boolean(token), 'true');
  check('记住我时有效期为 7 天', login.payload?.data?.expiresIn, 604800);

  section('2. 鉴权');
  const noToken = await call('POST', '/api/employees', { body: validEmployee() });
  check('无 token 建号返回 401', noToken.status, 401);

  const forged = await call('GET', '/api/employees', { token: 'forged.token.value' });
  check('伪造 token 返回 401', forged.status, 401);

  const me = await call('GET', '/api/auth/me', { token });
  check('合法 token 可访问 /me', me.status, 200);
  check('/me 返回用户名', me.payload?.data?.username, 'admin');

  section('3. 建号校验（POST 传参）');
  const badPhone = await call('POST', '/api/employees', {
    token, body: validEmployee({ phone: '12345', email: 'bad-email' }),
  });
  check('非法手机号+Email 返回 422', badPhone.status, 422);
  check('返回手机号字段级错误', Boolean(badPhone.payload?.errors?.phone), 'true');
  check('返回 Email 字段级错误', Boolean(badPhone.payload?.errors?.email), 'true');

  const badGender = await call('POST', '/api/employees', { token, body: validEmployee({ gender: '未知' }) });
  check('性别非法值返回 422', badGender.status, 422);

  const weakPwd = await call('POST', '/api/employees', { token, body: validEmployee({ loginAccount: 'weakpwd', password: 'abcdef' }) });
  check('纯字母密码返回 422', weakPwd.status, 422);

  const badAccount = await call('POST', '/api/employees', { token, body: validEmployee({ loginAccount: '1abc' }) });
  check('账号数字开头返回 422', badAccount.status, 422);

  section('4. 正常建号');
  const MAIN_ACCOUNT = acct('zhangsan');
  const created = await call('POST', '/api/employees', { token, body: validEmployee({ loginAccount: MAIN_ACCOUNT }) });
  check('建号返回 201', created.status, 201);
  check('建号 code=0', created.payload.code, 0);
  check('回执含姓名', created.payload?.data?.name, '张三');
  check('回执不含密码字段', 'password' in (created.payload?.data || {}), 'false');

  section('5. 账号唯一性');
  const dup = await call('POST', '/api/employees', { token, body: validEmployee({ loginAccount: MAIN_ACCOUNT.toUpperCase(), name: '张三2', gender: '女' }) });
  check('大小写不同的重复账号被拒', dup.status === 422 || dup.status === 409, true);

  section('6. 灌入测试数据');
  const rows = [
    ['lisi', '李四', '女', '13912345678'],
    ['wangwu', '王五', '男', '13787654321'],
    ['zhaoliu', '赵六', '男', '13611112222'],
    ['sunqi', '孙七', '女', '13522223333'],
  ];
  for (const [key, name, gender, phone] of rows) {
    const account = acct(key);
    const r = await call('POST', '/api/employees', {
      token,
      body: { loginAccount: account, password: `${key}2026`, name, gender, phone, email: `${key}@company.com` },
    });
    console.log(`  ${account} (${name}) => HTTP ${r.status}`);
    if (r.status !== 201) { fail += 1; } else { pass += 1; }
  }
  // 本次运行共写入 5 条
  const EXPECTED_TOTAL = 5;
  // 姓名可能与历史数据重名，这里以本轮唯一账号前缀做断言基准
  const byRun = await call('GET', `/api/employees?page=1&pageSize=100&keyword=${RUN}`, { token });
  check('本轮写入 5 条账号', byRun.payload?.data?.total, EXPECTED_TOTAL);

  section('7. 员工列表（GET 传参）');
  const list = await call('GET', '/api/employees?page=1&pageSize=10', { token });
  check('列表返回 200', list.status, 200);
  check('列表非空', list.payload?.data?.list?.length > 0, true);
  const firstRow = list.payload?.data?.list?.[0] || {};
  check('列表行只含 id/loginAccount/name',
    Object.keys(firstRow).sort().join(','), 'id,loginAccount,name');
  check('按 id 升序', list.payload.data.list.map((r) => r.id).join(',') === [...list.payload.data.list.map((r) => r.id)].sort((a, b) => a - b).join(','), 'true');
  check('列表不含手机号字段', 'phone' in firstRow, 'false');
  check('列表不含性别字段', 'gender' in firstRow, 'false');

  const search = await call('GET', `/api/employees?page=1&pageSize=10&keyword=${encodeURIComponent(acct('lisi'))}`, { token });
  check('按账号搜索命中 1 条', search.payload?.data?.total, 1);
  check('搜索结果为李四', search.payload?.data?.list?.[0]?.name, '李四');

  const byName = await call('GET', `/api/employees?page=1&pageSize=10&keyword=${encodeURIComponent('王五')}`, { token });
  check('按姓名搜索命中至少 1 条', byName.payload?.data?.total >= 1, true);

  const noHit = await call('GET', '/api/employees?page=1&pageSize=10&keyword=zzz_not_exist_zzz', { token });
  check('无匹配时 total 为 0', noHit.payload?.data?.total, 0);
  check('无匹配时返回空数组', Array.isArray(noHit.payload?.data?.list) && noHit.payload.data.list.length === 0, true);

  const overflow = await call('GET', '/api/employees?page=999&pageSize=10', { token });
  check('页码越界回退到最后一页', overflow.payload?.data?.page, overflow.payload?.data?.totalPages);
  check('越界时仍有数据', overflow.payload?.data?.list?.length > 0, true);

  const capped = await call('GET', '/api/employees?page=1&pageSize=9999', { token });
  check('pageSize 被限制为 100', capped.payload?.data?.pageSize, 100);

  const badParam = await call('GET', '/api/employees?page=abc', { token });
  check('page 传非数字返回 400', badParam.status, 400);

  section('8. 方法与健壮性');
  const del = await call('DELETE', '/api/employees', { token });
  check('DELETE 返回 405', del.status, 405);

  const badJson = await fetch(`${BASE}/api/auth/login`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: '{不是合法JSON',
  });
  check('非法 JSON 返回 400', badJson.status, 400);

  const badJson2 = await call('POST', '/api/employees', { token, body: ['数组', '不是对象'] });
  check('数组 body 返回 400', badJson2.status, 400);

  console.log(`\n────────────────────────────`);
  console.log(`通过 ${pass}项，失败 ${fail} 项`);
  console.log(`────────────────────────────`);
  process.exit(fail > 0 ? 1 : 0);
}

main().catch((err) => {
  console.error('测试执行异常：', err);
  process.exit(1);
});