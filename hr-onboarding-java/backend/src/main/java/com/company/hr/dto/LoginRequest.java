package com.company.hr.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 管理员登录请求体（POST /api/auth/login） */
@Data
public class LoginRequest {

  @NotBlank(message = "请输入账号")
  @Size(max = 50, message = "账号长度超出限制")
  private String username;

  @NotBlank(message = "请输入密码")
  @Size(max = 128, message = "密码长度超出限制")
  private String password;

  /** 是否记住我：延长 JWT 有效期（默认 2 小时 → 7 天） */
  private boolean remember = false;
}