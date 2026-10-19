package com.company.hr.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 层配置：注册鉴权拦截器与跨域策略。
 *
 * <p>前后端分离部署，前端跑在 Vite devServer（默认 5173），
 * 通过 CORS 放行；生产同域部署时把 allowedOriginPatterns 收紧即可。
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

  private final JwtAuthInterceptor jwtAuthInterceptor;

  public WebConfig(JwtAuthInterceptor jwtAuthInterceptor) {
    this.jwtAuthInterceptor = jwtAuthInterceptor;
  }

  @Override
  public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(jwtAuthInterceptor)
        .addPathPatterns("/api/**")
        .excludePathPatterns(
            "/api/auth/login",
            "/api/auth/captcha");
  }

  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/api/**")
        .allowedOriginPatterns("*")
        .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
        .allowedHeaders("*")
        .exposedHeaders("Content-Disposition")
        .allowCredentials(true)
        .maxAge(3600);
  }
}