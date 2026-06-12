package com.yuexuan.mall.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuexuan.mall.common.R;
import com.yuexuan.mall.entity.po.Category;
import com.yuexuan.mall.entity.po.Product;
import com.yuexuan.mall.service.ICategoryService;
import com.yuexuan.mall.service.IProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 管理端 — 商品管理
 */
@Tag(name = "管理端商品管理")
@RestController
@RequestMapping("/api/admin/product")
public class AdminProductController {

    private final IProductService productService;
    private final ICategoryService categoryService;

    public AdminProductController(IProductService productService,
                                  ICategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @Operation(summary = "商品列表（分页+搜索）")
    @GetMapping("/list")
    public R<Page<Product>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice) {

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<Product>()
                .like(StrUtil.isNotBlank(keyword), Product::getName, keyword)
                .or(StrUtil.isNotBlank(keyword))
                .like(StrUtil.isNotBlank(keyword), Product::getBrand, keyword)
                .or(StrUtil.isNotBlank(keyword))
                .like(StrUtil.isNotBlank(keyword), Product::getKeywords, keyword)
                .eq(status != null, Product::getStatus, status)
                .ge(minPrice != null, Product::getPrice, minPrice)
                .le(maxPrice != null, Product::getPrice, maxPrice)
                .eq(Product::getIsDeleted, 0)
                .orderByDesc(Product::getCreateTime);

        // 分类筛选：支持大类自动包含子分类
        if (categoryId != null) {
            List<Long> categoryIds = getCategoryIdsRecursive(categoryId);
            wrapper.in(Product::getCategoryId, categoryIds);
        }

        return R.success(productService.page(new Page<>(page, size), wrapper));
    }

    @Operation(summary = "商品详情")
    @GetMapping("/detail/{id}")
    public R<Product> detail(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) {
            return R.failed("商品不存在");
        }
        return R.success(product);
    }

    @Operation(summary = "新增商品")
    @PostMapping("/create")
    public R<Void> create(@RequestBody Product product) {
        if (StrUtil.isBlank(product.getName()) || product.getPrice() == null) {
            return R.failed("商品名称和价格不能为空");
        }
        if (product.getCategoryId() == null) {
            return R.failed("请选择商品分类");
        }
        // JSON 列字段处理：空字符串转为 null，避免 MySQL json 列报错
        if (StrUtil.isNotBlank(product.getSpecs())) {
            try {
                // 验证是否为合法 JSON
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                mapper.readTree(product.getSpecs());
            } catch (Exception e) {
                return R.failed("规格（specs）格式不正确，请输入合法 JSON");
            }
        } else {
            product.setSpecs(null);
        }
        if (StrUtil.isBlank(product.getImages())) {
            product.setImages(null);
        }
        product.setSales(product.getSales() == null ? 0 : product.getSales());
        product.setStatus(1);
        productService.save(product);
        return R.success("新增商品成功");
    }

    @Operation(summary = "修改商品")
    @PutMapping("/update")
    public R<Void> update(@RequestBody Product product) {
        if (product.getId() == null) {
            return R.failed("商品ID不能为空");
        }
        if (product.getCategoryId() == null) {
            return R.failed("请选择商品分类");
        }
        // JSON 列字段处理：空字符串转为 null
        if (StrUtil.isBlank(product.getSpecs())) {
            product.setSpecs(null);
        }
        if (StrUtil.isBlank(product.getImages())) {
            product.setImages(null);
        }
        productService.updateById(product);
        return R.success("修改成功");
    }

    @Operation(summary = "上架商品")
    @PostMapping("/up/{id}")
    public R<Void> up(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) return R.failed("商品不存在");
        product.setStatus(1);
        productService.updateById(product);
        return R.success("已上架");
    }

    @Operation(summary = "下架商品")
    @PostMapping("/down/{id}")
    public R<Void> down(@PathVariable Long id) {
        Product product = productService.getById(id);
        if (product == null) return R.failed("商品不存在");
        product.setStatus(0);
        productService.updateById(product);
        return R.success("已下架");
    }

    @Operation(summary = "批量上架")
    @PostMapping("/batchUp")
    public R<Void> batchUp(@RequestBody List<Long> ids) {
        productService.lambdaUpdate()
                .in(Product::getId, ids)
                .set(Product::getStatus, 1)
                .update();
        return R.success("批量上架成功");
    }

    @Operation(summary = "批量下架")
    @PostMapping("/batchDown")
    public R<Void> batchDown(@RequestBody List<Long> ids) {
        productService.lambdaUpdate()
                .in(Product::getId, ids)
                .set(Product::getStatus, 0)
                .update();
        return R.success("批量下架成功");
    }

    @Operation(summary = "删除商品（逻辑删除）")
    @DeleteMapping("/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        productService.removeById(id);
        return R.success("删除成功");
    }

    /**
     * 递归获取分类及其所有子分类的 ID 列表
     */
    private List<Long> getCategoryIdsRecursive(Long parentId) {
        List<Long> ids = new ArrayList<>();
        ids.add(parentId);
        List<Category> children = categoryService.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, parentId)
                .eq(Category::getIsDeleted, 0));
        for (Category child : children) {
            ids.addAll(getCategoryIdsRecursive(child.getId()));
        }
        return ids;
    }
}
