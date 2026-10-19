package com.company.hr.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * JWT 鉴权拦截器。
 *
 * <p>从 {@code Authorization: Bearer <token>} 提取 JWT，校验通过则把用户名与角色
 * 写入 request 属性供 Controller 使用；失败直接写 401 JSON，避免进入业务层。
 */
@Component
public class JwtAuthInterceptor implements HandlerInterceptor {

  public static final String ATTR_USERNAME = "authUsername";
  public static final String ATTR_ROLE = "authRole";

  private final JwtUtil jwtUtil;

  public JwtAuthInterceptor(JwtUtil jwtUtil) {
    this.jwtUtil = jwtUtil;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) throws IOException {
    // OPTIONS 预检直接放行
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      return true;
    }
    // 静态资源与文档类请求不鉴权
    if (!(handler instanceof HandlerMethod)) {
      return true;
    }

    String header = request.getHeader("Authorization");
    if (header == null || !header.startsWith("Bearer ")) {
      return unauthorized(response, "未提供登录凭证，请先登录");
    }

    String token = header.substring(7).trim();
    var claims = jwtUtil.parse(token);
    if (claims == null) {
      return unauthorized(response, "登录凭证无效或已过期，请重新登录");
    }

    request.setAttribute(ATTR_USERNAME, claims.getSubject());
    request.setAttribute(ATTR_ROLE, claims.get("role", String.class));
    return true;
  }

  private boolean unauthorized(HttpServletResponse response, String message) throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");
    response.getWriter().write(
        "{\"code\":401,\"message\":\"" + message + "\",\"data\":null}");
    return false;
  }
}