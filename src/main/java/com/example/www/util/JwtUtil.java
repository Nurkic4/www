package com.example.www.util;

// 引入JWT相关类
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Date;

/**
 * JWT工具类，用于生成和解析Token
 */
public class JwtUtil {
    // 签名密钥
    private static final String SECRET_KEY = "mySecretKey123456";
    // Token有效期（毫秒），这里为1天
    private static final long EXPIRATION = 86400000; // 1天

    /**
     * 生成JWT Token（包含用户ID和用户名）
     * @param userId 用户ID
     * @param username 用户名
     * @return 生成的Token字符串
     */
    public static String generateToken(Long userId, String username) {
        return Jwts.builder()
                .setSubject(username) // 设置主题为用户名
                .claim("userId", userId) // 添加用户ID
                .setIssuedAt(new Date()) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION)) // 过期时间
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // 签名算法和密钥
                .compact();
    }

    /**
     * 生成JWT Token（兼容旧版本，只包含用户名）
     * @param username 用户名
     * @return 生成的Token字符串
     */
    public static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username) // 设置主题为用户名
                .setIssuedAt(new Date()) // 签发时间
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION)) // 过期时间
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY) // 签名算法和密钥
                .compact();
    }

    /**
     * 从Token中解析用户名
     * @param token JWT Token
     * @return 用户名
     */
    public static String getUsernameFromToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY) // 设置签名密钥
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject(); // 获取主题（用户名）
    }

    /**
     * 从Token中解析用户ID
     * @param token JWT Token
     * @return 用户ID，如果token中没有用户ID则返回null
     */
    public static Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .setSigningKey(SECRET_KEY) // 设置签名密钥
                    .parseClaimsJws(token)
                    .getBody();
            return claims.get("userId", Long.class); // 获取用户ID
        } catch (Exception e) {
            // 如果token中没有用户ID（旧版本token），返回null
            return null;
        }
    }

    /**
     * 检查Token是否包含用户ID
     * @param token JWT Token
     * @return 是否包含用户ID
     */
    public static boolean hasUserId(String token) {
        return getUserIdFromToken(token) != null;
    }
} 