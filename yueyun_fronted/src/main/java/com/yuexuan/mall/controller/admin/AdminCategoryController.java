package com.yuexuan.mall.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.yuexuan.mall.common.R;
import com.yuexuan.mall.entity.po.Category;
import com.yuexuan.mall.service.ICategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 管理端 — 分类管理
 */
@Tag(name = "管理端分类管理")
@RestController
@RequestMapping("/api/admin/category")
public class AdminCategoryController {

    private final ICategoryService categoryService;

    public AdminCategoryController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @Operation(summary = "分类列表（树形结构）")
    @GetMapping("/list")
    public R<List<Map<String, Object>>> list() {
        List<Category> all = categoryService.list(new LambdaQueryWrapper<Category>()
                .eq(Category::getIsDeleted, 0)
                .orderByAsc(Category::getSortOrder));

        List<Map<String, Object>> tree = buildTree(all, 0L);
        return R.success(tree);
    }

    @Operation(summary = "新增分类")
    @PostMapping("/create")
    public R<Void> create(@RequestBody Category category) {
        if (StrUtil.isBlank(category.getName())) {
            return R.failed("分类名称不能为空");
        }
        category.setParentId(category.getParentId() == null ? 0L : category.getParentId());
        category.setSortOrder(category.getSortOrder() == null ? 30 : category.getSortOrder());
        category.setStatus(1);
        categoryService.save(category);
        return R.success("新增分类成功");
    }

    @Operation(summary = "修改分类")
    @PutMapping("/update")
    public R<Void> update(@RequestBody Category category) {
        if (category.getId() == null) {
            return R.failed("分类ID不能为空");
        }
        categoryService.updateById(category);
        return R.success("修改成功");
    }

    @Operation(summary = "删除分类（逻辑删除）")
    @DeleteMapping("/delete/{id}")
    public R<Void> delete(@PathVariable Long id) {
        // 检查是否有子分类
        long childCount = categoryService.count(new LambdaQueryWrapper<Category>()
                .eq(Category::getParentId, id)
                .eq(Category::getIsDeleted, 0));
        if (childCount > 0) {
            return R.failed("存在子分类，请先删除子分类");
        }
        categoryService.removeById(id);
        return R.success("删除成功");
    }

    /**
     * 递归构建树形结构
     */
    private List<Map<String, Object>> buildTree(List<Category> all, Long parentId) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        for (Category c : all) {
            if (c.getParentId().equals(parentId)) {
                Map<String, Object> node = new HashMap<>();
                node.put("id", c.getId());
                node.put("name", c.getName());
                node.put("parentId", c.getParentId());
                node.put("sortOrder", c.getSortOrder());
                node.put("status", c.getStatus());
                List<Map<String, Object>> children = buildTree(all, c.getId());
                if (!children.isEmpty()) {
                    node.put("children", children);
                }
                nodes.add(node);
            }
        }
        return nodes;
    }
}
