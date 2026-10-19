package com.company.hr.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JWT 签发与校验。
 *
 * <p>密钥来自配置 {@code hr.jwt.secret}，生产环境必须通过环境变量覆盖，
 * 不可使用默认值。有效期按「记住我」在2 小时与 7 天之间切换。
 */
@Component
public class JwtUtil {

  private final SecretKey key;
  private final long normalTtlSeconds;
  private final long rememberTtlSeconds;

  public JwtUtil(@Value("${hr.jwt.secret}") String secret,
      @Value("${hr.jwt.ttl-seconds:7200}") long normalTtlSeconds,
      @Value("${hr.jwt.remember-ttl-seconds:604800}") long rememberTtlSeconds) {
    byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
    if (keyBytes.length < 32) {
      throw new IllegalStateException("hr.jwt.secret 长度至少 32 字节（HS256 要求）");
    }
    this.key = Keys.hmacShaKeyFor(keyBytes);
    this.normalTtlSeconds = normalTtlSeconds;
    this.rememberTtlSeconds = rememberTtlSeconds;
  }

  public long ttlSeconds(boolean remember) {
    return remember ? rememberTtlSeconds : normalTtlSeconds;
  }

  public String generate(String username, String role, boolean remember) {
    long ttl = ttlSeconds(remember);
    Date now = new Date();
    return Jwts.builder()
        .subject(username)
        .claim("role", role)
        .issuedAt(now)
        .expiration(new Date(now.getTime() + ttl * 1000))
        .signWith(key)
        .compact();
  }

  /**
   * 解析并校验签名与过期时间。
   *
   * @return Claims，非法或过期时返回 null（由调用方转401）
   */
  public Claims parse(String token) {
    try {
      return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    } catch (JwtException | IllegalArgumentException ex) {
      return null;
    }
  }
}