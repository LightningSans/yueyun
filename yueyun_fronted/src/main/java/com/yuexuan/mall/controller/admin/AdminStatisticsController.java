package com.yuexuan.mall.controller.admin;

import com.yuexuan.mall.common.R;
import com.yuexuan.mall.entity.po.Order;
import com.yuexuan.mall.entity.po.Product;
import com.yuexuan.mall.service.IOrderService;
import com.yuexuan.mall.service.IProductService;
import com.yuexuan.mall.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 管理端 — 数据统计
 */
@Tag(name = "管理端数据统计")
@RestController
@RequestMapping("/api/admin/statistics")
public class AdminStatisticsController {

    private final IOrderService orderService;
    private final IProductService productService;
    private final IUserService userService;

    public AdminStatisticsController(IOrderService orderService,
                                     IProductService productService,
                                     IUserService userService) {
        this.orderService = orderService;
        this.productService = productService;
        this.userService = userService;
    }

    @Operation(summary = "数据概览")
    @GetMapping("/overview")
    public R<Map<String, Object>> overview() {
        long totalOrders = orderService.count();
        long totalUsers = userService.count();
        long totalProducts = productService.count();

        // 使用 LambdaQueryWrapper 安全传参（避免链式 wrapper 的参数推断问题）
        List<Order> completedOrders = orderService.lambdaQuery()
                .in(Order::getStatus, Arrays.asList("DELIVERED", "COMPLETED"))
                .list();
        double totalRevenue = completedOrders.stream()
                .mapToDouble(o -> o.getPayAmount() != null ? o.getPayAmount().doubleValue() : 0)
                .sum();

        Map<String, Object> data = new HashMap<>();
        data.put("totalOrders", totalOrders);
        data.put("totalRevenue", totalRevenue);
        data.put("totalUsers", totalUsers);
        data.put("totalProducts", totalProducts);
        return R.success(data);
    }

    @Operation(summary = "月度订单趋势")
    @GetMapping("/orderTrend")
    public R<List<Map<String, Object>>> orderTrend() {
        List<Order> allOrders = orderService.lambdaQuery()
                .orderByAsc(Order::getCreateTime)
                .list();

        Map<String, Long> monthCount = new LinkedHashMap<>();
        for (Order o : allOrders) {
            if (o.getCreateTime() != null) {
                String month = o.getCreateTime()
                        .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
                monthCount.merge(month, 1L, Long::sum);
            }
        }

        List<Map<String, Object>> trend = new ArrayList<>();
        for (Map.Entry<String, Long> entry : monthCount.entrySet()) {
            Map<String, Object> item = new HashMap<>();
            item.put("month", entry.getKey());
            item.put("count", entry.getValue());
            trend.add(item);
        }
        return R.success(trend);
    }

    @Operation(summary = "热销商品 Top 10")
    @GetMapping("/topProducts")
    public R<List<Product>> topProducts() {
        List<Product> list = productService.lambdaQuery()
                .eq(Product::getIsDeleted, 0)
                .orderByDesc(Product::getSales)
                .last("LIMIT 10")
                .list();
        return R.success(list);
    }
}
