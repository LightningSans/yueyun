-- ============================================================
-- 悦选商城 — 数据库初始化脚本
-- 数据库: yueyun (MySQL 8.0)
-- 说明: 建表脚本，运行后启动 Spring Boot 即可自动填充默认数据
-- ============================================================

-- 创建数据库（如果不存在）
CREATE DATABASE IF NOT EXISTS `yueyun` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `yueyun`;

-- ============================================================
-- 1. 用户表
-- ============================================================
DROP TABLE IF EXISTS `review`;
DROP TABLE IF EXISTS `payment`;
DROP TABLE IF EXISTS `order_item`;
DROP TABLE IF EXISTS `order`;
DROP TABLE IF EXISTS `address`;
DROP TABLE IF EXISTS `cart`;
DROP TABLE IF EXISTS `product`;
DROP TABLE IF EXISTS `category`;
DROP TABLE IF EXISTS `courier`;
DROP TABLE IF EXISTS `admin`;
DROP TABLE IF EXISTS `user`;

CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `username` varchar(50) NOT NULL COMMENT '登录名，唯一',
  `password` varchar(255) NOT NULL COMMENT '加密后密码（BCrypt）',
  `nickname` varchar(50) DEFAULT NULL COMMENT '用户昵称/显示名',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像 URL',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号，可用于登录或短信通知',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱',
  `gender` tinyint DEFAULT '0' COMMENT '性别：0-未知，1-男，2-女',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `status` tinyint DEFAULT '1' COMMENT '账户状态：1-正常，0-禁用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最近一次登录时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID（注册时为0-系统，管理员代建时为 admin.id）',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人ID（关联 admin.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================
-- 2. 管理员表
-- ============================================================
CREATE TABLE `admin` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `username` varchar(50) NOT NULL COMMENT '登录名，唯一（后台用）',
  `password` varchar(255) NOT NULL COMMENT '加密后的密码（BCrypt）',
  `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号（可选）',
  `email` varchar(50) DEFAULT NULL COMMENT '邮箱（可选）',
  `role` varchar(20) NOT NULL DEFAULT 'ADMIN' COMMENT '角色：SUPER_ADMIN（超级管理员）、ADMIN（普通管理员）',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-在职，0-禁用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID（初始超级管理员为0-系统，后续为 SUPER_ADMIN 的 id）',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人ID（关联 admin.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';

-- ============================================================
-- 3. 分类表
-- ============================================================
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `name` varchar(50) NOT NULL COMMENT '分类名称（如"手机"、"电脑"）',
  `parent_id` bigint NOT NULL DEFAULT '0' COMMENT '父分类ID，0表示顶级分类',
  `sort_order` int DEFAULT '30' COMMENT '排序值（数值越小越靠前）',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-启用，0-禁用',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID（关联 admin.id）',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人ID（关联 admin.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- ============================================================
-- 4. 商品表
-- ============================================================
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品主键，自增',
  `name` varchar(200) NOT NULL COMMENT '商品标题（用户端展示）',
  `category_id` bigint NOT NULL COMMENT '所属分类ID（关联分类表）',
  `brand` varchar(50) DEFAULT NULL COMMENT '品牌',
  `price` decimal(10,2) NOT NULL COMMENT '销售价格（必填）',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价/划线价（用于促销展示）',
  `stock` int NOT NULL DEFAULT '0' COMMENT '当前库存数量',
  `sales` int NOT NULL DEFAULT '0' COMMENT '累计销量（默认0，下单时递增）',
  `images` json DEFAULT NULL COMMENT '商品图片列表（JSON数组，第一张为主图）',
  `description` text COMMENT '商品详细描述（文本）',
  `specs` json DEFAULT NULL COMMENT '商品规格定义（JSON）',
  `keywords` varchar(255) DEFAULT NULL COMMENT '搜索关键词，逗号分隔',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-上架，0-下架',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID（关联 admin.id）',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人ID（关联 admin.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';

-- ============================================================
-- 5. 购物车表
-- ============================================================
CREATE TABLE `cart` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `user_id` bigint NOT NULL COMMENT '用户ID（买家）',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) NOT NULL COMMENT '商品名称（冗余，加入时快照）',
  `product_image` varchar(255) DEFAULT NULL COMMENT '商品主图（冗余，快照）',
  `price` decimal(10,2) NOT NULL COMMENT '加入时的单价（冗余，避免下单时价格变动争议）',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '购买数量（≥1）',
  `selected` tinyint DEFAULT '1' COMMENT '是否勾选：1-选中，0-未选',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID（关联 user.id）',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人ID（关联 user.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`, `product_id`) COMMENT '同一用户对同一商品只能有一条购物车记录',
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';

-- ============================================================
-- 6. 收货地址表
-- ============================================================
CREATE TABLE `address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货人手机号',
  `province_name` varchar(50) DEFAULT NULL COMMENT '省名称',
  `city_name` varchar(50) DEFAULT NULL COMMENT '市名称',
  `district_name` varchar(50) DEFAULT NULL COMMENT '区/县名称',
  `detail_address` varchar(255) NOT NULL COMMENT '详细地址（街道、楼号、门牌）',
  `postal_code` varchar(10) DEFAULT NULL COMMENT '邮编',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认地址：1-是，0-否',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID（关联 user.id）',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人ID（关联 user.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址表';

-- ============================================================
-- 7. 订单表（order 为 MySQL 保留字，须用反引号）
-- ============================================================
CREATE TABLE `order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `order_no` varchar(32) NOT NULL COMMENT '订单号（唯一，对外展示）',
  `user_id` bigint NOT NULL COMMENT '买家ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '商品总金额（未减优惠）',
  `discount_amount` decimal(10,2) DEFAULT '0.00' COMMENT '优惠金额（默认0）',
  `freight_amount` decimal(10,2) DEFAULT '3.00' COMMENT '运费（默认3）',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '实付金额（total - discount + freight）',
  `payment_method` varchar(20) DEFAULT NULL COMMENT '支付方式：BALANCE-余额支付，MOCK_PAY-模拟支付',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `status` varchar(30) NOT NULL DEFAULT 'PENDING_PAYMENT' COMMENT '订单状态',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` varchar(255) DEFAULT NULL COMMENT '取消原因',
  `courier_id` bigint DEFAULT NULL COMMENT '配送员ID（初始为空）',
  `receiver_name` varchar(50) NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) NOT NULL COMMENT '收货人手机号',
  `receiver_province` varchar(50) DEFAULT NULL COMMENT '省',
  `receiver_city` varchar(50) DEFAULT NULL COMMENT '市',
  `receiver_district` varchar(50) DEFAULT NULL COMMENT '区/县',
  `receiver_detail` varchar(255) NOT NULL COMMENT '详细地址',
  `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID（关联 user.id，买家自己下单）',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人ID（关联 admin.id 或 courier.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_courier_id` (`courier_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

-- ============================================================
-- 8. 订单明细表（只读快照）
-- ============================================================
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `order_id` bigint NOT NULL COMMENT '订单ID（关联 order.id）',
  `order_no` varchar(32) NOT NULL COMMENT '订单号（冗余）',
  `product_id` bigint NOT NULL COMMENT '商品ID（溯源用）',
  `product_name` varchar(200) NOT NULL COMMENT '商品名称（下单时快照）',
  `product_image` varchar(255) DEFAULT NULL COMMENT '商品主图（下单时快照）',
  `price` decimal(10,2) NOT NULL COMMENT '下单时单价',
  `quantity` int NOT NULL COMMENT '购买数量',
  `total_price` decimal(10,2) NOT NULL COMMENT '小计（price * quantity）',
  `spec_info` json DEFAULT NULL COMMENT '下单时的规格快照',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表（只读快照，无操作人字段）';

-- ============================================================
-- 9. 配送员表
-- ============================================================
CREATE TABLE `courier` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) NOT NULL COMMENT '登录名',
  `password` varchar(255) NOT NULL COMMENT '加密密码',
  `nick_name` varchar(50) DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) NOT NULL COMMENT '手机号',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-在岗，0-禁用',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID（关联 admin.id）',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人ID（关联 admin.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_courier_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配送员表';

-- ============================================================
-- 10. 支付记录表
-- ============================================================
CREATE TABLE `payment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) NOT NULL COMMENT '订单号（冗余）',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `payment_method` varchar(20) DEFAULT 'MOCK_PAY' COMMENT '支付方式：BALANCE-余额支付，MOCK_PAY-模拟支付',
  `pay_status` tinyint DEFAULT '0' COMMENT '1-支付成功 0-失败/待支付',
  `remark` varchar(255) DEFAULT NULL COMMENT '备注',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表（追加写入，无操作人字段）';

-- ============================================================
-- 11. 评价表
-- ============================================================
CREATE TABLE `review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '评价用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `rating` tinyint NOT NULL COMMENT '评分 1-5',
  `content` text COMMENT '评价内容',
  `images` varchar(500) DEFAULT NULL COMMENT '评价图片，JSON数组',
  `is_anonymous` tinyint DEFAULT '0' COMMENT '是否匿名 0-否 1-是',
  `status` tinyint DEFAULT '1' COMMENT '审核状态：1-显示，0-隐藏',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID（关联 user.id）',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人ID（关联 admin.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_user_product` (`order_id`, `user_id`, `product_id`) COMMENT '同一用户对同一订单中的同一商品只能评价一次',
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';

-- ============================================================
-- 初始化完成
-- 说明：默认账号密码由后端 DataInitializer 在启动时自动创建
-- 启动 Spring Boot 后再登录微信小程序即可
-- ============================================================
