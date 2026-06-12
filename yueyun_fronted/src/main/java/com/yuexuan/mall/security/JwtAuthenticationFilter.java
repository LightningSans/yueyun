package com.yuexuan.mall.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 认证过滤器
 * 从请求头中提取 Token，校验并设置 SecurityContext
 * 认证失败时直接返回 401，不再继续请求链
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestURI = request.getRequestURI();
        // 登录/注册接口直接放行
        if (requestURI.equals("/api/admin/login") || requestURI.equals("/api/user/login")
                || requestURI.equals("/api/user/register")
                || requestURI.equals("/api/courier/login")
                || requestURI.equals("/api/courier/wx-login")
                || requestURI.equals("/api/ai/chat")
                || requestURI.equals("/api/ai/hot-questions")) {
            filterChain.doFilter(request, response);
            return;
        }

        String header = request.getHeader("Authorization");
        String token = jwtTokenProvider.resolveToken(header);

        if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtTokenProvider.validateToken(token)) {
                try {
                    // 从 Token 中直接提取用户名和角色，无需查库
                    String username = jwtTokenProvider.getUsername(token);
                    String role = jwtTokenProvider.getRole(token);
                    Long userId = jwtTokenProvider.getUserId(token);

                    CustomUserDetails userDetails = new CustomUserDetails(
                            userId, username, "", role, username, true);

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails, null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    log.debug("JWT 认证成功: username={}, role={}", username, role);
                } catch (Exception e) {
                    log.warn("JWT 认证失败: {}", e.getMessage());
                    writeUnauthorized(response, "用户信息无效");
                    return;
                }
            } else {
                log.warn("JWT Token 无效或已过期");
                writeUnauthorized(response, "Token 无效或已过期");
                return;
            }
        } else if (token == null && !requestURI.startsWith("/api/")) {
            // 非 API 请求（如静态资源）直接放行
            filterChain.doFilter(request, response);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws IOException {
        response.setContentType("application/json;charset=utf-8");
        response.setStatus(401);
        response.getWriter().write("{\"code\":401,\"msg\":\"" + msg + "\",\"data\":null}");
    }
}
