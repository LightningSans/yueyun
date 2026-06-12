package com.yuexuan.mall.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuexuan.mall.common.R;
import com.yuexuan.mall.entity.po.Admin;
import com.yuexuan.mall.service.IAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 — 管理员管理（超级管理员专属）
 * 角色权限由 SecurityConfig 中的路径匹配控制，不再使用 @RolesAllowed
 */
@Tag(name = "管理端管理员管理")
@RestController
@RequestMapping("/api/admin/admin")
public class AdminAdminController {

    private final IAdminService adminService;
    private final PasswordEncoder passwordEncoder;

    public AdminAdminController(IAdminService adminService, PasswordEncoder passwordEncoder) {
        this.adminService = adminService;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "管理员列表（分页）")
    @GetMapping("/list")
    public R<Page<Admin>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<Admin> wrapper = new LambdaQueryWrapper<Admin>()
                .like(StrUtil.isNotBlank(keyword), Admin::getUsername, keyword)
                .or(StrUtil.isNotBlank(keyword))
                .like(StrUtil.isNotBlank(keyword), Admin::getRealName, keyword)
                .eq(StrUtil.isNotBlank(role), Admin::getRole, role)
                .eq(status != null, Admin::getStatus, status)
                .orderByDesc(Admin::getCreateTime);

        Page<Admin> result = adminService.page(new Page<>(page, size), wrapper);
        result.getRecords().forEach(a -> a.setPassword(null));
        return R.success(result);
    }

    @Operation(summary = "新增管理员")
    @PostMapping("/create")
    public R<Void> create(@RequestBody Admin admin) {
        if (StrUtil.isBlank(admin.getUsername()) || StrUtil.isBlank(admin.getPassword())) {
            return R.failed("用户名和密码不能为空");
        }
        long count = adminService.count(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, admin.getUsername()));
        if (count > 0) {
            return R.failed("用户名已存在");
        }
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        admin.setRole(admin.getRole() == null ? "ADMIN" : admin.getRole());
        admin.setStatus(1);
        adminService.save(admin);
        return R.success("新增管理员成功");
    }

    @Operation(summary = "修改管理员")
    @PutMapping("/update")
    public R<Void> update(@RequestBody Admin admin) {
        Admin exist = adminService.getById(admin.getId());
        if (exist == null) {
            return R.failed("管理员不存在");
        }
        if (StrUtil.isNotBlank(admin.getPassword())) {
            admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        } else {
            admin.setPassword(null);
        }
        adminService.updateById(admin);
        return R.success("修改成功");
    }

    @Operation(summary = "禁用/启用管理员")
    @PostMapping("/toggleStatus/{id}")
    public R<Void> toggleStatus(@PathVariable Long id) {
        Admin admin = adminService.getById(id);
        if (admin == null) {
            return R.failed("管理员不存在");
        }
        admin.setStatus(admin.getStatus() == 1 ? 0 : 1);
        adminService.updateById(admin);
        return R.success(admin.getStatus() == 1 ? "已启用" : "已禁用");
    }
}
