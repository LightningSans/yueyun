package com.yuexuan.mall.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuexuan.mall.common.R;
import com.yuexuan.mall.entity.po.User;
import com.yuexuan.mall.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理端 — 用户管理
 */
@Tag(name = "管理端用户管理")
@RestController
@RequestMapping("/api/admin/user")
public class AdminUserController {

    private final IUserService userService;

    public AdminUserController(IUserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "用户列表（分页+搜索）")
    @GetMapping("/list")
    public R<Page<User>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<User>()
                .like(keyword != null, User::getUsername, keyword)
                .or(keyword != null)
                .like(keyword != null, User::getPhone, keyword)
                .eq(status != null, User::getStatus, status)
                .orderByDesc(User::getCreateTime);

        return R.success(userService.page(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/detail/{id}")
    public R<User> detail(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return R.failed("用户不存在");
        }
        return R.success(user);
    }

    @Operation(summary = "冻结用户")
    @PostMapping("/freeze/{id}")
    public R<Void> freeze(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return R.failed("用户不存在");
        }
        user.setStatus(0);
        userService.updateById(user);
        return R.success("用户已冻结");
    }

    @Operation(summary = "解冻用户")
    @PostMapping("/unfreeze/{id}")
    public R<Void> unfreeze(@PathVariable Long id) {
        User user = userService.getById(id);
        if (user == null) {
            return R.failed("用户不存在");
        }
        user.setStatus(1);
        userService.updateById(user);
        return R.success("用户已解冻");
    }

    @Operation(summary = "管理员修改用户信息")
    @PutMapping("/update")
    public R<Void> update(@RequestBody User user) {
        if (user.getId() == null) {
            return R.failed("用户ID不能为空");
        }
        // 只允许修改特定字段，防止密码被覆盖
        User existing = userService.getById(user.getId());
        if (existing == null) {
            return R.failed("用户不存在");
        }
        if (user.getNickname() != null) existing.setNickname(user.getNickname());
        if (user.getPhone() != null) existing.setPhone(user.getPhone());
        if (user.getEmail() != null) existing.setEmail(user.getEmail());
        if (user.getGender() != null) existing.setGender(user.getGender());
        if (user.getStatus() != null) existing.setStatus(user.getStatus());
        userService.updateById(existing);
        return R.success("修改成功");
    }
}
