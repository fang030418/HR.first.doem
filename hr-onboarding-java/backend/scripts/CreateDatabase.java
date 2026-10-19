import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

/**
 * 一次性工具：确保 hr_onboarding 数据库存在。
 *
 * 用法： java -cp <mysql-connector-j.jar> CreateDatabase.java
 * 凭证从环境变量 DB_USERNAME / DB_PASSWORD 读取，不落在源码里。
 */
public class CreateDatabase {

  private static final String HOST = "127.0.0.1";
  private static final int PORT = 3306;
  private static final String DB = "hr_onboarding";

  public static void main(String[] args) throws Exception {
    String user = System.getenv().getOrDefault("DB_USERNAME", "root");
    String pass = System.getenv().getOrDefault("DB_PASSWORD", "");

    // 先连到 mysql 系统库，确认凭证有效
    String baseUrl = "jdbc:mysql://" + HOST + ":" + PORT + "/mysql"
        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai";

    Properties props = new Properties();
    props.setProperty("user", user);
    props.setProperty("password", pass);

    System.out.println("连接 MySQL: " + HOST + ":" + PORT + " (user=" + user + ")");
    try (Connection conn = DriverManager.getConnection(baseUrl, props);
         Statement stmt = conn.createStatement()) {

      // 确认服务可用并打印版本，便于排查方言差异
      try (ResultSet rs = stmt.executeQuery("SELECT VERSION()")) {
        rs.next();
        System.out.println("MySQL 版本: " + rs.getString(1));
      }

      // utf8mb4 以完整支持中文与特殊字符
      stmt.executeUpdate(
          "CREATE DATABASE IF NOT EXISTS `" + DB + "` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci");
      System.out.println("数据库 `" + DB + "` 已就绪（utf8mb4）");
    }
  }
}