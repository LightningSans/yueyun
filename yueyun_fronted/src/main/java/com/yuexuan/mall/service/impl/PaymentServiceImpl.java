package com.yuexuan.mall.service.impl;

import com.yuexuan.mall.entity.po.Payment;
import com.yuexuan.mall.mapper.PaymentMapper;
import com.yuexuan.mall.service.IPaymentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 支付记录表（追加写入，无操作人字段） 服务实现类
 * </p>
 *
 * @author huge
 * @since 2026-05-31
 */
@Service
public class PaymentServiceImpl extends ServiceImpl<PaymentMapper, Payment> implements IPaymentService {

}
