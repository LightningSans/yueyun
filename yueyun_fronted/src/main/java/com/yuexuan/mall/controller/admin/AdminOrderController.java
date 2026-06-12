package com.yuexuan.mall.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuexuan.mall.common.R;
import com.yuexuan.mall.entity.po.Order;
import com.yuexuan.mall.entity.po.OrderItem;
import com.yuexuan.mall.entity.po.Courier;
import com.yuexuan.mall.service.IOrderService;
import com.yuexuan.mall.service.IOrderItemService;
import com.yuexuan.mall.service.ICourierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理端 — 订单管理
 */
@Tag(name = "管理端订单管理")
@RestController
@RequestMapping("/api/admin/order")
public class AdminOrderController {

    private final IOrderService orderService;
    private final IOrderItemService orderItemService;
    private final ICourierService courierService;

    public AdminOrderController(IOrderService orderService,
                                IOrderItemService orderItemService,
                                ICourierService courierService) {
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.courierService = courierService;
    }

    @Operation(summary = "订单列表（分页+搜索）")
    @GetMapping("/list")
    public R<Page<Order>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(StrUtil.isNotBlank(orderNo), Order::getOrderNo, orderNo)
                .like(StrUtil.isNotBlank(username), Order::getReceiverName, username)
                .eq(StrUtil.isNotBlank(status), Order::getStatus, status)
                .ge(StrUtil.isNotBlank(startDate), Order::getCreateTime, startDate)
                .le(StrUtil.isNotBlank(endDate), Order::getCreateTime, endDate + " 23:59:59")
                .orderByDesc(Order::getCreateTime);

        return R.success(orderService.page(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "订单详情（含商品明细）")
    @GetMapping("/detail/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        Order order = orderService.getById(id);
        if (order == null) {
            return R.failed("订单不存在");
        }
        List<OrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, id));

        Map<String, Object> data = new HashMap<>();
        data.put("order", order);
        data.put("items", items);
        return R.success(data);
    }

    @Operation(summary = "指派配送员")
    @PostMapping("/assign/{id}")
    public R<Void> assignCourier(@PathVariable Long id, @RequestBody Map<String, Long> body) {
        Long courierId = body.get("courierId");
        if (courierId == null) {
            return R.failed("配送员ID不能为空");
        }
        Order order = orderService.getById(id);
        if (order == null) {
            return R.failed("订单不存在");
        }
        Courier courier = courierService.getById(courierId);
        if (courier == null) {
            return R.failed("配送员不存在");
        }
        // 只有待发货的订单才能指派
        if (!"PENDING_DELIVERY".equals(order.getStatus())) {
            return R.failed("当前订单状态不允许指派配送员");
        }
        order.setCourierId(courierId);
        order.setStatus("ASSIGNED");
        orderService.updateById(order);
        return R.success("配送员已指派");
    }
}
