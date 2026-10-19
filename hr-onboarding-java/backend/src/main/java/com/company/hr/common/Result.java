package com.company.hr.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一响应包装。
 *
 * <p>与前端 {@code public/js/common.js} 中的消费约定保持一致：
 * {@code code === 0} 为成功，非0 为业务/HTTP 错误。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> {

  private int code;
  private String message;
  private T data;

  public static <T> Result<T> ok(T data) {
    return new Result<>(0, "success", data);
  }

  public static <T> Result<T> ok(T data, String message) {
    return new Result<>(0, message, data);
  }

  public static <T> Result<T> fail(int code, String message) {
    return new Result<>(code, message, null);
  }
}