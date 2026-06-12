package com.yuexuan.mall.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.yuexuan.mall.common.R;
import com.yuexuan.mall.entity.po.Review;
import com.yuexuan.mall.entity.po.User;
import com.yuexuan.mall.security.CustomUserDetails;
import com.yuexuan.mall.service.IReviewService;
import com.yuexuan.mall.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户端 — 评价
 */
@Tag(name = "用户端评价")
@RestController
@RequestMapping("/api/review")
public class ReviewController {

    private final IReviewService reviewService;
    private final IUserService userService;

    public ReviewController(IReviewService reviewService, IUserService userService) {
        this.reviewService = reviewService;
        this.userService = userService;
    }

    private Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return ((CustomUserDetails) auth.getPrincipal()).getId();
    }

    @Operation(summary = "创建评价")
    @PostMapping("/create")
    public R<Void> create(@RequestBody Review review) {
        Long userId = getUserId();
        if (review.getProductId() == null || review.getOrderId() == null) {
            return R.failed("参数错误");
        }
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            return R.failed("评分范围为 1-5");
        }

        review.setUserId(userId);
        review.setStatus(1);
        review.setCreateBy(userId);
        review.setUpdateBy(userId);
        reviewService.save(review);
        return R.success("评价成功");
    }

    @Operation(summary = "商品评价列表")
    @GetMapping("/list/{productId}")
    public R<Map<String, Object>> list(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {

        LambdaQueryWrapper<Review> wrapper = new LambdaQueryWrapper<Review>()
                .eq(Review::getProductId, productId)
                .eq(Review::getStatus, 1)
                .orderByDesc(Review::getCreateTime);

        Page<Review> reviewPage = reviewService.page(new Page<>(page, size), wrapper);

        // 构建带用户信息的响应
        List<Map<String, Object>> records = reviewPage.getRecords().stream().map(r -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", r.getId());
            m.put("userId", r.getUserId());
            m.put("rating", r.getRating());
            m.put("content", r.getContent());
            m.put("images", r.getImages());
            m.put("isAnonymous", r.getIsAnonymous());
            m.put("status", r.getStatus());
            m.put("createTime", r.getCreateTime());

            // 用户信息
            User user = userService.getById(r.getUserId());
            if (user != null) {
                if (r.getIsAnonymous() == 1) {
                    m.put("userNickname", "匿名用户");
                    m.put("userAvatar", null);
                } else {
                    m.put("userNickname", user.getNickname());
                    m.put("userAvatar", user.getAvatar());
                }
            }
            return m;
        }).collect(Collectors.toList());

        // 评分统计
        List<Review> allReviews = reviewService.list(new LambdaQueryWrapper<Review>()
                .eq(Review::getProductId, productId)
                .eq(Review::getStatus, 1)
                .select(Review::getRating));

        double ratingAvg = allReviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);

        // 评分分布
        Map<String, Integer> distribution = new LinkedHashMap<>();
        for (int i = 5; i >= 1; i--) {
            final int star = i;
            long count = allReviews.stream().filter(r -> r.getRating() == star).count();
            distribution.put(String.valueOf(star), (int) count);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("records", records);
        result.put("total", reviewPage.getTotal());
        result.put("size", reviewPage.getSize());
        result.put("current", reviewPage.getCurrent());
        result.put("pages", reviewPage.getPages());
        result.put("ratingAvg", Math.round(ratingAvg * 10) / 10.0);
        result.put("ratingDistribution", distribution);
        return R.success(result);
    }
}
