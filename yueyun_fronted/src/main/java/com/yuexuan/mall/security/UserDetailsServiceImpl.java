package com.yuexuan.mall.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexuan.mall.entity.po.Admin;
import com.yuexuan.mall.entity.po.Courier;
import com.yuexuan.mall.entity.po.User;
import com.yuexuan.mall.mapper.AdminMapper;
import com.yuexuan.mall.mapper.CourierMapper;
import com.yuexuan.mall.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * 统一 UserDetailsService
 * 同时支持管理员、普通用户、配送员登录
 * - 优先匹配 admin 表（管理端 /api/admin/login）
 * - 再查 courier 表（配送员 /api/courier/login）
 * - 最后查 user 表（用户端 /api/user/login，支持手机号）
 */
@Slf4j
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final AdminMapper adminMapper;
    private final CourierMapper courierMapper;
    private final UserMapper userMapper;

    public UserDetailsServiceImpl(AdminMapper adminMapper,
                                  CourierMapper courierMapper,
                                  UserMapper userMapper) {
        this.adminMapper = adminMapper;
        this.courierMapper = courierMapper;
        this.userMapper = userMapper;
    }

    @Override
    public CustomUserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 1. 先查 admin 表（管理端登录）
        Admin admin = adminMapper.selectOne(
                new LambdaQueryWrapper<Admin>()
                        .eq(Admin::getUsername, username));
        if (admin != null) {
            if (admin.getStatus() == null || admin.getStatus() == 0) {
                throw new UsernameNotFoundException("管理员账号已被禁用");
            }
            log.debug("管理员认证: username={}, role={}", admin.getUsername(), admin.getRole());
            return new CustomUserDetails(
                    admin.getId(),
                    admin.getUsername(),
                    admin.getPassword(),
                    admin.getRole(),
                    admin.getRealName(),
                    admin.getStatus() == 1
            );
        }

        // 2. 再查 courier 表（配送员端登录）
        Courier courier = courierMapper.selectOne(
                new LambdaQueryWrapper<Courier>()
                        .eq(Courier::getUsername, username));
        if (courier != null) {
            if (courier.getStatus() == null || courier.getStatus() == 0) {
                throw new UsernameNotFoundException("配送员账号已被禁用");
            }
            log.debug("配送员认证: username={}", courier.getUsername());
            return new CustomUserDetails(
                    courier.getId(),
                    courier.getUsername(),
                    courier.getPassword(),
                    "COURIER",
                    courier.getNickName(),
                    courier.getStatus() == 1
            );
        }

        // 3. 最后查 user 表（用户端登录，支持用户名或手机号）
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getUsername, username)
                        .eq(User::getIsDeleted, 0));
        if (user == null) {
            user = userMapper.selectOne(
                    new LambdaQueryWrapper<User>()
                            .eq(User::getPhone, username)
                            .eq(User::getIsDeleted, 0));
        }
        if (user != null) {
            if (user.getStatus() == null || user.getStatus() == 0) {
                throw new UsernameNotFoundException("账号已被禁用");
            }
            log.debug("用户认证: username={}, phone={}", user.getUsername(), user.getPhone());
            return new CustomUserDetails(
                    user.getId(),
                    user.getUsername(),
                    user.getPassword(),
                    "USER",
                    user.getNickname(),
                    user.getStatus() == 1
            );
        }

        throw new UsernameNotFoundException("用户不存在: " + username);
    }
}
