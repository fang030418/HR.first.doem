package com.company.hr.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 入职建号请求体（POST /api/employees）。
 *
 * <p>校验规则与前端 {@code frontend/src/views/EmployeeCreate.vue} 保持同源定义，
 * 服务端为最终防线。手机号与 Email 的格式在此处用注解做第一道拦截，
 * 错误信息由 {@code GlobalExceptionHandler} 转成字段级 errors 回填前端。
 */
@Data
public class EmployeeCreateRequest {

  /** 登录账号：字母开头，允许字母/数字/下划线/点/短横，4-20 位 */
  @NotBlank(message = "登录账号不能为空")
  @Pattern(regexp = "^[A-Za-z][A-Za-z0-9_.-]{3,19}$",
      message = "登录账号需以字母开头，仅含字母/数字/下划线/点/短横，长度 4-20 位")
  private String loginAccount;

  /** 密码：6-32 位且须同时含字母和数字 */
  @NotBlank(message = "密码不能为空")
  @Size(min = 6, max = 32, message = "密码长度需为 6-32 位")
  @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$", message = "密码需同时包含字母和数字")
  private String password;

  @NotBlank(message = "姓名不能为空")
  @Size(max = 20, message = "姓名长度不能超过 20 个字符")
  private String name;

  /** 性别：仅允许「男」或「女」，前端为 radio 单选 */
  @NotBlank(message = "请选择性别")
  @Pattern(regexp = "^(男|女)$", message = "性别只能为男或女")
  private String gender;

  /** 手机号：中国大陆 11 位 */
  @NotBlank(message = "手机号不能为空")
  @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确（应为 1 开头的 11 位数字）")
  private String phone;

  @NotBlank(message = "Email 不能为空")
  @Email(message = "Email 格式不正确，请检查形如 name@example.com")
  @Size(max = 64, message = "Email 长度不能超过 64 个字符")
  private String email;
}