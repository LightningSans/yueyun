package com.yuexuan.mall.service.impl;

import com.yuexuan.mall.entity.po.OrderItem;
import com.yuexuan.mall.mapper.OrderItemMapper;
import com.yuexuan.mall.service.IOrderItemService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单明细表（只读快照，无操作人字段） 服务实现类
 * </p>
 *
 * @author huge
 * @since 2026-05-31
 */
@Service
public class OrderItemServiceImpl extends ServiceImpl<OrderItemMapper, OrderItem> implements IOrderItemService {

}
