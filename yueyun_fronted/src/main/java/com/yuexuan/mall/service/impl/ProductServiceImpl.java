package com.yuexuan.mall.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yuexuan.mall.entity.po.Product;
import com.yuexuan.mall.mapper.ProductMapper;
import com.yuexuan.mall.service.IProductService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 商品表 服务实现类
 * 全量缓存策略：
 * - 商品数量少（≤100），全部缓存到 Redis
 * - 启动时自动加载
 * - 读操作优先走缓存
 * - 写操作后清除缓存，下次读时自动回填
 */
@Slf4j
@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements IProductService {

    private static final String CACHE_KEY_ALL = "product:all";
    private static final String CACHE_KEY_PREFIX = "product:id:";
    private static final long CACHE_TTL_HOURS = 24;

    private final RedisTemplate<String, Object> redisTemplate;

    public ProductServiceImpl(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @PostConstruct
    public void init() {
        log.info("===== 商品缓存初始化（清除旧缓存 + 重新加载）=====");
        // 清除旧格式缓存（防止序列化格式冲突）
        redisTemplate.delete(CACHE_KEY_ALL);
        Set<String> keys = redisTemplate.keys(CACHE_KEY_PREFIX + "*");
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
        refreshCache();
    }

    // ══════════════════════════════════════════
    //  缓存读取
    // ══════════════════════════════════════════

    @Override
    @SuppressWarnings("unchecked")
    public List<Product> getCachedAll() {
        // 1. 尝试从 Redis 获取
        Object cached = redisTemplate.opsForValue().get(CACHE_KEY_ALL);
        if (cached instanceof List) {
            log.debug("商品列表：缓存命中");
            return (List<Product>) cached;
        }

        // 2. 缓存未命中 → 查数据库并回填缓存
        log.debug("商品列表：缓存未命中，查询数据库");
        List<Product> list = loadAllToCache();

        // 3. 通知其他 Service 清空它们可能持有的引用（如 PaymentController）
        return list;
    }

    @Override
    public Product getCachedById(Long id) {
        // 1. 尝试从 Redis 获取单个商品
        String key = CACHE_KEY_PREFIX + id;
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached instanceof Product) {
            log.debug("商品详情 [{}]：缓存命中", id);
            return (Product) cached;
        }

        // 2. 从全量缓存中查找
        List<Product> all = getCachedAll();
        if (all != null) {
            for (Product p : all) {
                if (p.getId().equals(id)) {
                    // 回填单商品缓存
                    redisTemplate.opsForValue().set(key, p, CACHE_TTL_HOURS, TimeUnit.HOURS);
                    return p;
                }
            }
        }

        // 3. 兜底：查数据库（直接调用 MP 的 baseMapper 避免递归）
        Product product = baseMapper.selectById(id);
        if (product != null) {
            redisTemplate.opsForValue().set(key, product, CACHE_TTL_HOURS, TimeUnit.HOURS);
        }
        return product;
    }

    // ══════════════════════════════════════════
    //  缓存管理
    // ══════════════════════════════════════════

    @Override
    public void evictCache() {
        redisTemplate.delete(CACHE_KEY_ALL);
        // 注意：不清除单商品缓存，它们由下一次 getCachedAll 回填时覆盖
        log.info("商品全量缓存已清除");
    }

    @Override
    public void refreshCache() {
        loadAllToCache();
    }

    /**
     * 从数据库加载全部商品到 Redis
     */
    @SuppressWarnings("unchecked")
    private List<Product> loadAllToCache() {
        List<Product> list = Collections.emptyList();
        try {
            list = baseMapper.selectList(new LambdaQueryWrapper<Product>()
                    .eq(Product::getIsDeleted, 0)
                    .orderByDesc(Product::getCreateTime));
            redisTemplate.opsForValue().set(CACHE_KEY_ALL, list, CACHE_TTL_HOURS, TimeUnit.HOURS);
            log.info("已缓存全部商品：{} 条", list.size());
        } catch (Exception e) {
            log.error("缓存商品失败，回退数据库查询", e);
        }
        return list;
    }

    // ══════════════════════════════════════════
    //  重写 MyBatis-Plus 方法 → 优先缓存
    // ══════════════════════════════════════════

    @Override
    public Product getById(Serializable id) {
        return getCachedById((Long) id);
    }

    @Override
    public boolean save(Product entity) {
        boolean result = super.save(entity);
        if (result) evictCache();
        return result;
    }

    @Override
    public boolean updateById(Product entity) {
        boolean result = super.updateById(entity);
        if (result) evictCache();
        return result;
    }

    @Override
    public boolean removeById(Serializable id) {
        boolean result = super.removeById(id);
        if (result) evictCache();
        return result;
    }

    @Override
    public boolean removeById(Product entity) {
        boolean result = super.removeById(entity);
        if (result) evictCache();
        return result;
    }
}
