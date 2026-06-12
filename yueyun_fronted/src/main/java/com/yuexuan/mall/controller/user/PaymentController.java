package com.yuexuan.mall.controller.user;

import com.yuexuan.mall.common.R;
import com.yuexuan.mall.common.ResultCode;
import com.yuexuan.mall.entity.po.Order;
import com.yuexuan.mall.entity.po.OrderItem;
import com.yuexuan.mall.entity.po.Payment;
import com.yuexuan.mall.entity.po.Product;
import com.yuexuan.mall.security.CustomUserDetails;
import com.yuexuan.mall.service.IOrderItemService;
import com.yuexuan.mall.service.IOrderService;
import com.yuexuan.mall.service.IPaymentService;
import com.yuexuan.mall.service.IProductService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户端 — 模拟支付
 */
@Tag(name = "用户端支付")
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    private final IPaymentService paymentService;
    private final IOrderService orderService;
    private final IOrderItemService orderItemService;
    private final IProductService productService;

    public PaymentController(IPaymentService paymentService,
                             IOrderService orderService,
                             IOrderItemService orderItemService,
                             IProductService productService) {
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.productService = productService;
    }

    private Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    @Operation(summary = "模拟支付")
    @PostMapping("/pay")
    @Transactional(rollbackFor = Exception.class)
    public R<Map<String, Object>> pay(@RequestBody PayRequest request) {
        Long userId = getUserId();

        Order order = orderService.getById(request.getOrderId());
        if (order == null || !order.getUserId().equals(userId)) {
            return R.failed("订单不存在");
        }
        if (!"PENDING_PAYMENT".equals(order.getStatus())) {
            return R.failed(ResultCode.ORDER_STATUS_ERROR);
        }

        // 校验库存
        List<OrderItem> items = orderItemService.list(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        for (OrderItem item : items) {
            Product product = productService.getById(item.getProductId());
            if (product == null || product.getStock() < item.getQuantity()) {
                return R.failed("商品「" + item.getProductName() + "」库存不足");
            }
        }

        // 1. 创建支付记录
        Payment payment = new Payment();
        payment.setOrderId(order.getId());
        payment.setOrderNo(order.getOrderNo());
        payment.setPayAmount(order.getPayAmount());
        payment.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "MOCK_PAY");
        payment.setPayStatus(0);
        payment.setRemark("待支付");
        paymentService.save(payment);

        // 2. 模拟支付成功
        payment.setPayStatus(1);
        payment.setPayTime(LocalDateTime.now());
        payment.setRemark("模拟支付成功");
        paymentService.updateById(payment);

        // 3. 扣减库存
        for (OrderItem item : items) {
            Product product = productService.getById(item.getProductId());
            if (product != null) {
                product.setStock(product.getStock() - item.getQuantity());
                product.setSales(product.getSales() + item.getQuantity());
                productService.updateById(product);
            }
        }

        // 4. 更新订单状态
        order.setStatus("PENDING_DELIVERY");
        order.setPaymentMethod(payment.getPaymentMethod());
        order.setPayTime(LocalDateTime.now());
        order.setUpdateBy(userId);
        orderService.updateById(order);

        Map<String, Object> data = new HashMap<>();
        data.put("paymentId", payment.getId());
        data.put("orderId", order.getId());
        data.put("orderNo", order.getOrderNo());
        data.put("payAmount", payment.getPayAmount());
        data.put("paymentMethod", payment.getPaymentMethod());
        data.put("payStatus", payment.getPayStatus());
        data.put("payTime", payment.getPayTime());
        data.put("remark", payment.getRemark());
        return R.success(data);
    }

    @Operation(summary = "查询支付记录")
    @GetMapping("/record/{orderId}")
    public R<Map<String, Object>> record(@PathVariable Long orderId) {
        Long userId = getUserId();
        Order order = orderService.getById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            return R.failed("订单不存在");
        }

        Payment payment = paymentService.getOne(new LambdaQueryWrapper<Payment>()
                .eq(Payment::getOrderId, orderId)
                .orderByDesc(Payment::getCreateTime)
                .last("LIMIT 1"));

        if (payment == null) {
            return R.failed("暂无支付记录");
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", payment.getId());
        data.put("orderId", payment.getOrderId());
        data.put("orderNo", payment.getOrderNo());
        data.put("payAmount", payment.getPayAmount());
        data.put("paymentMethod", payment.getPaymentMethod());
        data.put("payStatus", payment.getPayStatus());
        data.put("remark", payment.getRemark());
        data.put("payTime", payment.getPayTime());
        data.put("createTime", payment.getCreateTime());
        return R.success(data);
    }

    public static class PayRequest {
        private Long orderId;
        private String paymentMethod;
        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public String getPaymentMethod() { return paymentMethod; }
        public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }
    }
}
