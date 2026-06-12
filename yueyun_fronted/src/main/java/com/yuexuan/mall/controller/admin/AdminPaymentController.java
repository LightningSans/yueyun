package com.yuexuan.mall.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuexuan.mall.common.R;
import com.yuexuan.mall.entity.po.Payment;
import com.yuexuan.mall.service.IPaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * 管理端 — 支付记录（只读）
 */
@Tag(name = "管理端支付记录")
@RestController
@RequestMapping("/api/admin/payment")
public class AdminPaymentController {

    private final IPaymentService paymentService;

    public AdminPaymentController(IPaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @Operation(summary = "支付记录列表（分页）")
    @GetMapping("/list")
    public R<Page<Payment>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String paymentMethod,
            @RequestParam(required = false) Integer payStatus) {

        LambdaQueryWrapper<Payment> wrapper = new LambdaQueryWrapper<Payment>()
                .eq(StrUtil.isNotBlank(orderNo), Payment::getOrderNo, orderNo)
                .eq(StrUtil.isNotBlank(paymentMethod), Payment::getPaymentMethod, paymentMethod)
                .eq(payStatus != null, Payment::getPayStatus, payStatus)
                .orderByDesc(Payment::getCreateTime);

        return R.success(paymentService.page(new Page<>(page, size), wrapper));
    }
}
