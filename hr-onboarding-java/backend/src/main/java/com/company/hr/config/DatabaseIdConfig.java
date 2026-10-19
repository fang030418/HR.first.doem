package com.company.hr.config;

import java.sql.SQLException;
import javax.sql.DataSource;
import org.apache.ibatis.mapping.DatabaseIdProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 数据库标识提供器。
 *
 * <p>向 MyBatis 注入 {@code _databaseId} 参数（值为 {@code mysql} 或 {@code sqlite}），
 * 使同一条 SQL 内部可用 {@code <choose>} 对两种方言分派，
 * 例如 LIKE 的字符串拼接：MySQL 用 {@code CONCAT('%', ?, '%')}，SQLite 用 {@code '%' || ? || '%'}。
 */
@Configuration
public class DatabaseIdConfig {

  @Bean
  public DatabaseIdProvider databaseIdProvider() {
    return new DatabaseIdProvider() {
      @Override
      public String getDatabaseId(DataSource dataSource) {
        String product;
        try (var connection = dataSource.getConnection()) {
          product = connection.getMetaData().getDatabaseProductName().toLowerCase();
        } catch (SQLException ex) {
          // 保留原始 SQLException 作为 cause：连不上数据库通常是账号密码/库名/网络问题，
          // 若在此覆盖成「无法获取数据库产品名」，会把真实原因掩盖成误导性的方言问题
          throw new IllegalStateException(
              "无法连接数据库以识别类型，请检查 spring.datasource 配置"
                  + "（profile、URL、账号、密码、数据库是否已创建）。原始错误：" + ex.getMessage(),
              ex);
        }
        if (product.contains("mysql") || product.contains("mariadb")) {
          return "mysql";
        }
        if (product.contains("sqlite")) {
          return "sqlite";
        }
        throw new IllegalStateException(
            "不支持的数据库类型: " + product + "。当前仅支持 MySQL(MariaDB) 与 SQLite，"
                + "请通过 spring.profiles.active 指定 mysql 或 sqlite");
      }
    };
  }
}