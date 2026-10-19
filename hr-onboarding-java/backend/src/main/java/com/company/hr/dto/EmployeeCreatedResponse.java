package com.company.hr.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

/** 建号成功后的回执（不含任何密码信息） */
@Data
@AllArgsConstructor
public class EmployeeCreatedResponse {

  private Long id;
  private String loginAccount;
  private String name;
  private String gender;
  private String phone;
  private String email;
  private LocalDateTime createdAt;
}