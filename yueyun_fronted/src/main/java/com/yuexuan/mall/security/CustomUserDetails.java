package com.yuexuan.mall.security;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * 自定义 UserDetails，适配三种角色
 * - 管理员（ROLE_ADMIN / ROLE_SUPER_ADMIN）
 * - 用户（ROLE_USER）
 * - 配送员（ROLE_COURIER）
 */
@Data
public class CustomUserDetails implements UserDetails {

    private Long id;
    private String username;
    private String password;
    private String role;        // ADMIN / SUPER_ADMIN / USER / COURIER
    private String realName;    // 真实姓名（管理员/配送员）或昵称（用户）
    private boolean enabled;

    public CustomUserDetails(Long id, String username, String password, String role,
                             String realName, boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.role = role;
        this.realName = realName;
        this.enabled = enabled;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }
}
