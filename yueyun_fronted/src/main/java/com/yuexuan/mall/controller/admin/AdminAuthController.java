package com.yuexuan.mall.controller.admin;

import cn.hutool.core.bean.BeanUtil;
import com.yuexuan.mall.common.R;
import com.yuexuan.mall.common.ResultCode;
import com.yuexuan.mall.entity.po.Admin;
import com.yuexuan.mall.security.CustomUserDetails;
import com.yuexuan.mall.security.JwtTokenProvider;
import com.yuexuan.mall.service.IAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 管理端 — 认证接口
 */
@Slf4j
@Tag(name = "管理端认证")
@RestController
@RequestMapping("/api/admin")
public class AdminAuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final IAdminService adminService;

    public AdminAuthController(AuthenticationManager authenticationManager,
                               JwtTokenProvider jwtTokenProvider,
                               IAdminService adminService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.adminService = adminService;
    }

    @Operation(summary = "管理员登录")
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
            String token = jwtTokenProvider.generateToken(
                    userDetails.getId(), userDetails.getUsername(), userDetails.getRole());

            // 更新最后登录时间
            Admin admin = adminService.getById(userDetails.getId());
            if (admin != null) {
                admin.setLastLoginTime(LocalDateTime.now());
                adminService.updateById(admin);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("tokenHead", "Bearer");

            Map<String, Object> adminInfo = new HashMap<>();
            adminInfo.put("id", userDetails.getId());
            adminInfo.put("username", userDetails.getUsername());
            adminInfo.put("realName", userDetails.getRealName());
            adminInfo.put("role", userDetails.getRole());
            data.put("adminInfo", adminInfo);

            return R.success(data);
        } catch (BadCredentialsException e) {
            return R.failed(ResultCode.PASSWORD_ERROR);
        } catch (DisabledException e) {
            return R.failed(ResultCode.USER_DISABLED);
        }
    }

    @Operation(summary = "获取当前管理员信息")
    @GetMapping("/info")
    public R<Map<String, Object>> info() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        Map<String, Object> info = BeanUtil.beanToMap(userDetails);
        info.remove("password");
        info.remove("authorities");
        info.remove("enabled");
        return R.success(info);
    }

    @Operation(summary = "登出")
    @PostMapping("/logout")
    public R<Void> logout() {
        // JWT 无状态，登出由前端清除 Token
        // 进阶可将 Token 加入 Redis 黑名单
        return R.success();
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
}
