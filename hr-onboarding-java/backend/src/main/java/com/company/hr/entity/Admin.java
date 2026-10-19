package com.company.hr.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.time.LocalDateTime;
import lombok.Data;

/**
 * 管理员实体，对应 admin 表。
 *
 * <p>密码使用 BCrypt 哈希存储，实体本身永不返回给前端。
 */
@Data
@TableName("admin")
public class Admin {

  @TableId(type = IdType.ASSIGN_ID)
  private Long id;

  private String username;

  /** BCrypt 哈希，非明文 */
  private String passwordHash;

  private String displayName;

  @TableField(fill = FieldFill.INSERT)
  private LocalDateTime createdAt;
}