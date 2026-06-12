package com.yuexuan.mall.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuexuan.mall.common.R;
import com.yuexuan.mall.entity.po.Courier;
import com.yuexuan.mall.service.ICourierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 — 配送员管理
 */
@Tag(name = "管理端配送员管理")
@RestController
@RequestMapping("/api/admin/courier")
public class AdminCourierController {

    private final ICourierService courierService;
    private final PasswordEncoder passwordEncoder;

    public AdminCourierController(ICourierService courierService, PasswordEncoder passwordEncoder) {
        this.courierService = courierService;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(summary = "配送员列表")
    @GetMapping("/list")
    public R<Page<Courier>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<Courier> wrapper = new LambdaQueryWrapper<Courier>()
                .like(StrUtil.isNotBlank(keyword), Courier::getUsername, keyword)
                .or(StrUtil.isNotBlank(keyword))
                .like(StrUtil.isNotBlank(keyword), Courier::getNickName, keyword)
                .eq(status != null, Courier::getStatus, status)
                .orderByDesc(Courier::getCreateTime);

        Page<Courier> result = courierService.page(new Page<>(page, size), wrapper);
        result.getRecords().forEach(c -> c.setPassword(null));
        return R.success(result);
    }

    @Operation(summary = "新增配送员")
    @PostMapping("/create")
    public R<Void> create(@RequestBody Courier courier) {
        if (StrUtil.isBlank(courier.getUsername()) || StrUtil.isBlank(courier.getPassword())) {
            return R.failed("用户名和密码不能为空");
        }
        long count = courierService.count(new LambdaQueryWrapper<Courier>()
                .eq(Courier::getUsername, courier.getUsername()));
        if (count > 0) {
            return R.failed("用户名已存在");
        }
        courier.setPassword(passwordEncoder.encode(courier.getPassword()));
        courier.setStatus(1);
        courierService.save(courier);
        return R.success("新增配送员成功");
    }

    @Operation(summary = "修改配送员")
    @PutMapping("/update")
    public R<Void> update(@RequestBody Courier courier) {
        if (courier.getId() == null) return R.failed("ID不能为空");
        if (StrUtil.isNotBlank(courier.getPassword())) {
            courier.setPassword(passwordEncoder.encode(courier.getPassword()));
        } else {
            courier.setPassword(null);
        }
        courierService.updateById(courier);
        return R.success("修改成功");
    }

    @Operation(summary = "禁用配送员")
    @PostMapping("/disable/{id}")
    public R<Void> disable(@PathVariable Long id) {
        Courier courier = courierService.getById(id);
        if (courier == null) return R.failed("配送员不存在");
        courier.setStatus(0);
        courierService.updateById(courier);
        return R.success("已禁用");
    }

    @Operation(summary = "启用配送员")
    @PostMapping("/enable/{id}")
    public R<Void> enable(@PathVariable Long id) {
        Courier courier = courierService.getById(id);
        if (courier == null) return R.failed("配送员不存在");
        courier.setStatus(1);
        courierService.updateById(courier);
        return R.success("已启用");
    }

    @Operation(summary = "重置密码")
    @PostMapping("/resetPwd/{id}")
    public R<Void> resetPwd(@PathVariable Long id) {
        Courier courier = courierService.getById(id);
        if (courier == null) return R.failed("配送员不存在");
        courier.setPassword(passwordEncoder.encode("123456"));
        courierService.updateById(courier);
        return R.success("密码已重置为 123456");
    }
}
