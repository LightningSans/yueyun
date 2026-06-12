package com.yuexuan.mall.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexuan.mall.entity.po.Order;
import com.yuexuan.mall.entity.po.OrderItem;
import com.yuexuan.mall.entity.po.Product;
import com.yuexuan.mall.mapper.OrderItemMapper;
import com.yuexuan.mall.mapper.OrderMapper;
import com.yuexuan.mall.service.IProductService;
import com.yuexuan.mall.service.impl.ToolResultHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;

/**
 * AI Tool 函数注册 — Spring AI Function Calling
 *
 * 所有 Tool 函数注册为 Spring Bean，AI 模型在需要时自动调用。
 * 函数内部对接真实数据库和 Service。
 */
@Slf4j
@Configuration
public class AiToolFunctions {

    private final ObjectMapper mapper = new ObjectMapper();

    // ════════════════════════════════════════════
    //  queryProduct — 商品搜索
    // ════════════════════════════════════════════

    @Bean
    @Description("""
            根据用户输入的查询关键词和预算范围搜索符合条件的商品，返回商品列表（含id、name、price、brand、mainImage）。
            当用户询问商品推荐、商品信息、比价时调用此函数。
            keywords：搜索词（如"手机 Apple"），minPrice/maxPrice：价格范围（元）。
            """)
    public Function<QueryProductRequest, QueryProductResponse> queryProduct(
            IProductService productService, ToolResultHolder holder) {
        return request -> {
            String keywords = request.keywords() != null ? request.keywords() : "";
            BigDecimal minPrice = request.minPrice();
            BigDecimal maxPrice = request.maxPrice();
            log.info("Tool: queryProduct, keywords={}, minPrice={}, maxPrice={}", keywords, minPrice, maxPrice);

            List<Product> allProducts = productService.getCachedAll();
            if (allProducts == null || allProducts.isEmpty()) {
                holder.setProducts(List.of());
                return new QueryProductResponse(List.of());
            }

            String[] kwParts = keywords.toLowerCase().trim().split("[\\s,，、]+");
            boolean hasKeywords = kwParts.length > 0 && !kwParts[0].isEmpty();

            List<Product> matched = new ArrayList<>();
            for (Product p : allProducts) {
                if (p.getStatus() == null || p.getStatus() != 1) continue;
                if (p.getPrice() == null) continue;
                if (minPrice != null && p.getPrice().compareTo(minPrice) < 0) continue;
                if (maxPrice != null && p.getPrice().compareTo(maxPrice) > 0) continue;
                if (hasKeywords && !matchProduct(p, kwParts)) continue;
                matched.add(p);
            }

            matched.sort(maxPrice != null
                    ? Comparator.comparing(Product::getPrice, Comparator.nullsLast(Comparator.naturalOrder()))
                    : (a, b) -> Integer.compare(b.getSales() != null ? b.getSales() : 0, a.getSales() != null ? a.getSales() : 0));

            if (matched.size() > 10) matched = matched.subList(0, 10);

            List<Map<String, Object>> cardList = toCardList(matched);
            holder.setProducts(cardList);
            log.info("Tool queryProduct 匹配 {} 条", cardList.size());
            return new QueryProductResponse(cardList);
        };
    }

    // ════════════════════════════════════════════
    //  queryProductById — 按ID查询单个商品详情
    // ════════════════════════════════════════════

    @Bean
    @Description("根据商品ID查询某商品的完整详细信息。当用户询问某款具体商品的参数时调用。")
    public Function<QueryProductByIdRequest, QueryProductByIdResponse> queryProductById(
            IProductService productService) {
        return request -> {
            Long pid = request.productId();
            log.info("Tool: queryProductById, productId={}", pid);
            Product p = productService.getById(pid);
            if (p == null) return new QueryProductByIdResponse(null, "未找到商品ID=" + pid);

            Map<String, Object> data = new LinkedHashMap<>();
            data.put("id", p.getId());
            data.put("name", p.getName());
            data.put("price", p.getPrice());
            data.put("originalPrice", p.getOriginalPrice());
            data.put("brand", p.getBrand());
            data.put("stock", p.getStock());
            data.put("sales", p.getSales());
            data.put("description", p.getDescription());
            data.put("mainImage", extractMainImage(p.getImages()));
            return new QueryProductByIdResponse(data, null);
        };
    }

    // ════════════════════════════════════════════
    //  prePlaceOrder — 创建订单（实际写入数据库）
    // ════════════════════════════════════════════

    @Bean
    @Description("""
            创建订单：根据用户选定的商品ID和数量创建真实订单。
            参数items：商品列表（productId+quantity）。用户必须已登录。
            返回创建的订单信息（orderNo、金额、商品明细、状态等）。
            用户明确要购买某商品时调用此函数。
            """)
    public Function<PrePlaceOrderRequest, PrePlaceOrderResponse> prePlaceOrder(
            IProductService productService, OrderMapper orderMapper,
            OrderItemMapper orderItemMapper,
            ToolResultHolder holder) {
        return request -> {
            Long userId = holder.getCurrentUserId();
            log.info("Tool: prePlaceOrder, items={}, userId={}", request.items(), userId);

            List<PrePlaceOrderItem> items = request.items();
            if (items == null || items.isEmpty()) {
                return new PrePlaceOrderResponse(null, BigDecimal.ZERO, "请先选择要购买的商品");
            }

            // 生成订单号
            String orderNo = "AI" + System.currentTimeMillis();

            BigDecimal totalAmount = BigDecimal.ZERO;
            List<Map<String, Object>> itemDetails = new ArrayList<>();

            for (var item : items) {
                Long pid = item.productId();
                int qty = item.quantity();
                Product p = productService.getById(pid);
                if (p != null && p.getPrice() != null) {
                    BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(qty));
                    totalAmount = totalAmount.add(subtotal);
                    Map<String, Object> detail = new LinkedHashMap<>();
                    detail.put("productId", p.getId());
                    detail.put("name", p.getName());
                    detail.put("price", p.getPrice());
                    detail.put("quantity", qty);
                    detail.put("subtotal", subtotal);
                    detail.put("mainImage", extractMainImage(p.getImages()));
                    itemDetails.add(detail);
                }
            }

            // 创建订单
            Order order = new Order();
            order.setOrderNo(orderNo);
            order.setUserId(userId);
            order.setTotalAmount(totalAmount);
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setFreightAmount(BigDecimal.valueOf(3));
            order.setPayAmount(totalAmount.add(BigDecimal.valueOf(3)));
            order.setStatus("PENDING_PAYMENT");
            order.setPaymentMethod("MOCK_PAY");
            order.setReceiverName("待补充");
            order.setReceiverPhone("待补充");
            order.setReceiverDetail("AI自动下单，请完善收货信息");
            order.setCreateBy(userId);
            order.setUpdateBy(userId);
            order.setCreateTime(LocalDateTime.now());
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.insert(order);

            // ★ 创建 order_item 明细记录
            for (var item : items) {
                Long pid = item.productId();
                int qty = item.quantity();
                Product p = productService.getById(pid);
                if (p != null && p.getPrice() != null) {
                    BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(qty));
                    OrderItem oi = new OrderItem();
                    oi.setOrderId(order.getId());
                    oi.setOrderNo(orderNo);
                    oi.setProductId(pid);
                    oi.setProductName(p.getName());
                    oi.setProductImage(extractMainImage(p.getImages()));
                    oi.setPrice(p.getPrice());
                    oi.setQuantity(qty);
                    oi.setTotalPrice(subtotal);
                    oi.setCreateTime(LocalDateTime.now());
                    orderItemMapper.insert(oi);
                }
            }

            // 返回完整信息
            Map<String, Object> orderInfo = new LinkedHashMap<>();
            orderInfo.put("orderId", order.getId());
            orderInfo.put("orderNo", orderNo);
            orderInfo.put("totalAmount", totalAmount);
            orderInfo.put("payAmount", totalAmount.add(BigDecimal.valueOf(3)));
            orderInfo.put("status", "PENDING_PAYMENT");
            orderInfo.put("statusText", "待支付");
            orderInfo.put("items", itemDetails);

            return new PrePlaceOrderResponse(orderInfo, totalAmount,
                    "订单已创建！\n订单号：" + orderNo
                    + "\n商品明细：" + itemDetails.stream().map(d -> d.get("name") + " ×" + d.get("quantity")).reduce((a, b) -> a + "、" + b).orElse("")
                    + "\n金额：¥" + totalAmount.add(BigDecimal.valueOf(3))
                    + "\n请在我的订单中查看并支付～");
        };
    }

    // ════════════════════════════════════════════
    //  queryOrderByNo — 按订单号查询
    // ════════════════════════════════════════════

    @Bean
    @Description("根据订单号查询某订单的详细信息（状态、金额、收货信息等）。用户提供订单号时调用。")
    public Function<QueryOrderByNoRequest, QueryOrderByNoResponse> queryOrderByNo(
            OrderMapper orderMapper) {
        return request -> {
            String orderNo = request.orderNo();
            log.info("Tool: queryOrderByNo, orderNo={}", orderNo);

            Order order = orderMapper.selectOne(
                    new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo)
                            .eq(Order::getIsDeleted, 0));
            if (order == null) {
                return new QueryOrderByNoResponse(null, "未找到订单 " + orderNo);
            }

            Map<String, Object> data = orderToMap(order);
            return new QueryOrderByNoResponse(data, null);
        };
    }

    // ════════════════════════════════════════════
    //  queryUserOrders — 查询用户订单列表
    // ════════════════════════════════════════════

    @Bean
    @Description("查询当前登录用户的最近订单列表。用户想查看自己订单时调用。")
    public Function<QueryUserOrdersRequest, QueryUserOrdersResponse> queryUserOrders(
            OrderMapper orderMapper, ToolResultHolder holder) {
        return request -> {
            Long userId = holder.getCurrentUserId();
            log.info("Tool: queryUserOrders, userId={}", userId);

            List<Order> orders = orderMapper.selectList(
                    new LambdaQueryWrapper<Order>()
                            .eq(userId != null, Order::getUserId, userId)
                            .eq(Order::getIsDeleted, 0)
                            .orderByDesc(Order::getCreateTime)
                            .last("LIMIT 10"));

            List<Map<String, Object>> list = new ArrayList<>();
            for (Order o : orders) {
                list.add(orderToMap(o));
            }

            String msg = list.isEmpty() ? "您还没有订单" : "共查到 " + list.size() + " 条订单";
            return new QueryUserOrdersResponse(list, msg);
        };
    }

    // ════════════════════════════════════════════
    //  cancelOrder — 取消订单
    // ════════════════════════════════════════════

    @Bean
    @Description("取消指定订单（仅限待支付或待发货状态的订单可取消）。参数orderNo：订单号，reason：取消原因（可选）。已付款订单取消时会自动恢复库存。")
    public Function<CancelOrderRequest, CancelOrderResponse> cancelOrder(
            OrderMapper orderMapper, IProductService productService,
            OrderItemMapper orderItemMapper) {
        return request -> {
            String orderNo = request.orderNo();
            String reason = request.reason();
            log.info("Tool: cancelOrder, orderNo={}, reason={}", orderNo, reason);

            Order order = orderMapper.selectOne(
                    new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo)
                            .eq(Order::getIsDeleted, 0));
            if (order == null) {
                return new CancelOrderResponse(false, "未找到订单 " + orderNo);
            }

            String status = order.getStatus();
            if (!"PENDING_PAYMENT".equals(status) && !"PENDING_DELIVERY".equals(status)) {
                return new CancelOrderResponse(false,
                        "当前订单状态为「" + status + "」，无法取消。配送中或已完成的订单请申请退货退款。");
            }

            // 已付款的订单（PENDING_DELIVERY）需要恢复库存
            boolean wasPaid = "PENDING_DELIVERY".equals(status);
            if (wasPaid) {
                List<OrderItem> items = orderItemMapper.selectList(
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
            order.setCancelReason(reason != null ? reason : "用户取消");
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);                // 更新状态、取消原因
            orderMapper.deleteById(order.getId());         // MyBatis-Plus 逻辑删除 → SET is_deleted = 1

            return new CancelOrderResponse(true, "订单 " + orderNo + " 已成功取消。");
        };
    }

    // ════════════════════════════════════════════
    //  createReturnOrder — 创建退货单
    // ════════════════════════════════════════════

    @Bean
    @Description("对已发货/已送达的订单创建退货退款申请。参数orderNo：订单号，reason：退货原因。退款时会自动恢复库存。")
    public Function<CreateReturnOrderRequest, CreateReturnOrderResponse> createReturnOrder(
            OrderMapper orderMapper, IProductService productService,
            OrderItemMapper orderItemMapper) {
        return request -> {
            String orderNo = request.orderNo();
            String reason = request.reason();
            log.info("Tool: createReturnOrder, orderNo={}, reason={}", orderNo, reason);

            Order order = orderMapper.selectOne(
                    new LambdaQueryWrapper<Order>().eq(Order::getOrderNo, orderNo)
                            .eq(Order::getIsDeleted, 0));
            if (order == null) {
                return new CreateReturnOrderResponse(null, "未找到订单 " + orderNo);
            }

            String status = order.getStatus();
            if (!"DELIVERED".equals(status) && !"COMPLETED".equals(status)) {
                return new CreateReturnOrderResponse(null,
                        "当前订单状态为「" + status + "」，无法申请退货。仅已送达或已完成的订单可以退货。");
            }

            // 恢复库存
            List<OrderItem> items = orderItemMapper.selectList(
                    new LambdaQueryWrapper<OrderItem>().eq(OrderItem::getOrderId, order.getId()));
            for (OrderItem item : items) {
                Product product = productService.getById(item.getProductId());
                if (product != null && item.getQuantity() != null) {
                    product.setStock(product.getStock() + item.getQuantity());
                    product.setSales(product.getSales() != null ? product.getSales() - item.getQuantity() : 0);
                    productService.updateById(product);
                }
            }

            // 将订单状态改为 "REFUNDING"（退款中）
            order.setStatus("REFUNDING");
            order.setCancelTime(LocalDateTime.now());
            order.setCancelReason(reason != null ? reason : "用户申请退货退款");
            order.setUpdateTime(LocalDateTime.now());
            orderMapper.updateById(order);

            String returnNo = "R" + System.currentTimeMillis();
            return new CreateReturnOrderResponse(1L,
                    "退货退款申请已提交（售后编号：" + returnNo + "），库存已恢复，退款将在 1-3 个工作日内原路返回。");
        };
    }

    // ════════════════════════════════════════════
    //  内部方法
    // ════════════════════════════════════════════

    private boolean matchProduct(Product p, String[] keywords) {
        String text = (p.getName() != null ? p.getName() : "")
                + " " + (p.getBrand() != null ? p.getBrand() : "")
                + " " + (p.getKeywords() != null ? p.getKeywords() : "");
        text = text.toLowerCase();
        for (String kw : keywords) {
            if (kw.length() >= 1 && text.contains(kw)) return true;
        }
        return false;
    }

    private List<Map<String, Object>> toCardList(List<Product> products) {
        List<Map<String, Object>> list = new ArrayList<>();
        for (Product p : products) {
            Map<String, Object> card = new LinkedHashMap<>();
            card.put("id", p.getId());
            card.put("name", p.getName());
            card.put("price", p.getPrice());
            card.put("originalPrice", p.getOriginalPrice());
            card.put("brand", p.getBrand());
            card.put("sales", p.getSales());
            card.put("stock", p.getStock());
            card.put("mainImage", extractMainImage(p.getImages()));
            list.add(card);
        }
        return list;
    }

    private String extractMainImage(String images) {
        if (images == null || images.isEmpty()) return null;
        try {
            if (images.startsWith("[")) {
                List<String> imgList = mapper.readValue(images, List.class);
                return imgList.isEmpty() ? null : imgList.get(0);
            }
        } catch (Exception ignored) {}
        // 不是 JSON 数组，直接返回
        return images;
    }

    private Map<String, Object> orderToMap(Order o) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", o.getId());
        map.put("orderNo", o.getOrderNo());
        map.put("totalAmount", o.getTotalAmount());
        map.put("payAmount", o.getPayAmount());
        map.put("status", o.getStatus());
        map.put("statusText", getStatusText(o.getStatus()));
        map.put("createTime", o.getCreateTime() != null ? o.getCreateTime().toString() : null);
        return map;
    }

    private String getStatusText(String status) {
        if (status == null) return "未知";
        return switch (status) {
            case "PENDING_PAYMENT" -> "待支付";
            case "PENDING_DELIVERY" -> "待发货";
            case "ASSIGNED" -> "已指派";
            case "IN_TRANSIT" -> "配送中";
            case "DELIVERED" -> "已送达";
            case "COMPLETED" -> "已完成";
            case "CANCELLED" -> "已取消";
            case "REFUNDING" -> "退款中";
            default -> status;
        };
    }

    // ════════════════════════════════════════════
    //  Records
    // ════════════════════════════════════════════

    public record QueryProductRequest(String keywords, BigDecimal minPrice, BigDecimal maxPrice) {}
    public record QueryProductResponse(List<Map<String, Object>> products) {}
    public record QueryProductByIdRequest(Long productId) {}
    public record QueryProductByIdResponse(Map<String, Object> product, String error) {}
    public record PrePlaceOrderItem(Long productId, int quantity) {}
    public record PrePlaceOrderRequest(List<PrePlaceOrderItem> items) {}
    public record PrePlaceOrderResponse(Map<String, Object> orderInfo, BigDecimal totalAmount, String message) {}
    public record QueryOrderByNoRequest(String orderNo) {}
    public record QueryOrderByNoResponse(Map<String, Object> order, String error) {}
    public record QueryUserOrdersRequest() {}
    public record QueryUserOrdersResponse(List<Map<String, Object>> orders, String message) {}
    public record CancelOrderRequest(String orderNo, String reason) {}
    public record CancelOrderResponse(boolean success, String message) {}
    public record CreateReturnOrderRequest(String orderNo, String reason) {}
    public record CreateReturnOrderResponse(Long returnOrderId, String message) {}
}
