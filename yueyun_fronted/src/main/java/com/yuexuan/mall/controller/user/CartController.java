package com.yuexuan.mall.controller.user;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexuan.mall.common.R;
import com.yuexuan.mall.entity.po.Cart;
import com.yuexuan.mall.entity.po.Product;
import com.yuexuan.mall.security.CustomUserDetails;
import com.yuexuan.mall.service.ICartService;
import com.yuexuan.mall.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户端 — 购物车管理
 */
@Tag(name = "用户端购物车")
@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final ICartService cartService;
    private final IProductService productService;

    public CartController(ICartService cartService, IProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    /** 获取当前登录用户ID */
    private Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    @Operation(summary = "购物车列表（按店铺分组）")
    @GetMapping("/list")
    public R<List<Map<String, Object>>> list() {
        Long userId = getUserId();
        List<Cart> cartList = cartService.list(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .orderByDesc(Cart::getCreateTime));

        // 按店铺分组（暂时全部分为一组）
        List<Map<String, Object>> items = cartList.stream().map(c -> {
            Map<String, Object> item = new HashMap<>();
            item.put("id", c.getId());
            item.put("userId", c.getUserId());
            item.put("productId", c.getProductId());
            item.put("productName", c.getProductName());
            item.put("productImage", c.getProductImage());
            item.put("price", c.getPrice());
            item.put("quantity", c.getQuantity());
            item.put("selected", c.getSelected() == 1);
            return item;
        }).collect(Collectors.toList());

        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> group = new HashMap<>();
        group.put("shopGroup", "悦选商城");
        group.put("items", items);
        result.add(group);

        return R.success(result);
    }

    @Operation(summary = "加入购物车")
    @PostMapping("/add")
    public R<Void> add(@RequestBody AddRequest request) {
        Long userId = getUserId();
        if (request.getProductId() == null || request.getQuantity() == null || request.getQuantity() < 1) {
            return R.failed("参数错误");
        }

        Product product = productService.getById(request.getProductId());
        if (product == null || product.getStatus() != 1) {
            return R.failed("商品不存在或已下架");
        }

        // 检查是否已存在
        Cart existing = cartService.getOne(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getProductId, request.getProductId()));
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + request.getQuantity());
            existing.setUpdateBy(userId);
            cartService.updateById(existing);
            return R.success("已更新数量");
        }

        Cart cart = new Cart();
        cart.setUserId(userId);
        cart.setProductId(request.getProductId());
        cart.setProductName(product.getName());
        // 取第一张图
        String mainImage = null;
        if (StrUtil.isNotBlank(product.getImages())) {
            try {
                var arr = new com.fasterxml.jackson.databind.ObjectMapper().readTree(product.getImages());
                if (arr.isArray() && arr.size() > 0) {
                    mainImage = arr.get(0).asText();
                }
            } catch (Exception ignored) { }
        }
        cart.setProductImage(mainImage);
        cart.setPrice(product.getPrice());
        cart.setQuantity(request.getQuantity());
        cart.setSelected(1);
        cart.setCreateBy(userId);
        cart.setUpdateBy(userId);
        cartService.save(cart);
        return R.success("加入购物车成功");
    }

    @Operation(summary = "修改购物车（数量/勾选）")
    @PutMapping("/update")
    public R<Void> update(@RequestBody CartUpdateRequest request) {
        Long userId = getUserId();
        Cart cart = cartService.getById(request.getId());
        if (cart == null || !cart.getUserId().equals(userId)) {
            return R.failed("购物车记录不存在");
        }
        if (request.getQuantity() != null) cart.setQuantity(request.getQuantity());
        if (request.getSelected() != null) cart.setSelected(request.getSelected() ? 1 : 0);
        cart.setUpdateBy(userId);
        cartService.updateById(cart);
        return R.success("修改成功");
    }

    @Operation(summary = "删除购物车项")
    @DeleteMapping("/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        Long userId = getUserId();
        Cart cart = cartService.getById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            return R.failed("购物车记录不存在");
        }
        cartService.removeById(id);
        return R.success("已删除");
    }

    @Operation(summary = "清空购物车（已勾选）")
    @DeleteMapping("/clear")
    public R<Void> clear() {
        Long userId = getUserId();
        cartService.remove(new LambdaQueryWrapper<Cart>()
                .eq(Cart::getUserId, userId)
                .eq(Cart::getSelected, 1));
        return R.success("已清空");
    }

    // ─── DTO ───

    public static class AddRequest {
        private Long productId;
        private Integer quantity;
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
    }

    public static class CartUpdateRequest {
        private Long id;
        private Integer quantity;
        private Boolean selected;
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Boolean getSelected() { return selected; }
        public void setSelected(Boolean selected) { this.selected = selected; }
    }
}
