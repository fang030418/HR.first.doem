package com.company.hr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.company.hr.entity.Admin;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface AdminMapper extends BaseMapper<Admin> {

  /** 按账号查询管理员，避免全表扫描 */
  @Select("SELECT id, username, password_hash, display_name, created_at FROM admin WHERE username = #{username}")
  Admin findByUsername(@Param("username") String username);
}