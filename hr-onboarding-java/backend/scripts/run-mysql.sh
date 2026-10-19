#!/usr/bin/env bash
# 以 mysql profile 启动后端，并从 .env.local 读取数据库凭证。
# 凭证不写进命令行，避免出现在进程列表与终端历史里。
#
# 用法： bash scripts/run-mysql.sh
set -euo pipefail

cd "$(dirname "$0")/.."

if [ ! -f .env.local ]; then
  echo "缺少 .env.local，请先创建并填入 DB_URL / DB_USERNAME / DB_PASSWORD"
  exit 1
fi

set -a
# shellcheck disable=SC1091
source .env.local
set +a

echo "已加载 .env.local"
echo "启动 profile=mysql，端口 9091（SQLite 实例跑在 9090，互不冲突）"
exec mvn -B spring-boot:run \
  -Dspring-boot.run.profiles=mysql \
  -Dspring-boot.run.arguments=--server.port=9091