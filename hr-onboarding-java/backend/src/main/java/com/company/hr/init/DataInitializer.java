package com.company.hr.init;

import com.company.hr.entity.Admin;
import com.company.hr.mapper.AdminMapper;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 启动初始化：admin 表为空时写入默认管理员账号。
 * 只在空表时执行一次，重复启动不会覆盖已有账号。
 */
@Component
public class DataInitializer implements ApplicationRunner {

  private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

  private final AdminMapper adminMapper;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  @Value("${hr.admin.username:admin}")
  private String username;

  @Value("${hr.admin.password:admin123}")
  private String password;

  @Value("${hr.admin.display-name:系统管理员}")
  private String displayName;

  public DataInitializer(AdminMapper adminMapper) {
    this.adminMapper = adminMapper;
  }

  @Override
  public void run(ApplicationArguments args) {
    if (adminMapper.findByUsername(username) != null) {
      log.info("管理员账号已存在，跳过初始化");
      return;
    }
    Admin admin = new Admin();
    admin.setUsername(username);
    admin.setPasswordHash(passwordEncoder.encode(password));
    admin.setDisplayName(displayName);
    admin.setCreatedAt(LocalDateTime.now());
    adminMapper.insert(admin);
    log.info("已初始化管理员账号：{} / {}", username, password);
  }
}