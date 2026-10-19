package com.company.hr.controller;

import com.company.hr.common.Result;
import com.company.hr.dto.EmployeeBriefVO;
import com.company.hr.dto.EmployeeCreateRequest;
import com.company.hr.dto.EmployeeCreatedResponse;
import com.company.hr.dto.PageResult;
import com.company.hr.service.EmployeeService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 新员工接口。
 *
 * <p>建号用 <b>POST</b>（JSON body），列表用 <b>GET</b>（query string）。
 */
@RestController
@RequestMapping("/api/employees")
public class EmployeeController {

  private final EmployeeService employeeService;

  public EmployeeController(EmployeeService employeeService) {
    this.employeeService = employeeService;
  }

  /** 入职新建账号：POST /api/employees */
  @PostMapping
  public ResponseEntity<Result<EmployeeCreatedResponse>> create(
      @Valid @RequestBody EmployeeCreateRequest request) {
    EmployeeCreatedResponse created = employeeService.create(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(Result.ok(created, "新员工账号创建成功"));
  }

  /** 新员工列表：GET /api/employees?page=&pageSize=&keyword= */
  @GetMapping
  public Result<PageResult<EmployeeBriefVO>> list(
      @RequestParam(defaultValue = "1") int page,
      @RequestParam(defaultValue = "10") int pageSize,
      @RequestParam(required = false) String keyword) {
    return Result.ok(employeeService.page(page, pageSize, keyword));
  }
}