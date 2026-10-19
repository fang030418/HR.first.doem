import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

/** 校验数据确实写入 MySQL，并检查中文字符集是否正确。 */
public class VerifyData {

  public static void main(String[] args) throws Exception {
    String user = System.getenv().getOrDefault("DB_USERNAME", "root");
    String pass = System.getenv().getOrDefault("DB_PASSWORD", "");

    String url = "jdbc:mysql://127.0.0.1:3306/hr_onboarding"
        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai"
        + "&characterEncoding=utf8";

    Properties props = new Properties();
    props.setProperty("user", user);
    props.setProperty("password", pass);

    try (Connection conn = DriverManager.getConnection(url, props);
         Statement stmt = conn.createStatement()) {

      try (ResultSet rs = stmt.executeQuery(
          "SELECT COUNT(*) FROM employee")) {
        rs.next();
        System.out.println("employee 表记录数: " + rs.getInt(1));
      }

      try (ResultSet rs = stmt.executeQuery(
          "SELECT login_account, name, gender, phone, email FROM employee ORDER BY id LIMIT 8")) {
        System.out.println("--- 前 8 条（验证中文与字段完整性）---");
        while (rs.next()) {
          System.out.printf("%-22s | %-8s | %-4s | %-13s | %s%n",
              rs.getString(1), rs.getString(2), rs.getString(3),
              rs.getString(4), rs.getString(5));
        }
      }

      // 密码必须以 BCrypt 哈希存储，绝不能是明文
      try (ResultSet rs = stmt.executeQuery(
          "SELECT password_hash FROM employee LIMIT 1")) {
        rs.next();
        String hash = rs.getString(1);
        boolean bcrypt = hash != null
            && (hash.startsWith("$2a$") || hash.startsWith("$2b$"));
        System.out.println("--- 密码存储检查 ---");
        System.out.println("是 BCrypt 哈希: " + bcrypt);
        System.out.println("疑似明文(长度<20): " + (hash != null && hash.length() < 20));
      }

      try (ResultSet rs = stmt.executeQuery(
          "SELECT username FROM admin")) {
        while (rs.next()) {
          System.out.println("管理员账号: " + rs.getString(1));
        }
      }
    }
  }
}