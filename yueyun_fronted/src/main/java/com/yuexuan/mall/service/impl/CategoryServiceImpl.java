package com.yuexuan.mall.service.impl;

import com.yuexuan.mall.entity.po.Category;
import com.yuexuan.mall.mapper.CategoryMapper;
import com.yuexuan.mall.service.ICategoryService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 分类表 服务实现类
 * </p>
 *
 * @author huge
 * @since 2026-05-31
 */
@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements ICategoryService {

}
