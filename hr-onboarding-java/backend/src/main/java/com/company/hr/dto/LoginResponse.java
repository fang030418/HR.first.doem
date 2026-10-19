package com.company.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 登录成功响应 */
@Data
@AllArgsConstructor
public class LoginResponse {

  private String token;

  /** token 有效期（秒），前端可用于安排刷新时机 */
  private long expiresIn;

  private String username;

  private String displayName;
}