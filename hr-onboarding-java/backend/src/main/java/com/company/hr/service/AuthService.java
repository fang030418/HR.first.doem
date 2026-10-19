package com.company.hr.service;

import com.company.hr.common.BusinessException;
import com.company.hr.config.JwtUtil;
import com.company.hr.dto.LoginRequest;
import com.company.hr.dto.LoginResponse;
import com.company.hr.entity.Admin;
import com.company.hr.mapper.AdminMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 管理员认证服务。
 *
 * <p>密码比对使用 BCrypt（自带随机盐，天然抗彩虹表）。
 * 登录失败时不区分「账号不存在」与「密码错误」，避免账号枚举。
 */
@Service
public class AuthService {

  private static final Logger log = LoggerFactory.getLogger(AuthService.class);

  private final AdminMapper adminMapper;
  private final JwtUtil jwtUtil;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public AuthService(AdminMapper adminMapper, JwtUtil jwtUtil) {
    this.adminMapper = adminMapper;
    this.jwtUtil = jwtUtil;
  }

  @Transactional
  public LoginResponse login(LoginRequest request) {
    Admin admin = adminMapper.findByUsername(request.getUsername());
    // 账号不存在与密码错误返回同一提示
    if (admin == null || !passwordEncoder.matches(request.getPassword(), admin.getPasswordHash())) {
      log.info("登录失败：账号 {}", request.getUsername());
      throw new BusinessException(401, "账号或密码错误");
    }

    String token = jwtUtil.generate(admin.getUsername(), "admin", request.isRemember());
    long ttl = jwtUtil.ttlSeconds(request.isRemember());

    log.info("登录成功：{} ", admin.getUsername());
    return new LoginResponse(token, ttl, admin.getUsername(), admin.getDisplayName());
  }
}