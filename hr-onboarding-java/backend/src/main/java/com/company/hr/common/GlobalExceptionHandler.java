package com.company.hr.common;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * 全局异常处理：把各类异常统一收敛为 {@link Result} 结构，
 * 避免把堆栈与SQL细节直接暴露给前端。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /** 业务异常（含字段级校验错误） */
  @ExceptionHandler(BusinessException.class)
  public ResponseEntity<Object> handleBusiness(BusinessException ex) {
    if (ex instanceof BusinessException.FieldValidationException fve) {
      return ResponseEntity.status(resolveStatus(ex.getCode()))
          .body(new ResultWithErrors(ex.getCode(), ex.getMessage(), fve.getErrors()));
    }
    return ResponseEntity.status(resolveStatus(ex.getCode()))
        .body(Result.fail(ex.getCode(), ex.getMessage()));
  }

  /** @Valid 校验失败：字段名转成驼峰，与前端表单字段对齐 */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Result<Void>> handleValid(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
      errors.put(toCamelCase(fieldError.getField()), fieldError.getDefaultMessage());
    }
    String firstMessage = errors.values().stream().findFirst().orElse("表单校验未通过");
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(new ResultWithErrors(422, firstMessage, errors));
  }

  /** 唯一索引冲突兜底：正常已在Service 层前置校验，这里防止并发穿透 */
  @ExceptionHandler(DuplicateKeyException.class)
  public ResponseEntity<Result<Void>> handleDuplicateKey(DuplicateKeyException ex) {
    log.warn("唯一约束冲突: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(new ResultWithErrors(409, "该登录账号已存在，请更换",
            Map.of("loginAccount", "登录账号已存在")));
  }

  /**
   * 请求体无法解析：非法 JSON、数组/标量而非对象、缺少 Content-Type 等。
   * Spring 默认会当成 500 上抛，这里显式收敛为 400。
   */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Result<Void>> handleUnreadable(HttpMessageNotReadableException ex) {
    log.warn("请求体解析失败: {}", ex.getMessage());
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Result.fail(400, "请求体不是合法的 JSON 对象"));
  }

  /** 请求方法不支持，例如对只注册了 GET/POST 的路径发 DELETE */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<Result<Void>> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex) {
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .body(Result.fail(405, "请求方法不被允许"));
  }

  /** 参数类型不匹配，例如 page 传了非数字 */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Result<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(Result.fail(400, "参数类型不正确：" + ex.getName()));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Result<Void>> handleOther(HttpServletRequest request, Exception ex) {
    log.error("未预期异常 {} {}", request.getMethod(), request.getRequestURI(), ex);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(Result.fail(500, "服务器内部错误"));
  }

  private int resolveStatus(int code) {
    return switch (code) {
      case 400, 401, 403, 404, 409, 422, 500 -> code;
      default -> 400;
    };
  }

  private String toCamelCase(String field) {
    if (field == null || field.isEmpty()) {
      return field;
    }
    StringBuilder sb = new StringBuilder();
    boolean upperNext = false;
    for (char c : field.toCharArray()) {
      if (c == '_') {
        upperNext = true;
      } else if (upperNext) {
        sb.append(Character.toUpperCase(c));
        upperNext = false;
      } else {
        sb.append(c);
      }
    }
    return sb.toString();
  }

  /** 携带字段错误的响应体（Result 不含 errors 字段，故用子类扩展） */
  public static class ResultWithErrors extends Result<Void> {
    private final Map<String, String> errors;

    public ResultWithErrors(int code, String message, Map<String, String> errors) {
      super(code, message, null);
      this.errors = errors;
    }

    public Map<String, String> getErrors() {
      return errors;
    }
  }
}