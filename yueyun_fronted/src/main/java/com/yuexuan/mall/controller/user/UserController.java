package com.yuexuan.mall.controller.user;

import cn.hutool.core.util.StrUtil;
import com.yuexuan.mall.common.R;
import com.yuexuan.mall.common.ResultCode;
import com.yuexuan.mall.entity.po.User;
import com.yuexuan.mall.security.CustomUserDetails;
import com.yuexuan.mall.security.JwtTokenProvider;
import com.yuexuan.mall.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 用户端 — 用户认证与信息管理
 */
@Slf4j
@Tag(name = "用户端用户管理")
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final IUserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    public UserController(IUserService userService,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "用户登录（支持用户名或手机号）")
    @PostMapping("/login")
    public R<Map<String, Object>> login(@RequestBody LoginRequest request) {
        if (StrUtil.isBlank(request.getUsername()) || StrUtil.isBlank(request.getPassword())) {
            return R.failed("用户名和密码不能为空");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // 确保登录的是普通用户角色
            if (!"USER".equals(userDetails.getRole())) {
                return R.failed("请使用用户端登录入口");
            }

            String token = jwtTokenProvider.generateToken(
                    userDetails.getId(), userDetails.getUsername(), userDetails.getRole());

            // 更新最后登录时间
            try {
                User loginUser = userService.getById(userDetails.getId());
                if (loginUser != null) {
                    loginUser.setLastLoginTime(LocalDateTime.now());
                    userService.updateById(loginUser);
                }
            } catch (Exception ignored) { }

            Map<String, Object> data = new HashMap<>();
            data.put("token", token);
            data.put("tokenHead", "Bearer");

            // 获取完整的用户信息返回
            User fullUser = userService.getById(userDetails.getId());
            Map<String, Object> userInfo = new HashMap<>();
            if (fullUser != null) {
                userInfo.put("id", fullUser.getId());
                userInfo.put("username", fullUser.getUsername());
                userInfo.put("nickname", fullUser.getNickname());
                userInfo.put("avatar", fullUser.getAvatar());
                userInfo.put("phone", fullUser.getPhone());
                userInfo.put("email", fullUser.getEmail());
                userInfo.put("gender", fullUser.getGender());
            } else {
                userInfo.put("id", userDetails.getId());
                userInfo.put("username", userDetails.getUsername());
            }
            data.put("userInfo", userInfo);

            return R.success(data);

        } catch (BadCredentialsException e) {
            return R.failed(ResultCode.PASSWORD_ERROR);
        } catch (DisabledException e) {
            return R.failed(ResultCode.USER_DISABLED);
        }
    }

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public R<Void> register(@RequestBody RegisterRequest request) {
        if (StrUtil.isBlank(request.getUsername()) || StrUtil.isBlank(request.getPassword())) {
            return R.failed("用户名和密码不能为空");
        }
        long count = userService.lambdaQuery()
                .eq(User::getUsername, request.getUsername())
                .count();
        if (count > 0) {
            return R.failed(ResultCode.USERNAME_EXISTS);
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setGender(request.getGender() != null ? request.getGender() : 0);
        user.setStatus(1);
        user.setCreateBy(0L);
        userService.save(user);

        log.info("新用户注册成功: username={}", request.getUsername());
        return R.success("注册成功");
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/info")
    public R<Map<String, Object>> info() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        User user = userService.getById(userDetails.getId());
        if (user == null) {
            return R.failed("用户不存在");
        }
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("nickname", user.getNickname());
        info.put("avatar", user.getAvatar());
        info.put("phone", user.getPhone());
        info.put("email", user.getEmail());
        info.put("gender", user.getGender());
        info.put("birthday", user.getBirthday());
        info.put("status", user.getStatus());
        return R.success(info);
    }

    @Operation(summary = "更新个人信息")
    @PutMapping("/update")
    public R<Void> update(@RequestBody User user) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        User existing = userService.getById(userDetails.getId());
        if (existing == null) {
            return R.failed("用户不存在");
        }
        if (StrUtil.isNotBlank(user.getNickname())) existing.setNickname(user.getNickname());
        if (StrUtil.isNotBlank(user.getPhone())) existing.setPhone(user.getPhone());
        if (StrUtil.isNotBlank(user.getEmail())) existing.setEmail(user.getEmail());
        if (user.getGender() != null) existing.setGender(user.getGender());
        if (user.getBirthday() != null) existing.setBirthday(user.getBirthday());
        userService.updateById(existing);
        return R.success("修改成功");
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public R<Void> password(@RequestBody PasswordRequest request) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();

        User user = userService.getById(userDetails.getId());
        if (user == null) {
            return R.failed("用户不存在");
        }

        // 验证原密码
        if (!passwordEncoder.matches(
                request.getOldPassword(), user.getPassword())) {
            return R.failed("原密码错误");
        }
        if (StrUtil.isBlank(request.getNewPassword())) {
            return R.failed("新密码不能为空");
        }
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.updateById(user);
        return R.success("密码修改成功");
    }

    // ─── DTO 类 ───

    public static class LoginRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class RegisterRequest {
        private String username;
        private String password;
        private String nickname;
        private String phone;
        private String email;
        private Integer gender;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public Integer getGender() { return gender; }
        public void setGender(Integer gender) { this.gender = gender; }
    }

    public static class PasswordRequest {
        private String oldPassword;
        private String newPassword;
        public String getOldPassword() { return oldPassword; }
        public void setOldPassword(String oldPassword) { this.oldPassword = oldPassword; }
        public String getNewPassword() { return newPassword; }
        public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    }
}
