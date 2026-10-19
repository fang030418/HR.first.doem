package com.company.hr.service;

import com.company.hr.common.BusinessException;
import com.company.hr.dto.EmployeeCreateRequest;
import com.company.hr.dto.EmployeeCreatedResponse;
import com.company.hr.dto.PageResult;
import com.company.hr.dto.EmployeeBriefVO;
import com.company.hr.entity.Employee;
import com.company.hr.mapper.EmployeeMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 新员工入职业务。
 *
 * <p>入职建号统一使用 BCrypt 存储密码；账号唯一性在Service 层前置校验，
 * DB 唯一索引作为并发场景下的最终兜底（撞索引由全局异常处理器转 409）。
 */
@Service
public class EmployeeService {

  private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
  private static final int MAX_PAGE_SIZE = 100;

  private final EmployeeMapper employeeMapper;
  private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

  public EmployeeService(EmployeeMapper employeeMapper) {
    this.employeeMapper = employeeMapper;
  }

  /** 入职建号（POST /api/employees） */
  @Transactional
  public EmployeeCreatedResponse create(EmployeeCreateRequest request) {
    String account = request.getLoginAccount().trim();

    // 账号唯一性前置校验，避免直接撞唯一索引抛出底层 SQL 异常
    if (employeeMapper.countByAccount(account) > 0) {
      throw new BusinessException.FieldValidationException(
          "该登录账号已存在，请更换",
          java.util.Map.of("loginAccount", "登录账号已存在"));
    }

    Employee employee = new Employee();
    employee.setLoginAccount(account);
    // 密码加密后落库，明文不进入数据层
    employee.setPasswordHash(passwordEncoder.encode(request.getPassword()));
    employee.setName(request.getName().trim());
    employee.setGender(request.getGender());
    employee.setPhone(request.getPhone().trim());
    employee.setEmail(request.getEmail().trim());
    employee.setCreatedAt(LocalDateTime.now());

    try {
      employeeMapper.insert(employee);
    } catch (DuplicateKeyException ex) {
      // 并发下另一个请求刚插入同一账号
      throw new BusinessException.FieldValidationException(
          "该登录账号已存在，请更换",
          java.util.Map.of("loginAccount", "登录账号已存在"));
    }

    log.info("新员工账号创建成功：{} ({})", employee.getLoginAccount(), employee.getName());
    return new EmployeeCreatedResponse(
        employee.getId(),
        employee.getLoginAccount(),
        employee.getName(),
        employee.getGender(),
        employee.getPhone(),
        employee.getEmail(),
        employee.getCreatedAt());
  }

  /** 员工列表（GET /api/employees），仅返回列表所需的账号与姓名 */
  public PageResult<EmployeeBriefVO> page(int page, int pageSize, String keyword) {
    int safePage = Math.max(page, 1);
    int safeSize = Math.min(Math.max(pageSize, 1), MAX_PAGE_SIZE);
    String kw = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

    long total = employeeMapper.countByKeyword(kw);
    long offset = (long) (safePage - 1) * safeSize;

    // 删除末页最后一条导致页码越界时，回退到最后一页而不是返回空列表
    int totalPages = (int) Math.max(1, Math.ceil(total / (double) safeSize));
    if (safePage > totalPages) {
      safePage = totalPages;
      offset = (long) (safePage - 1) * safeSize;
    }

    List<Employee> rows = employeeMapper.selectBriefPage(kw, safeSize, offset);
    List<EmployeeBriefVO> list = rows.stream()
        .map(e -> new EmployeeBriefVO(e.getId(), e.getLoginAccount(), e.getName()))
        .toList();

    return new PageResult<>(list, total, safePage, safeSize, totalPages);
  }
}