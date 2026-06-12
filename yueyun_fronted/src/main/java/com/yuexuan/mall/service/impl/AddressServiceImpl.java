package com.yuexuan.mall.service.impl;

import com.yuexuan.mall.entity.po.Address;
import com.yuexuan.mall.mapper.AddressMapper;
import com.yuexuan.mall.service.IAddressService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 收货地址表 服务实现类
 * </p>
 *
 * @author huge
 * @since 2026-05-31
 */
@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements IAddressService {

}
