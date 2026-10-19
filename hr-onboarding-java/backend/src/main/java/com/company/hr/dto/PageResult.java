package com.company.hr.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

/** 统一分页结果 */
@Data
@AllArgsConstructor
public class PageResult<T> {

  private List<T> list;
  private long total;
  private int page;
  private int pageSize;
  private int totalPages;
}