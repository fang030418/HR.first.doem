package com.company.hr.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 新员工实体，对应 employee 表。
 *
 * <p>主键策略说明：MySQL 与 SQLite 的自增主键语法与取值方式不一致
 * （MySQL 用 AUTO_INCREMENT，SQLite 用 AUTOINCREMENT 且仅允许 INTEGER PRIMARY KEY）。
 * 因此统一采用 MyBatis-Plus 的 {@code ASSIGN_ID} 雪花算法在应用层生成主键，
 * 使 DDL 与插入语句在两种数据库下完全一致，无需方言分支。
 */
@Data
@TableName("employee")
public class Employee {

  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  /** 登录账号，唯一 */
  private String loginAccount;

  /** BCrypt 哈希，非明文 */
  private String passwordHash;

  /** 姓名 */
  private String name;

  /** 性别：男 / 女 */
  private String gender;

  /** 手机号 */
  private String phone;

  /** Email */
  private String email;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;
}