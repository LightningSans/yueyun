package com.yuexuan.mall.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexuan.mall.entity.po.Admin;
import com.yuexuan.mall.entity.po.Courier;
import com.yuexuan.mall.mapper.AdminMapper;
import com.yuexuan.mall.mapper.CourierMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * 数据初始化器
 * 启动时自动检查并创建默认管理员账号和配送员账号
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final AdminMapper adminMapper;
    private final CourierMapper courierMapper;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(AdminMapper adminMapper,
                           CourierMapper courierMapper,
                           PasswordEncoder passwordEncoder) {
        this.adminMapper = adminMapper;
        this.courierMapper = courierMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        initAdminData();
        initCourierData();
        log.info("===== 数据初始化完成 =====");
    }

    /** 初始化管理员数据 */
    private void initAdminData() {
        long count = adminMapper.selectCount(new LambdaQueryWrapper<>());
        if (count > 0) {
            log.info("管理员数据已存在，跳过初始化");
            return;
        }

        log.info("===== 检测到无管理员数据，开始初始化默认账号 =====");
        log.info("初始化超级管理员: superadmin / admin123");

        Admin superAdmin = new Admin();
        superAdmin.setUsername("superadmin");
        superAdmin.setPassword(passwordEncoder.encode("admin123"));
        superAdmin.setRealName("系统管理员");
        superAdmin.setPhone("13800000001");
        superAdmin.setRole("SUPER_ADMIN");
        superAdmin.setStatus(1);
        superAdmin.setCreateBy(0L);
        adminMapper.insert(superAdmin);

        Admin admin1 = new Admin();
        admin1.setUsername("admin01");
        admin1.setPassword(passwordEncoder.encode("admin123"));
        admin1.setRealName("张管理");
        admin1.setPhone("13800000002");
        admin1.setRole("ADMIN");
        admin1.setStatus(1);
        admin1.setCreateBy(superAdmin.getId());
        adminMapper.insert(admin1);

        Admin admin2 = new Admin();
        admin2.setUsername("admin02");
        admin2.setPassword(passwordEncoder.encode("admin123"));
        admin2.setRealName("李管理");
        admin2.setPhone("13800000003");
        admin2.setRole("ADMIN");
        admin2.setStatus(1);
        admin2.setCreateBy(superAdmin.getId());
        adminMapper.insert(admin2);

        log.info("管理员账号: superadmin / admin01 / admin02，密码: admin123");
    }

    /** 初始化配送员数据 */
    private void initCourierData() {
        long count = courierMapper.selectCount(new LambdaQueryWrapper<>());
        if (count > 0) {
            log.info("配送员数据已存在，跳过初始化");
            return;
        }

        log.info("===== 开始初始化默认配送员账号 =====");

        Courier courier1 = new Courier();
        courier1.setUsername("courier01");
        courier1.setPassword(passwordEncoder.encode("123456"));
        courier1.setNickName("快递小王");
        courier1.setPhone("13900001111");
        courier1.setStatus(1);
        courier1.setCreateBy(0L);
        courierMapper.insert(courier1);

        Courier courier2 = new Courier();
        courier2.setUsername("courier02");
        courier2.setPassword(passwordEncoder.encode("123456"));
        courier2.setNickName("快递小李");
        courier2.setPhone("13900002222");
        courier2.setStatus(1);
        courier2.setCreateBy(0L);
        courierMapper.insert(courier2);

        log.info("配送员账号: courier01 / courier02，密码: 123456");
    }
}
