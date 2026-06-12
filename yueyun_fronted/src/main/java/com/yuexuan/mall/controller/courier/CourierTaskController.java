package com.yuexuan.mall.controller.courier;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuexuan.mall.common.R;
import com.yuexuan.mall.entity.po.Courier;
import com.yuexuan.mall.entity.po.Order;
import com.yuexuan.mall.entity.po.OrderItem;
import com.yuexuan.mall.security.CustomUserDetails;
import com.yuexuan.mall.service.ICourierService;
import com.yuexuan.mall.service.IOrderItemService;
import com.yuexuan.mall.service.IOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 配送员端 — 任务管理
 */
@Tag(name = "配送员端任务管理")
@RestController
@RequestMapping("/api/courier")
public class CourierTaskController {

    private final IOrderService orderService;
    private final IOrderItemService orderItemService;
    private final ICourierService courierService;

    public CourierTaskController(IOrderService orderService,
                                 IOrderItemService orderItemService,
                                 ICourierService courierService) {
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.courierService = courierService;
    }

    /** 获取当前登录配送员 ID */
    private Long getCourierId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    @Operation(summary = "任务列表（按状态筛选）")
    @GetMapping("/task/list")
    public R<List<Map<String, Object>>> taskList(
            @RequestParam(required = false) String status) {
        Long courierId = getCourierId();

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getCourierId, courierId)
                .orderByDesc(Order::getCreateTime);

        // 状态筛选：ASSIGNED / IN_TRANSIT / DELIVERED / COMPLETED
        if (StrUtil.isNotBlank(status)) {
            wrapper.eq(Order::getStatus, status);
        } else {
            // 不传 status 则查所有已指派的（ASSIGNED、IN_TRANSIT、DELIVERED、COMPLETED）
            wrapper.in(Order::getStatus, "ASSIGNED", "IN_TRANSIT", "DELIVERED", "COMPLETED");
        }

        List<Order> orders = orderService.list(wrapper);

        List<Map<String, Object>> result = orders.stream().map(this::toTaskMap).collect(Collectors.toList());
        return R.success(result);
    }

    @Operation(summary = "任务详情")
    @GetMapping("/task/detail/{id}")
    public R<Map<String, Object>> taskDetail(@PathVariable Long id) {
        Long courierId = getCourierId();
        Order order = orderService.getById(id);
        if (order == null || !courierId.equals(order.getCourierId())) {
            return R.failed("任务不存在");
        }

        Map<String, Object> map = toTaskMap(order);

        // 附加订单明细
        List<OrderItem> items = orderItemService.list(
                new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
        map.put("items", items.stream().map(item -> {
            Map<String, Object> m = new HashMap<>();
            m.put("productName", item.getProductName());
            m.put("productImage", item.getProductImage());
            m.put("price", item.getPrice());
            m.put("quantity", item.getQuantity());
            return m;
        }).collect(Collectors.toList()));

        return R.success(map);
    }

    @Operation(summary = "确认取货")
    @PostMapping("/task/pickup/{id}")
    public R<Void> pickup(@PathVariable Long id) {
        Long courierId = getCourierId();
        Order order = orderService.getById(id);
        if (order == null || !courierId.equals(order.getCourierId())) {
            return R.failed("任务不存在");
        }
        if (!"ASSIGNED".equals(order.getStatus())) {
            return R.failed("当前订单状态不允许取货");
        }
        order.setStatus("IN_TRANSIT");
        order.setUpdateBy(courierId);
        orderService.updateById(order);
        return R.success("已确认取货");
    }

    @Operation(summary = "确认送达")
    @PostMapping("/task/deliver/{id}")
    public R<Void> deliver(@PathVariable Long id) {
        Long courierId = getCourierId();
        Order order = orderService.getById(id);
        if (order == null || !courierId.equals(order.getCourierId())) {
            return R.failed("任务不存在");
        }
        if (!"IN_TRANSIT".equals(order.getStatus())) {
            return R.failed("当前订单状态不允许确认送达");
        }
        order.setStatus("DELIVERED");
        order.setUpdateBy(courierId);
        orderService.updateById(order);
        return R.success("已确认送达");
    }

    @Operation(summary = "历史配送记录")
    @GetMapping("/history/list")
    public R<Map<String, Object>> historyList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        Long courierId = getCourierId();

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getCourierId, courierId)
                .in(Order::getStatus, "DELIVERED", "COMPLETED")
                .orderByDesc(Order::getUpdateTime);

        Page<Order> orderPage = orderService.page(new Page<>(page, size), wrapper);

        List<Map<String, Object>> records = orderPage.getRecords().stream().map(this::toTaskMap).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", orderPage.getTotal());
        result.put("size", orderPage.getSize());
        result.put("current", orderPage.getCurrent());
        result.put("pages", orderPage.getPages());
        return R.success(result);
    }

    @Operation(summary = "配送员数据统计")
    @GetMapping("/stats")
    public R<Map<String, Object>> stats() {
        Long courierId = getCourierId();
        Courier courier = courierService.getById(courierId);

        // 今日起始时间
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);

        // 统计总数
        long totalOrders = orderService.count(new LambdaQueryWrapper<Order>()
                .eq(Order::getCourierId, courierId)
                .in(Order::getStatus, "ASSIGNED", "IN_TRANSIT", "DELIVERED", "COMPLETED"));

        long todayOrders = orderService.count(new LambdaQueryWrapper<Order>()
                .eq(Order::getCourierId, courierId)
                .between(Order::getCreateTime, todayStart, todayEnd));

        long completedOrders = orderService.count(new LambdaQueryWrapper<Order>()
                .eq(Order::getCourierId, courierId)
                .in(Order::getStatus, "DELIVERED", "COMPLETED"));

        long inProgressOrders = orderService.count(new LambdaQueryWrapper<Order>()
                .eq(Order::getCourierId, courierId)
                .in(Order::getStatus, "ASSIGNED", "IN_TRANSIT"));

        Map<String, Object> data = new HashMap<>();
        data.put("totalOrders", (int) totalOrders);
        data.put("todayOrders", (int) todayOrders);
        data.put("completedOrders", (int) completedOrders);
        data.put("inProgressOrders", (int) inProgressOrders);
        data.put("rating", 4.9);
        data.put("praiseRate", 98);
        data.put("onlineDays", courier != null ? 32 : 0);
        data.put("todayIncome", 86.00);
        data.put("monthIncome", 2580.00);
        data.put("availableBalance", 1200.00);
        return R.success(data);
    }

    // ─── 辅助方法 ───

    /** 将 Order 转为任务 Map */
    private Map<String, Object> toTaskMap(Order order) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("orderNo", order.getOrderNo());
        map.put("status", order.getStatus());
        map.put("statusText", statusText(order.getStatus()));
        map.put("pickupAddress", "中关村科技大厦B座1206");
        map.put("pickupContact", "王女士");
        map.put("pickupPhone", "138****9001");
        map.put("deliveryName", order.getReceiverName());
        map.put("deliveryPhone", order.getReceiverPhone());
        map.put("deliveryAddress", order.getReceiverProvince()
                + order.getReceiverCity()
                + order.getReceiverDistrict()
                + " " + order.getReceiverDetail());
        map.put("distance", 3.2);
        map.put("payAmount", order.getPayAmount());
        map.put("createTime", order.getCreateTime());
        map.put("deliveryTime", order.getDeliveryTime());
        return map;
    }

    private String statusText(String s) {
        Map<String, String> m = new HashMap<>();
        m.put("ASSIGNED", "待取货");
        m.put("IN_TRANSIT", "配送中");
        m.put("DELIVERED", "已送达");
        m.put("COMPLETED", "已完成");
        m.put("PENDING_PAYMENT", "待支付");
        m.put("PENDING_DELIVERY", "待发货");
        m.put("CANCELLED", "已取消");
        return m.getOrDefault(s, s);
    }
}
