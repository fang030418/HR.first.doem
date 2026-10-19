-- ============================================================
-- SQLite 建表脚本（profile: sqlite，默认）
-- 主键由应用层雪花算法生成，因此无需 AUTOINCREMENT，两库结构保持一致
-- ============================================================

CREATE TABLE IF NOT EXISTS admin (
  id            INTEGER PRIMARY KEY,
  username      TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  display_name  TEXT,
  created_at    TEXT
);

CREATE TABLE IF NOT EXISTS employee (
  id            INTEGER PRIMARY KEY,
  login_account TEXT NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  name          TEXT NOT NULL,
  gender        TEXT NOT NULL,
  phone         TEXT NOT NULL,
  email         TEXT NOT NULL,
  created_at    TEXT
);

CREATE INDEX IF NOT EXISTS idx_employee_name ON employee (name);