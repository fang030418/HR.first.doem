-- ============================================================
-- MySQL 建表脚本（profile: mysql）
-- 字符集 utf8mb4 以完整支持中文与特殊字符
-- ============================================================

CREATE TABLE IF NOT EXISTS admin (
  id            BIGINT       NOT NULL COMMENT '雪花ID',
  username      VARCHAR(50)  NOT NULL COMMENT '登录账号',
  password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码哈希',
  display_name  VARCHAR(50)  DEFAULT NULL COMMENT '显示名',
  created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_admin_username (username)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '管理员';

CREATE TABLE IF NOT EXISTS employee (
  id            BIGINT       NOT NULL COMMENT '雪花ID',
  login_account VARCHAR(20)  NOT NULL COMMENT '登录账号',
  password_hash VARCHAR(100) NOT NULL COMMENT 'BCrypt 密码哈希',
  name          VARCHAR(20)  NOT NULL COMMENT '姓名',
  gender        VARCHAR(4)   NOT NULL COMMENT '性别：男/女',
  phone         VARCHAR(11)  NOT NULL COMMENT '手机号',
  email         VARCHAR(64)  NOT NULL COMMENT 'Email',
  created_at    DATETIME     DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_employee_account (login_account),
  KEY idx_employee_name (name)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_general_ci COMMENT '新员工';