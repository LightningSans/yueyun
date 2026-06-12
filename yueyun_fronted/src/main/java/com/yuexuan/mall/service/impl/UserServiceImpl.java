package com.yuexuan.mall.service.impl;

import com.yuexuan.mall.entity.po.User;
import com.yuexuan.mall.mapper.UserMapper;
import com.yuexuan.mall.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 用户表 服务实现类
 * </p>
 *
 * @author huge
 * @since 2026-05-31
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

}
