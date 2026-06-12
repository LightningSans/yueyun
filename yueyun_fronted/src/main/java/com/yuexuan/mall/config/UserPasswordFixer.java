package com.yuexuan.mall.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexuan.mall.entity.po.User;
import com.yuexuan.mall.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 用户密码修复器
 * 启动时自动检测并修正用户密码
 * 解决 SQL 种子数据中 BCrypt hash 不匹配导致登录失败的问题
 */
@Slf4j
@Component
@Order(2)
public class UserPasswordFixer implements CommandLineRunner {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    public UserPasswordFixer(UserMapper userMapper, PasswordEncoder passwordEncoder) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("===== 开始检查用户密码 ===== ");
        List<User> users = userMapper.selectList(new LambdaQueryWrapper<>());
        int fixed = 0;
        for (User user : users) {
            String dbPassword = user.getPassword();
            // 尝试多个常见测试密码，看看哪个匹配
            String[] testPasswords = {"admin123", "123456", "password", user.getUsername()};
            boolean matchFound = false;
            for (String testPwd : testPasswords) {
                if (passwordEncoder.matches(testPwd, dbPassword)) {
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) {
                log.warn("用户密码不匹配，重置为默认密码: username={}, id={}", user.getUsername(), user.getId());
                user.setPassword(passwordEncoder.encode("admin123"));
                userMapper.updateById(user);
                fixed++;
            }
        }
        if (fixed > 0) {
            log.info("===== 已修复 {} 个用户的密码（默认密码: admin123）=====", fixed);
        } else {
            log.info("===== 所有 {} 个用户密码均正确 =====", users.size());
        }
    }
}
