package com.yuexuan.mall.controller.courier;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexuan.mall.common.R;
import com.yuexuan.mall.entity.po.Courier;
import com.yuexuan.mall.security.CustomUserDetails;
import com.yuexuan.mall.security.JwtTokenProvider;
import com.yuexuan.mall.service.ICourierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 配送员端 — 认证接口
 * 支持两种登录方式：
 * 1. 用户名+密码登录（开发测试用）
 * 2. 微信 code 登录（微信小程序 wx.login → code → openid）
 */
@Slf4j
@Tag(name = "配送员端认证")
@RestController
@RequestMapping("/api/courier")
public class CourierAuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final ICourierService courierService;

    @Value("${wechat.mini-program.app-id:}")
    private String wechatAppId;

    @Value("${wechat.mini-program.app-secret:}")
    private String wechatAppSecret;

    public CourierAuthController(AuthenticationManager authenticationManager,
                                 JwtTokenProvider jwtTokenProvider,
                                 ICourierService courierService) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.courierService = courierService;
    }

    // ═══════════════════════════════════════════
    //  方式一：用户名 + 密码登录（开发测试用）
    // ═══════════════════════════════════════════

    @Operation(summary = "配送员登录（用户名+密码）")
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

            if (!"COURIER".equals(userDetails.getRole())) {
                return R.failed("请使用配送员端登录入口");
            }

            String token = jwtTokenProvider.generateToken(
                    userDetails.getId(), userDetails.getUsername(), userDetails.getRole());

            return R.success(buildCourierData(token, userDetails));

        } catch (BadCredentialsException e) {
            return R.failed(4003, "密码错误");
        } catch (DisabledException e) {
            return R.failed(4001, "账户已被禁用");
        }
    }

    // ═══════════════════════════════════════════
    //  方式二：微信 code 登录（微信小程序生产用）
    //  小程序端：wx.login() → code → POST /api/courier/wx-login
    // ═══════════════════════════════════════════

    @Operation(summary = "微信小程序登录（wx.login → code）")
    @PostMapping("/wx-login")
    public R<Map<String, Object>> wxLogin(@RequestBody WxLoginRequest request) {
        if (StrUtil.isBlank(request.getCode())) {
            return R.failed("微信临时 code 不能为空");
        }

        String openid;

        // 优先走真实微信登录
        if (StrUtil.isNotBlank(wechatAppId) && StrUtil.isNotBlank(wechatAppSecret)) {
            // 通过微信 API 获取 openid
            openid = getWechatOpenid(request.getCode());
            if (openid == null) {
                return R.failed("微信登录失败，code 无效或已过期");
            }
        } else {
            // 未配置微信 AppId 时，使用 code 作为模拟 openid（开发模式）
            log.warn("微信小程序未配置 app-id/app-secret，使用模拟模式");
            openid = "mock_openid_" + request.getCode().hashCode();
        }

        // 根据 openid 查找配送员
        Courier courier = courierService.getOne(new LambdaQueryWrapper<Courier>()
                .eq(Courier::getUsername, openid));

        // 如果找不到，尝试找第一个可用的配送员绑定（开发模式便捷操作）
        if (courier == null) {
            courier = courierService.getOne(new LambdaQueryWrapper<Courier>()
                    .eq(Courier::getStatus, 1)
                    .last("LIMIT 1"));
        }

        if (courier == null) {
            return R.failed("没有可用的配送员账号，请联系管理员");
        }

        // 生成 JWT
        String token = jwtTokenProvider.generateToken(
                courier.getId(), courier.getUsername(), "COURIER");

        CustomUserDetails userDetails = new CustomUserDetails(
                courier.getId(), courier.getUsername(), "", "COURIER",
                courier.getNickName(), true);

        return R.success(buildCourierData(token, userDetails));
    }

    @Operation(summary = "获取配送员信息")
    @GetMapping("/info")
    public R<Map<String, Object>> info() {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder
                .getContext().getAuthentication().getPrincipal();
        Map<String, Object> info = new HashMap<>();
        info.put("id", userDetails.getId());
        info.put("username", userDetails.getUsername());
        info.put("nickName", userDetails.getRealName());
        info.put("role", userDetails.getRole());
        return R.success(info);
    }

    // ═══════════════════════════════════════════
    //  辅助方法
    // ═══════════════════════════════════════════

    /** 构建登录返回数据 */
    private Map<String, Object> buildCourierData(String token, CustomUserDetails userDetails) {
        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("tokenHead", "Bearer");

        // 从数据库获取完整的配送员信息
        Courier courier = courierService.getById(userDetails.getId());

        Map<String, Object> courierInfo = new HashMap<>();
        courierInfo.put("id", userDetails.getId());
        courierInfo.put("username", userDetails.getUsername());
        courierInfo.put("nickName", userDetails.getRealName());
        courierInfo.put("phone", courier != null ? courier.getPhone() : "");
        courierInfo.put("status", courier != null ? courier.getStatus() : 1);
        data.put("courierInfo", courierInfo);
        return data;
    }

    /** 通过微信 API 换取 openid */
    private String getWechatOpenid(String code) {
        try {
            String url = "https://api.weixin.qq.com/sns/jscode2session"
                    + "?appid=" + wechatAppId
                    + "&secret=" + wechatAppSecret
                    + "&js_code=" + code
                    + "&grant_type=authorization_code";

            RestTemplate rest = new RestTemplate();
            String response = rest.getForObject(url, String.class);

            // 手动解析 JSON，避免引入额外依赖
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var tree = mapper.readTree(response);
            String openid = tree.has("openid") ? tree.get("openid").asText() : null;
            if (openid != null) {
                log.info("微信登录成功，openid: {}", openid);
                return openid;
            }
            String errMsg = tree.has("errmsg") ? tree.get("errmsg").asText() : "未知错误";
            log.warn("微信登录失败: {}", errMsg);
            return null;
        } catch (Exception e) {
            log.error("微信登录请求失败", e);
            return null;
        }
    }

    // ═══════════════════════════════════════════
    //  内部 DTO
    // ═══════════════════════════════════════════

    public static class LoginRequest {
        private String username;
        private String password;
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }

    public static class WxLoginRequest {
        private String code;
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
    }
}
