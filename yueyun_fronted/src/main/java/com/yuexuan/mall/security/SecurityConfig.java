package com.yuexuan.mall.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security 配置
 * - 无状态 Session（JWT 认证）
 * - 放行登录、Swagger、静态资源
 * - 使用 authority 方式检查角色（避免 hasRole 的 ROLE_ 前缀问题）
 */
@Configuration
@EnableWebSecurity
// 移除 @EnableMethodSecurity，改用 path-based 控制，避免 AOP 干扰
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF（前后端分离）
                .csrf(AbstractHttpConfigurer::disable)

                // 无状态 Session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // 请求放行规则
                .authorizeHttpRequests(auth -> auth
                        // 无需认证（公开接口）
                        .requestMatchers("/api/admin/login").permitAll()
                        .requestMatchers("/api/user/login", "/api/user/register").permitAll()
                        .requestMatchers("/api/product/**").permitAll()
                        .requestMatchers("/api/courier/login", "/api/courier/wx-login").permitAll()
                        // AI 客服接口：对话和热点问题公开，停止和会话管理需登录
                        .requestMatchers("/api/ai/chat", "/api/ai/hot-questions").permitAll()
                        .requestMatchers("/api/ai/**").authenticated()
                        // Swagger
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // 静态资源
                        .requestMatchers("/favicon.ico", "/error").permitAll()
                        // 管理员管理接口（仅超级管理员）
                        .requestMatchers("/api/admin/admin/**").hasAuthority("ROLE_SUPER_ADMIN")
                        // 其余管理端接口（ADMIN 或 SUPER_ADMIN 均可）
                        .requestMatchers("/api/admin/**").hasAnyAuthority("ROLE_ADMIN", "ROLE_SUPER_ADMIN")
                        // 用户端接口（需要普通用户角色）
                        .requestMatchers("/api/user/**").hasAuthority("ROLE_USER")
                        .requestMatchers("/api/cart/**").hasAuthority("ROLE_USER")
                        .requestMatchers("/api/address/**").hasAuthority("ROLE_USER")
                        .requestMatchers("/api/order/**").hasAuthority("ROLE_USER")
                        .requestMatchers("/api/payment/**").hasAuthority("ROLE_USER")
                        .requestMatchers("/api/review/**").hasAuthority("ROLE_USER")
                        // 配送员接口
                        .requestMatchers("/api/courier/**").hasAuthority("ROLE_COURIER")
                        // 其余接口需要登录
                        .anyRequest().authenticated()
                )

                // 异常处理
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType("application/json;charset=utf-8");
                            response.setStatus(401);
                            response.getWriter().write("{\"code\":401,\"msg\":\"未登录或Token已过期\",\"data\":null}");
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType("application/json;charset=utf-8");
                            response.setStatus(403);
                            response.getWriter().write("{\"code\":403,\"msg\":\"无权限访问\",\"data\":null}");
                        })
                )

                // 添加 JWT 过滤器
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
