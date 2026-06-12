package com.yuexuan.mall.security;

import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Date;

/**
 * JWT Token 提供者
 * - 签发 Token（有效期 3 小时）
 * - 解析 Token
 * - 校验 Token
 * - 密钥通过 SHA-256 哈希确保长度 ≥ 256 位
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    @Value("${jwt.token-head: Bearer}")
    private String tokenHead;

    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        // 将任意长度的密钥哈希为 256 位，确保满足 HMAC-SHA256 要求
        this.secretKey = generateKey(secret);
        log.info("JWT 密钥初始化完成，有效期: {}ms ({}小时)", expiration, expiration / 3600000);
    }

    /**
     * 通过 SHA-256 将任意字符串转换为 256 位密钥
     */
    private SecretKey generateKey(String secret) {
        try {
            MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha256.digest(secret.getBytes(StandardCharsets.UTF_8));
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.warn("SHA-256 哈希失败，使用原始密钥（长度需 ≥ 32 字节）");
            byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
            if (keyBytes.length < 32) {
                keyBytes = Arrays.copyOf(keyBytes, 32);
            }
            return Keys.hmacShaKeyFor(keyBytes);
        }
    }

    /**
     * 生成 Token（有效期 3 小时）
     */
    public String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(username)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(secretKey)
                .compact();
    }

    /**
     * 从 Token 中提取用户名
     */
    public String getUsername(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 从 Token 中提取用户 ID
     */
    public Long getUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    /**
     * 从 Token 中提取角色
     */
    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    /**
     * 校验 Token 是否有效
     */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT 校验失败: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 从请求头中提取 Token（去掉 "Bearer " 前缀）
     */
    public String resolveToken(String header) {
        if (StrUtil.isNotBlank(header) && header.startsWith(tokenHead)) {
            return header.substring(tokenHead.length()).trim();
        }
        return null;
    }

    /**
     * 解析 JWT Claims
     */
    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
