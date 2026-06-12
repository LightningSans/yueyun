package com.yuexuan.mall.controller.user;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yuexuan.mall.common.R;
import com.yuexuan.mall.entity.po.Category;
import com.yuexuan.mall.entity.po.Product;
import com.yuexuan.mall.entity.po.Review;
import com.yuexuan.mall.service.ICategoryService;
import com.yuexuan.mall.service.IProductService;
import com.yuexuan.mall.service.IReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 用户端 — 商品浏览（公开接口）
 */
@Tag(name = "用户端商品")
@RestController
@RequestMapping("/api/product")
public class ProductController {

    private final IProductService productService;
    private final ICategoryService categoryService;
    private final IReviewService reviewService;

    public ProductController(IProductService productService,
                             ICategoryService categoryService,
                             IReviewService reviewService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
    }

    @Operation(summary = "商品列表（分页+筛选）— 来自 Redis 缓存")
    @GetMapping("/list")
    public R<Map<String, Object>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String order,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        // 从 Redis 缓存获取全部商品，在内存中过滤
        List<Product> all = productService.getCachedAll();

        // 筛选：状态为上架 + is_deleted = 0 已经在缓存层做了
        // 过滤条件
        Stream<Product> stream = all.stream()
                .filter(p -> p.getStatus() == 1);

        // 分类筛选（支持子分类）
        if (categoryId != null) {
            List<Long> categoryIds = getCategoryIdsRecursive(categoryId);
            stream = stream.filter(p -> categoryIds.contains(p.getCategoryId()));
        }

        // 关键词筛选
        if (StrUtil.isNotBlank(keyword)) {
            String kw = keyword.toLowerCase();
            stream = stream.filter(p ->
                    (p.getName() != null && p.getName().toLowerCase().contains(kw))
                            || (p.getKeywords() != null && p.getKeywords().toLowerCase().contains(kw))
                            || (p.getBrand() != null && p.getBrand().toLowerCase().contains(kw)));
        }

        // 价格区间
        if (minPrice != null) {
            stream = stream.filter(p -> p.getPrice().compareTo(minPrice) >= 0);
        }
        if (maxPrice != null) {
            stream = stream.filter(p -> p.getPrice().compareTo(maxPrice) <= 0);
        }

        List<Product> filtered = stream.collect(Collectors.toList());

        // 排序
        if ("price".equals(sort)) {
            if ("asc".equals(order)) {
                filtered.sort(Comparator.comparing(Product::getPrice));
            } else {
                filtered.sort(Comparator.comparing(Product::getPrice).reversed());
            }
        } else if ("sales".equals(sort)) {
            filtered.sort(Comparator.comparing(Product::getSales).reversed());
        } else {
            // 默认排序：销量降序 → 价格升序
            filtered.sort(Comparator.comparing(Product::getSales).reversed()
                    .thenComparing(Product::getPrice));
        }

        // 手动分页
        int total = filtered.size();
        int pages = (int) Math.ceil((double) total / size);
        int fromIndex = Math.min((page - 1) * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<Product> pageList = filtered.subList(fromIndex, toIndex);

        // 构建响应
        List<Map<String, Object>> records = pageList.stream().map(this::toProductMap).collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", total);
        result.put("size", size);
        result.put("current", page);
        result.put("pages", pages);
        return R.success(result);
    }

    @Operation(summary = "商品详情")
    @GetMapping("/detail/{id}")
    public R<Map<String, Object>> detail(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null || product.getIsDeleted() == 1) {
            return R.failed("商品不存在");
        }
        Map<String, Object> map = toProductMap(product);

        // 附加详情字段
        map.put("description", product.getDescription());

        // 解析 specs JSON 字符串为数组
        if (StrUtil.isNotBlank(product.getSpecs())) {
            try {
                map.put("specs", new ObjectMapper().readTree(product.getSpecs()));
            } catch (Exception e) {
                map.put("specs", product.getSpecs());
            }
        }

        // 评价统计
        long reviewCount = reviewService.count(new LambdaQueryWrapper<Review>()
                .eq(Review::getProductId, id)
                .eq(Review::getStatus, 1));
        map.put("reviewCount", reviewCount);

        // 计算平均评分
        List<Review> reviews = reviewService.list(new LambdaQueryWrapper<Review>()
                .eq(Review::getProductId, id)
                .eq(Review::getStatus, 1)
                .select(Review::getRating));
        double ratingAvg = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        map.put("ratingAvg", Math.round(ratingAvg * 10) / 10.0);

        return R.success(map);
    }

    @Operation(summary = "分类列表（树形结构）")
    @GetMapping("/category/list")
    public R<List<Map<String, Object>>> categoryList() {
        List<Category> all = categoryService.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getStatus, 1)
                .eq(Category::getIsDeleted, 0)
                .orderByAsc(Category::getSortOrder));
        return R.success(buildTree(all, 0L));
    }

    // ─── 辅助方法 ───

    /** 构建前端需要的商品 Map */
    private Map<String, Object> toProductMap(Product p) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", p.getId());
        map.put("name", p.getName());
        map.put("categoryId", p.getCategoryId());
        map.put("brand", p.getBrand());
        map.put("price", p.getPrice());
        map.put("originalPrice", p.getOriginalPrice());
        map.put("stock", p.getStock());
        map.put("sales", p.getSales());
        map.put("status", p.getStatus());
        map.put("keywords", p.getKeywords());

        // 分类名称
        Category cat = categoryService.getById(p.getCategoryId());
        map.put("categoryName", cat != null ? cat.getName() : null);

        // 解析 images JSON 为数组，取第一张为主图
        if (StrUtil.isNotBlank(p.getImages())) {
            try {
                var arr = new ObjectMapper().readTree(p.getImages());
                if (arr.isArray()) {
                    List<String> images = new ArrayList<>();
                    for (int i = 0; i < arr.size(); i++) {
                        images.add(arr.get(i).asText());
                    }
                    map.put("images", images);
                    map.put("mainImage", images.size() > 0 ? images.get(0) : null);
                }
            } catch (Exception e) {
                map.put("images", null);
                map.put("mainImage", null);
            }
        }
        return map;
    }

    /** 递归构建分类树 */
    private List<Map<String, Object>> buildTree(List<Category> all, Long parentId) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (Category c : all) {
            if (c.getParentId().equals(parentId)) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", c.getId());
                node.put("name", c.getName());
                node.put("parentId", c.getParentId());
                node.put("sortOrder", c.getSortOrder());
                List<Map<String, Object>> children = buildTree(all, c.getId());
                if (!children.isEmpty()) {
                    node.put("children", children);
                }
                nodes.add(node);
            }
        }
        return nodes;
    }

    /** 递归获取分类及其所有子分类的 ID 列表 */
    private List<Long> getCategoryIdsRecursive(Long parentId) {
        List<Long> ids = new ArrayList<>();
        ids.add(parentId);
        List<Category> children = categoryService.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, parentId)
                .eq(Category::getStatus, 1)
                .eq(Category::getIsDeleted, 0));
        for (Category child : children) {
            ids.addAll(getCategoryIdsRecursive(child.getId()));
        }
        return ids;
    }
}
