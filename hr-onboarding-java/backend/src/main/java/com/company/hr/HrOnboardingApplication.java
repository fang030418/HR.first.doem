package com.company.hr;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 新员工入职管理 —— 后端启动类
 *
 * <p>数据库支持 MySQL 与 SQLite，通过 Spring Profile 切换：
 * <ul>
 *   <li>默认 sqlite：零依赖本地文件库，开箱即跑</li>
 *   <li>mysql：java -jar xxx.jar --spring.profiles.active=mysql</li>
 * </ul>
 */
@SpringBootApplication
@MapperScan("com.company.hr.mapper")
public class HrOnboardingApplication {

  public static void main(String[] args) {
    SpringApplication.run(HrOnboardingApplication.class, args);
  }
}