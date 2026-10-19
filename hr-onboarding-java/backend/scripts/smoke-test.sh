#!/usr/bin/env bash
# 后端接口冒烟测试：登录 / 建号 / 列表
# 用法： bash scripts/smoke-test.sh [BASE_URL]
BASE="${1:-http://127.0.0.1:9090}"
CURL="curl -s --noproxy *"

hr() { printf '\n=== %s ===\n' "$1"; }

hr "1. 错误密码登录（期望 401）"
$CURL -X POST "$BASE/api/auth/login" -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"wrong"}'

hr "2. 正确登录（期望 code=0且返回 token）"
RESP=$($CURL -X POST "$BASE/api/auth/login" -H 'Content-Type: application/json' \
  -d '{"username":"admin","password":"admin123","remember":true}')
echo "$RESP"
TOKEN=$(echo "$RESP" | sed -E 's/.*"token":"([^"]+)".*/\1/')
if [ -z "$TOKEN" ] || [ "$TOKEN" = "$RESP" ]; then
  echo ">>> 未能取到 token，后续用例中止"; exit 1
fi
echo ">>> token 已获取（前25字符: ${TOKEN:0:25}...）"

hr "3. 无 token 建号（期望 401）"
$CURL -X POST "$BASE/api/employees" -H 'Content-Type: application/json' \
  -d '{"loginAccount":"nobody"}'

hr "4. 非法手机号+Email（期望 422 且带字段级 errors）"
$CURL -X POST "$BASE/api/employees" -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"loginAccount":"zhangsan","password":"abc123","name":"张三","gender":"男","phone":"12345","email":"bad-email"}'

hr "5. 性别非法值（期望 422）"
$CURL -X POST "$BASE/api/employees" -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"loginAccount":"zhangsan","password":"abc123","name":"张三","gender":"未知","phone":"13800138000","email":"a@b.com"}'

hr "6. 弱密码 abc（期望 422，密码需含字母和数字）"
$CURL -X POST "$BASE/api/employees" -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"loginAccount":"weakpwd","password":"abcdef","name":"弱密码","gender":"女","phone":"13800138001","email":"weak@example.com"}'

hr "7. 正常建号（期望 201）"
$CURL -X POST "$BASE/api/employees" -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"loginAccount":"zhangsan","password":"abc123","name":"张三","gender":"男","phone":"13800138000","email":"zhangsan@example.com"}'

hr "8. 重复账号（大小写不敏感，期望 422）"
$CURL -X POST "$BASE/api/employees" -H 'Content-Type: application/json' \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"loginAccount":"ZHANGSAN","password":"abc123","name":"张三2","gender":"女","phone":"13900139000","email":"dup@example.com"}'

hr "9. 灌入其余测试数据"
for row in \
  'lisi|lisi2026|李四|女|13912345678|lisi@company.com' \
  'wangwu|wangwu2026|王五|男|13787654321|wangwu@company.com' \
  'zhaoliu|zhaoliu2026|赵六|男|13611112222|zhaoliu@company.com' \
  'sunqi|sunqi2026|孙七|女|13522223333|sunqi@company.com'; do
  IFS='|' read -r acct pwd name gender phone mail <<< "$row"
  code=$($CURL -o /dev/null -w '%{http_code}' -X POST "$BASE/api/employees" \
    -H 'Content-Type: application/json' -H "Authorization: Bearer $TOKEN" \
    -d "{\"loginAccount\":\"$acct\",\"password\":\"$pwd\",\"name\":\"$name\",\"gender\":\"$gender\",\"phone\":\"$phone\",\"email\":\"$mail\"}")
  printf '%s => HTTP %s\n' "$acct" "$code"
done

hr "10. GET 列表（期望仅 loginAccount / name 两列）"
$CURL "$BASE/api/employees?page=1&pageSize=10" -H "Authorization: Bearer $TOKEN"

hr "11. GET 列表带 keyword（期望仅李四）"
$CURL "$BASE/api/employees?page=1&pageSize=10&keyword=%E6%9D%8E" -H "Authorization: Bearer $TOKEN"

hr "12. GET 列表页码越界（期望回退到最后一页）"
$CURL "$BASE/api/employees?page=99&pageSize=10" -H "Authorization: Bearer $TOKEN"

hr "13. 方法不允许（期望 405）"
$CURL -X DELETE "$BASE/api/employees" -H "Authorization: Bearer $TOKEN"

hr "14. 伪造 token（期望 401）"
$CURL "$BASE/api/employees" -H "Authorization: Bearer forged.token.value"

printf '\n=== 冒烟测试结束 ===\n'