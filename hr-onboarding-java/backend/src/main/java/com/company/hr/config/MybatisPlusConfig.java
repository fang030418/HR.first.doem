package com.company.hr.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** MyBatis-Plus 分页插件 */
@Configuration
public class MybatisPlusConfig {

  @Bean
  public MybatisPlusInterceptor mybatisPlusInterceptor() {
    MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
    PaginationInnerInterceptor pagination = new PaginationInnerInterceptor(DbType.SQLITE);
    // 单页上限，防止一次性拉全表
    pagination.setMaxLimit(100L);
    // 页码越界时不回到第一页，直接返回空集，由业务层决定是否回退
    pagination.setOverflow(false);
    interceptor.addInnerInterceptor(pagination);
    return interceptor;
  }
}