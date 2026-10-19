package com.company.hr.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.company.hr.entity.Employee;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 员工 Mapper。
 *
 * <p><b>双库方言适配</b>：字符串拼接在 MySQL 用 {@code CONCAT('%', ?, '%')}，
 * 在 SQLite 用 {@code '%' || ? || '%'}。这里通过 MyBatis 内置的 {@code _databaseId}
 * 参数（由 {@code DatabaseIdProvider} 在运行时注入）在同一条 SQL 内分派，
 * 从而让同一份 Mapper 同时服务 MySQL 与 SQLite，无需拆分 XML。
 *
 * <p>所有过滤条件均下推到 SQL，不做「全表捞出后在应用层过滤」。
 */
@Mapper
public interface EmployeeMapper extends BaseMapper<Employee> {

  /** LIKE 条件片段：按 _databaseId 选择字符串拼接函数 */
  String LIKE_FRAGMENT = """
      <choose>
        <when test="_databaseId == 'mysql'">
          AND (login_account LIKE CONCAT('%', #{keyword}, '%') OR name LIKE CONCAT('%', #{keyword}, '%'))
        </when>
        <otherwise>
          AND (login_account LIKE '%' || #{keyword} || '%' OR name LIKE '%' || #{keyword} || '%')
        </otherwise>
      </choose>
      """;

  /**
   * 分页查询员工，只 SELECT 列表展示需要的「登录账号 / 姓名」两列。
   *
   * <p>LIMIT/OFFSET 是标准 SQL，MySQL 与 SQLite 均支持。
   */
  @Select("""
      <script>
      SELECT id, login_account, name
      FROM employee
      <where>
        <if test="keyword != null and keyword != ''">
          """ + LIKE_FRAGMENT + """
        </if>
      </where>
      ORDER BY id ASC
      LIMIT #{limit} OFFSET #{offset}
      </script>
      """)
  List<Employee> selectBriefPage(@Param("keyword") String keyword,
                                 @Param("limit") long limit,
                                 @Param("offset") long offset);

  /** 统计符合过滤条件的总数，供分页计算 */
  @Select("""
      <script>
      SELECT COUNT(*) FROM employee
      <where>
        <if test="keyword != null and keyword != ''">
          """ + LIKE_FRAGMENT + """
        </if>
      </where>
      </script>
      """)
  long countByKeyword(@Param("keyword") String keyword);

  /** 账号是否已存在（大小写不敏感），唯一性前置校验，避免撞唯一索引抛底层异常 */
  @Select("SELECT COUNT(*) FROM employee WHERE LOWER(login_account) = LOWER(#{loginAccount})")
  long countByAccount(@Param("loginAccount") String loginAccount);
}