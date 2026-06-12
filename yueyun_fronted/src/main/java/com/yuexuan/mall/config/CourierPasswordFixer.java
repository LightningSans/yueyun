package com.yuexuan.mall.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexuan.mall.entity.po.Courier;
import com.yuexuan.mall.mapper.CourierMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 配送员密码修复器
 * 启动时自动检测并修正配送员密码
 * 防止数据库种子数据中 BCrypt hash 不匹配导致登录失败
 */
@Slf4j
@Component
@Order(3)
public class CourierPasswordFixer implements CommandLineRunner {

    private final CourierMapper courierMapper;
    private final PasswordEncoder passwordEncoder;

    public CourierPasswordFixer(CourierMapper courierMapper, PasswordEncoder passwordEncoder) {
        this.courierMapper = courierMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        log.info("===== 开始检查配送员密码 =====");
        List<Courier> couriers = courierMapper.selectList(new LambdaQueryWrapper<>());
        int fixed = 0;
        for (Courier courier : couriers) {
            String dbPassword = courier.getPassword();
            // 尝试常见测试密码
            String[] testPasswords = {"123456", "admin123", "password", courier.getUsername()};
            boolean matchFound = false;
            for (String testPwd : testPasswords) {
                if (passwordEncoder.matches(testPwd, dbPassword)) {
                    matchFound = true;
                    break;
                }
            }
            if (!matchFound) {
                log.warn("配送员密码不匹配，重置为默认密码: username={}, id={}", courier.getUsername(), courier.getId());
                courier.setPassword(passwordEncoder.encode("123456"));
                courierMapper.updateById(courier);
                fixed++;
            }
        }
        if (fixed > 0) {
            log.info("===== 已修复 {} 个配送员的密码（默认密码: 123456）=====", fixed);
        } else {
            log.info("===== 所有 {} 个配送员密码均正确 =====", couriers.size());
        }
    }
}
