package com.yuexuan.mall.service.impl;

import com.yuexuan.mall.entity.po.Order;
import com.yuexuan.mall.mapper.OrderMapper;
import com.yuexuan.mall.service.IOrderService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单表 服务实现类
 * </p>
 *
 * @author huge
 * @since 2026-05-31
 */
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements IOrderService {

}
