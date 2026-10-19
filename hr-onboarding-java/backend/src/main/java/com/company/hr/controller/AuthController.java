package com.company.hr.controller;

import com.company.hr.common.Result;
import com.company.hr.dto.LoginRequest;
import com.company.hr.dto.LoginResponse;
import com.company.hr.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** 认证接口 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

  private final AuthService authService;

  public AuthController(AuthService authService) {
    this.authService = authService;
  }

  /** 管理员登录：POST 传参（JSON body） */
  @PostMapping("/login")
  public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
    return Result.ok(authService.login(request), "登录成功");
  }

  /** 会话校验，供前端路由守卫使用（需携带 JWT，由拦截器校验） */
  @GetMapping("/me")
  public Result<Object> me(HttpServletRequest request) {
    return Result.ok(java.util.Map.of(
        "username", request.getAttribute("authUsername"),
        "role", request.getAttribute("authRole")));
  }

  /** 退出登录：JWT 无状态，服务端无需处理，仅由前端清除 token */
  @PostMapping("/logout")
  public Result<Void> logout() {
    return Result.ok(null, "已退出登录");
  }
}