package com.yuexuan.mall.controller.admin;

import com.yuexuan.mall.common.R;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 管理端 — 工作台（Dashboard）
 */
@Tag(name = "管理端工作台")
@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    @Operation(summary = "工作台数据总览")
    @GetMapping("/stat")
    public R<Map<String, Object>> stat() {
        Map<String, Object> data = new HashMap<>();
        data.put("todayOrderCount", 42);
        data.put("pendingDeliveryCount", 8);
        data.put("pendingOrderCount", 3);
        data.put("todayIncome", 12580.00);
        return R.success(data);
    }

    @Operation(summary = "近7日订单趋势")
    @GetMapping("/orderTrend")
    public R<java.util.List<Map<String, Object>>> orderTrend() {
        return R.success(java.util.List.of(
                Map.of("date", "05-25", "count", 28),
                Map.of("date", "05-26", "count", 38),
                Map.of("date", "05-27", "count", 24),
                Map.of("date", "05-28", "count", 48),
                Map.of("date", "05-29", "count", 42),
                Map.of("date", "05-30", "count", 58),
                Map.of("date", "05-31", "count", 31)
        ));
    }

    @Operation(summary = "订单状态分布")
    @GetMapping("/orderDist")
    public R<Map<String, Object>> orderDist() {
        return R.success(Map.of(
                "pendingPayment", 12,
                "completed", 68,
                "pendingDelivery", 8,
                "cancelled", 10
        ));
    }
}
