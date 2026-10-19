package com.company.hr.common;

import java.util.Map;

/** 业务异常：由 {@code GlobalExceptionHandler} 统一转换为 {@link Result} */
public class BusinessException extends RuntimeException {

  private final int code;

  public BusinessException(int code, String message) {
    super(message);
    this.code = code;
  }

  public int getCode() {
    return code;
  }

  /** 携带字段级错误的业务异常，用于把校验结果回填到前端控件 */
  public static class FieldValidationException extends BusinessException {

    private final transient Map<String, String> errors;

    public FieldValidationException(String message, Map<String, String> errors) {
      super(422, message);
      this.errors = errors;
    }

    public Map<String, String> getErrors() {
      return errors;
    }
  }
}