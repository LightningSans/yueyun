package com.yuexuan.mall.service;

import com.yuexuan.mall.entity.po.Product;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.Serializable;
import java.util.List;

/**
 * 商品表 服务类
 */
public interface IProductService extends IService<Product> {

    /**
     * 从缓存中获取所有上架商品列表
     */
    List<Product> getCachedAll();

    /**
     * 从缓存中获取单个商品
     */
    Product getCachedById(Long id);

    /**
     * 清除全部商品缓存（商品变更后调用）
     */
    void evictCache();

    /**
     * 刷新全部商品缓存
     */
    void refreshCache();

    /**
     * 获取商品（优先走缓存，缓存未命中查数据库）
     */
    @Override
    Product getById(Serializable id);
}
