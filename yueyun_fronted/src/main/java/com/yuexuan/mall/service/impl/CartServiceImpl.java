package com.yuexuan.mall.service.impl;

import com.yuexuan.mall.entity.po.Cart;
import com.yuexuan.mall.mapper.CartMapper;
import com.yuexuan.mall.service.ICartService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 购物车表 服务实现类
 * </p>
 *
 * @author huge
 * @since 2026-05-31
 */
@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements ICartService {

}
