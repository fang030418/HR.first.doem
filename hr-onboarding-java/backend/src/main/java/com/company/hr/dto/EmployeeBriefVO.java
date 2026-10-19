package com.company.hr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/** 列表行VO：按需求仅暴露「登录账号」「姓名」两列所需字段 */
@Data
@AllArgsConstructor
public class EmployeeBriefVO {

  private Long id;
  private String loginAccount;
  private String name;
}