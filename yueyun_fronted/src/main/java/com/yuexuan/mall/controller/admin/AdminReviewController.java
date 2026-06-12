package com.yuexuan.mall.controller.admin;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuexuan.mall.common.R;
import com.yuexuan.mall.entity.po.Product;
import com.yuexuan.mall.entity.po.Review;
import com.yuexuan.mall.entity.po.User;
import com.yuexuan.mall.entity.vo.ReviewVO;
import com.yuexuan.mall.service.IProductService;
import com.yuexuan.mall.service.IReviewService;
import com.yuexuan.mall.service.IUserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 管理端 — 评价管理
 */
@Tag(name = "管理端评价管理")
@RestController
@RequestMapping("/api/admin/review")
public class AdminReviewController {

    private final IReviewService reviewService;
    private final IProductService productService;
    private final IUserService userService;

    public AdminReviewController(IReviewService reviewService,
                                 IProductService productService,
                                 IUserService userService) {
        this.reviewService = reviewService;
        this.productService = productService;
        this.userService = userService;
    }

    @Operation(summary = "评价列表（分页+搜索）")
    @GetMapping("/list")
    public R<IPage<ReviewVO>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String productName,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) Integer status) {

        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<Review>()
                .eq(rating != null, Review::getRating, rating)
                .eq(status != null, Review::getStatus, status)
                .orderByDesc(Review::getCreateTime);

        Page<Review> reviewPage = reviewService.page(new Page<>(page, size), wrapper);

        // 转换为 VO，附加商品名称/图片和用户昵称
        List<ReviewVO> voList = reviewPage.getRecords().stream().map(r -> {
            ReviewVO vo = new ReviewVO();
            BeanUtils.copyProperties(r, vo);

            // 查询商品名称和图片
            Product product = productService.getById(r.getProductId());
            if (product != null) {
                vo.setProductName(product.getName());
                // 取 images JSON 数组的第一个作为主图
                if (StrUtil.isNotBlank(product.getImages())) {
                    try {
                        JsonNode arr = new ObjectMapper().readTree(product.getImages());
                        if (arr.isArray() && arr.size() > 0) {
                            vo.setProductImage(arr.get(0).asText());
                        }
                    } catch (Exception ignored) { }
                }
            }

            // 查询用户昵称
            User user = userService.getById(r.getUserId());
            if (user != null) {
                vo.setUserNickname(user.getNickname());
            }

            return vo;
        }).collect(Collectors.toList());

        // 构建分页结果
        Page<ReviewVO> voPage = new Page<>(reviewPage.getCurrent(), reviewPage.getSize(), reviewPage.getTotal());
        voPage.setRecords(voList);

        return R.success(voPage);
    }

    @Operation(summary = "隐藏评价")
    @PostMapping("/hide/{id}")
    public R<Void> hide(@PathVariable Long id) {
        Review review = reviewService.getById(id);
        if (review == null) return R.failed("评价不存在");
        review.setStatus(0);
        reviewService.updateById(review);
        return R.success("评价已隐藏");
    }

    @Operation(summary = "显示评价")
    @PostMapping("/show/{id}")
    public R<Void> show(@PathVariable Long id) {
        Review review = reviewService.getById(id);
        if (review == null) return R.failed("评价不存在");
        review.setStatus(1);
        reviewService.updateById(review);
        return R.success("评价已显示");
    }

    @Operation(summary = "批量隐藏")
    @PostMapping("/batchHide")
    public R<Void> batchHide(@RequestBody List<Long> ids) {
        reviewService.lambdaUpdate()
                .in(Review::getId, ids)
                .set(Review::getStatus, 0)
                .update();
        return R.success("批量隐藏成功");
    }

    @Operation(summary = "批量显示")
    @PostMapping("/batchShow")
    public R<Void> batchShow(@RequestBody List<Long> ids) {
        reviewService.lambdaUpdate()
                .in(Review::getId, ids)
                .set(Review::getStatus, 1)
                .update();
        return R.success("批量显示成功");
    }
}
