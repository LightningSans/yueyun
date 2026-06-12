package com.yuexuan.mall.controller.user;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuexuan.mall.common.R;
import com.yuexuan.mall.entity.po.*;
import com.yuexuan.mall.security.CustomUserDetails;
import com.yuexuan.mall.service.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户端 — 订单管理
 */
@Tag(name = "用户端订单管理")
@RestController
@RequestMapping("/api/order")
public class OrderController {

    private final IOrderService orderService;
    private final IOrderItemService orderItemService;
    private final ICartService cartService;
    private final IAddressService addressService;
    private final IProductService productService;
    private final IPaymentService paymentService;

    public OrderController(IOrderService orderService,
                           IOrderItemService orderItemService,
                           ICartService cartService,
                           IAddressService addressService,
                           IProductService productService,
                           IPaymentService paymentService) {
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.cartService = cartService;
        this.addressService = addressService;
        this.productService = productService;
        this.paymentService = paymentService;
    }

    private Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    @Operation(summary = "创建订单（从购物车）")
    @PostMapping("/create")
    @Transactional(rollbackFor = Exception.class)
    public R<Map<String, Object>> create(@RequestBody CreateRequest request) {
        Long userId = getUserId();

        if (request.getAddressId() == null || request.getCartItemIds() == null || request.getCartItemIds().isEmpty()) {
            return R.failed("参数错误：请选择地址和商品");
        }

        // 获取地址
        Address address = addressService.getById(request.getAddressId());
        if (address == null || !address.getUserId().equals(userId)) {
            return R.failed("地址不存在");
        }

        // 获取购物车项
        List<Cart> cartItems = cartService.list(new LambdaQueryWrapper<Cart>()
                .in(Cart::getId, request.getCartItemIds())
                .eq(Cart::getUserId, userId));
        if (cartItems.isEmpty()) {
            return R.failed("购物车中未找到选中商品");
        }

        // 校验库存
        for (Cart cart : cartItems) {
            Product product = productService.getById(cart.getProductId());
            if (product == null || product.getStatus() != 1) {
                return R.failed("商品「" + cart.getProductName() + "」已下架");
            }
            if (product.getStock() < cart.getQuantity()) {
                return R.failed("商品「" + cart.getProductName() + "」库存不足");
            }
        }

        // 生成订单号
        String orderNo = "ORD" + DateUtil.format(new Date(), "yyyyMMddHHmmss")
                + String.format("%04d", new Random().nextInt(10000));

        // 计算金额
        BigDecimal totalAmount = cartItems.stream()
                .map(c -> c.getPrice().multiply(BigDecimal.valueOf(c.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal freightAmount = totalAmount.compareTo(new BigDecimal("99")) >= 0
                ? BigDecimal.ZERO : new BigDecimal("3.00");
        BigDecimal payAmount = totalAmount.add(freightAmount);

        // 创建订单
        Order order = new Order();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setDiscountAmount(BigDecimal.ZERO);
        order.setFreightAmount(freightAmount);
        order.setPayAmount(payAmount);
        order.setStatus("PENDING_PAYMENT");
        order.setReceiverName(address.getReceiverName());
        order.setReceiverPhone(address.getReceiverPhone());
        order.setReceiverProvince(address.getProvinceName());
        order.setReceiverCity(address.getCityName());
        order.setReceiverDistrict(address.getDistrictName());
        order.setReceiverDetail(address.getDetailAddress());
        order.setCreateBy(userId);
        order.setUpdateBy(userId);
        orderService.save(order);

        // 创建订单明细
        for (Cart cart : cartItems) {
            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setOrderNo(orderNo);
            item.setProductId(cart.getProductId());
            item.setProductName(cart.getProductName());
            item.setProductImage(cart.getProductImage());
            item.setPrice(cart.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setTotalPrice(cart.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
            orderItemService.save(item);
        }

        // 删除购物车中已下单项
        cartService.remove(new LambdaQueryWrapper<Cart>()
                .in(Cart::getId, request.getCartItemIds())
                .eq(Cart::getUserId, userId));

        Map<String, Object> data = new HashMap<>();
        data.put("orderId", order.getId());
        data.put("orderNo", order.getOrderNo());
        data.put("status", order.getStatus());
        data.put("payAmount", order.getPayAmount());
        data.put("createTime", order.getCreateTime());
        return R.success(data);
    }

    @Operation(summary = "订单列表")
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String status) {
        Long userId = getUserId();

        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<Order>()
                .eq(Order::getUserId, userId)
                .eq(Order::getIsDeleted, 0)
                .eq(StrUtil.isNotBlank(status), Order::getStatus, status)
                .orderByDesc(Order::getCreateTime);

        Page<Order> orderPage = orderService.page(new Page<>(page, size), wrapper);

        List<Map<String, Object>> records = orderPage.getRecords().stream().map(this::toOrderMap).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", orderPage.getTotal());
        result.put("size", orderPage.getSize());
        result.put("current", orderPage.getCurrent());
        result.put("pages", orderPage.getPages());
        return R.success(result);
    }

    @Operation(summary = "订单详情")
    @GetMapping("/detail/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        Long userId = getUserId();
        Order order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId) || Integer.valueOf(1).equals(order.getIsDeleted())) {
            return R.failed("订单不存在");
        }
        return R.success(toOrderDetailMap(order));
    }

    @Operation(summary = "取消订单（待支付或待发货可取消）")
    @PostMapping("/cancel/{id}")
    @Transactional(rollbackFor = Exception.class)
    public R<Void> cancel(@PathVariable Long id, @RequestBody(required = false) Map<String, String> body) {
        Long userId = getUserId();
        Order order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return R.failed("订单不存在");
        }
        if (!"PENDING_PAYMENT".equals(order.getStatus()) && !"PENDING_DELIVERY".equals(order.getStatus())) {
            return R.failed("当前订单状态不允许取消");
        }

        // 如果是已付款的订单（PENDING_DELIVERY），恢复库存
        boolean wasPaid = "PENDING_DELIVERY".equals(order.getStatus());
        if (wasPaid) {
            List<OrderItem> items = orderItemService.list(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
            for (OrderItem item : items) {
                Product product = productService.getById(item.getProductId());
                if (product != null && item.getQuantity() != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                    product.setSales(product.getSales() != null ? product.getSales() - item.getQuantity() : 0);
                    productService.updateById(product);
                }
            }
        }

        order.setStatus("CANCELLED");
        order.setCancelTime(LocalDateTime.now());
        order.setCancelReason(body != null ? body.get("cancelReason") : null);
        order.setUpdateBy(userId);
        orderService.updateById(order);           // 更新状态、取消原因
        orderService.removeById(order.getId());   // MyBatis-Plus 逻辑删除 → SET is_deleted = 1
        return R.success("订单已取消");
    }

    @Operation(summary = "确认收货")
    @PostMapping("/confirm/{id}")
    public R<Void> confirm(@PathVariable Long id) {
        Long userId = getUserId();
        Order order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return R.failed("订单不存在");
        }
        if (!"DELIVERED".equals(order.getStatus())) {
            return R.failed("当前订单状态不允许确认收货");
        }
        order.setStatus("COMPLETED");
        order.setUpdateBy(userId);
        orderService.updateById(order);
        return R.success("已确认收货");
    }

    @Operation(summary = "软删除订单（仅已完成或已取消的订单可删除）")
    @PostMapping("/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Long userId = getUserId();
        Order order = orderService.getById(id);
        if (order == null || !order.getUserId().equals(userId)) {
            return R.failed("订单不存在");
        }
        if (!"COMPLETED".equals(order.getStatus()) && !"CANCELLED".equals(order.getStatus()) && !"REFUNDING".equals(order.getStatus())) {
            return R.failed("当前订单状态不允许删除，仅已完成、已取消或退款中的订单可删除");
        }
        order.setUpdateBy(userId);
        orderService.updateById(order);            // 更新操作人
        orderService.removeById(order.getId());    // MyBatis-Plus 逻辑删除 → SET is_deleted = 1
        return R.success("订单已删除");
    }

    // ─── 辅助方法 ───

    private Map<String, Object> toOrderMap(Order order) {
        Map<String, Object> map = toOrderDetailMap(order);
        // 列表不包含过多地址信息
        return map;
    }

    private Map<String, Object> toOrderDetailMap(Order order) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", order.getId());
        map.put("orderNo", order.getOrderNo());
        map.put("userId", order.getUserId());
        map.put("totalAmount", order.getTotalAmount());
        map.put("discountAmount", order.getDiscountAmount());
        map.put("freightAmount", order.getFreightAmount());
        map.put("payAmount", order.getPayAmount());
        map.put("paymentMethod", order.getPaymentMethod());
        map.put("status", order.getStatus());
        map.put("statusText", statusText(order.getStatus()));
        map.put("payTime", order.getPayTime());
        map.put("createTime", order.getCreateTime());
        map.put("cancelTime", order.getCancelTime());
        map.put("cancelReason", order.getCancelReason());
        map.put("courierId", order.getCourierId());
        map.put("deliveryTime", order.getDeliveryTime());

        // 收货地址
        map.put("receiverName", order.getReceiverName());
        map.put("receiverPhone", order.getReceiverPhone());
        map.put("receiverProvince", order.getReceiverProvince());
        map.put("receiverCity", order.getReceiverCity());
        map.put("receiverDistrict", order.getReceiverDistrict());
        map.put("receiverDetail", order.getReceiverDetail());

        // 订单项
        List<OrderItem> items = orderItemService.list(new LambdaQueryWrapper<OrderItem>()
                .eq(OrderItem::getOrderId, order.getId()));
        map.put("orderItems", items.stream().map(item -> {
            Map<String, Object> m = new HashMap<>();
            m.put("productId", item.getProductId());
            m.put("productName", item.getProductName());
            m.put("productImage", item.getProductImage());
            m.put("price", item.getPrice());
            m.put("quantity", item.getQuantity());
            m.put("totalPrice", item.getTotalPrice());
            if (StrUtil.isNotBlank(item.getSpecInfo())) {
                try {
                    m.put("specInfo", new com.fasterxml.jackson.databind.ObjectMapper().readTree(item.getSpecInfo()));
                } catch (Exception e) {
                    m.put("specInfo", item.getSpecInfo());
                }
            }
            return m;
        }).collect(Collectors.toList()));

        // 配送员信息（如果有）
        if (order.getCourierId() != null) {
            map.put("courierId", order.getCourierId());
        }

        return map;
    }

    private String statusText(String s) {
        Map<String, String> m = new HashMap<>();
        m.put("PENDING_PAYMENT", "待支付");
        m.put("PENDING_DELIVERY", "待发货");
        m.put("ASSIGNED", "已指派");
        m.put("IN_TRANSIT", "配送中");
        m.put("DELIVERED", "已送达");
        m.put("COMPLETED", "已完成");
        m.put("CANCELLED", "已取消");
        m.put("REFUNDING", "退款中");
        return m.getOrDefault(s, s);
    }

    // ─── DTO ───

    public static class CreateRequest {
        private Long addressId;
        private List<Long> cartItemIds;
        private String remark;
        public Long getAddressId() { return addressId; }
        public void setAddressId(Long addressId) { this.addressId = addressId; }
        public List<Long> getCartItemIds() { return cartItemIds; }
        public void setCartItemIds(List<Long> cartItemIds) { this.cartItemIds = cartItemIds; }
        public String getRemark() { return remark; }
        public void setRemark(String remark) { this.remark = remark; }
    }
}
