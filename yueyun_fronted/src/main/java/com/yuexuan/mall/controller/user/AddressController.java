package com.yuexuan.mall.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexuan.mall.common.R;
import com.yuexuan.mall.entity.po.Address;
import com.yuexuan.mall.security.CustomUserDetails;
import com.yuexuan.mall.service.IAddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 用户端 — 收货地址管理
 */
@Tag(name = "用户端地址管理")
@RestController
@RequestMapping("/api/address")
public class AddressController {

    private final IAddressService addressService;

    public AddressController(IAddressService addressService) {
        this.addressService = addressService;
    }

    private Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    @Operation(summary = "地址列表")
    @GetMapping("/list")
    public R<List<Address>> list() {
        Long userId = getUserId();
        List<Address> list = addressService.list(new LambdaQueryWrapper<Address>()
                .eq(Address::getUserId, userId)
                .eq(Address::getIsDeleted, 0)
                .orderByDesc(Address::getIsDefault)
                .orderByDesc(Address::getCreateTime));
        return R.success(list);
    }

    @Operation(summary = "新增/修改地址（有id为修改，无id为新增）")
    @PostMapping("/save")
    public R<Void> save(@RequestBody Address address) {
        Long userId = getUserId();

        if (address.getId() != null) {
            // 修改
            Address existing = addressService.getById(address.getId());
            if (existing == null || !existing.getUserId().equals(userId)) {
                return R.failed("地址不存在");
            }
            address.setUserId(userId);
            address.setUpdateBy(userId);
            addressService.updateById(address);
        } else {
            // 新增
            address.setUserId(userId);
            address.setCreateBy(userId);
            address.setUpdateBy(userId);
            address.setIsDeleted(0);
            addressService.save(address);

            // 如果是第一个地址，自动设为默认
            long count = addressService.count(new LambdaQueryWrapper<Address>()
                    .eq(Address::getUserId, userId)
                    .eq(Address::getIsDeleted, 0));
            if (count == 1) {
                address.setIsDefault(1);
                addressService.updateById(address);
            }
        }

        // 如果设为默认，取消其他默认地址
        if (address.getIsDefault() != null && address.getIsDefault() == 1) {
            addressService.lambdaUpdate()
                    .eq(Address::getUserId, userId)
                    .ne(address.getId() != null, Address::getId, address.getId())
                    .set(Address::getIsDefault, 0)
                    .update();
        }

        return R.success("保存成功");
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Long userId = getUserId();
        Address address = addressService.getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return R.failed("地址不存在");
        }
        address.setIsDeleted(1);
        address.setUpdateBy(userId);
        addressService.updateById(address);
        return R.success("删除成功");
    }

    @Operation(summary = "设为默认地址")
    @PutMapping("/default/{id}")
    public R<Void> setDefault(@PathVariable Long id) {
        Long userId = getUserId();
        Address address = addressService.getById(id);
        if (address == null || !address.getUserId().equals(userId)) {
            return R.failed("地址不存在");
        }
        // 取消所有默认
        addressService.lambdaUpdate()
                .eq(Address::getUserId, userId)
                .set(Address::getIsDefault, 0)
                .update();
        // 设置当前为默认
        address.setIsDefault(1);
        addressService.updateById(address);
        return R.success("已设为默认地址");
    }
}
