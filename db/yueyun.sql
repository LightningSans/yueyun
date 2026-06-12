-- MySQL dump 10.13  Distrib 8.0.26, for Win64 (x86_64)
--
-- Host: localhost    Database: yueyun
-- ------------------------------------------------------
-- Server version	8.0.26

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `address`
--

DROP TABLE IF EXISTS `address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `address` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `receiver_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人手机号',
  `province_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省名称',
  `city_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '市名称',
  `district_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区/县名称',
  `detail_address` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址（街道、楼号、门牌）',
  `postal_code` varchar(10) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮编',
  `is_default` tinyint DEFAULT '0' COMMENT '是否默认地址：1-是，0-否',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID（关联 user.id）',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人ID（关联 user.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收货地址表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `address`
--

LOCK TABLES `address` WRITE;
/*!40000 ALTER TABLE `address` DISABLE KEYS */;
INSERT INTO `address` VALUES (1,1,'张三','13812348881','北京市','北京市','朝阳区','建国路88号 SOHO现代城A座1508室','100022',1,0,1,1,'2026-01-10 10:00:00','2026-01-10 10:00:00'),(2,1,'张先生','13812348881','北京市','北京市','海淀区','中关村大街1号 银谷大厦1206','100086',0,0,1,1,'2026-03-01 10:00:00','2026-03-01 10:00:00'),(3,2,'李四','13812348882','上海市','上海市','浦东新区','张江高科技园区博云路2号 浦软大厦1601','201203',1,0,2,2,'2026-03-15 10:00:00','2026-03-15 10:00:00'),(4,2,'李女士','13812348882','上海市','上海市','徐汇区','漕溪北路331号 中金国际广场A座9楼','200030',0,0,2,2,'2026-04-01 10:00:00','2026-04-01 10:00:00'),(5,4,'赵六','13812348884','广东省','深圳市','南山区','科技园南区粤兴二道 武汉大学深圳产学研大楼','518057',1,0,4,4,'2025-09-01 10:00:00','2025-09-01 10:00:00'),(6,5,'孙七','13812348885','浙江省','杭州市','西湖区','文三路478号 华星科技大厦12楼','310012',1,0,5,5,'2026-04-01 11:00:00','2026-04-01 11:00:00');
/*!40000 ALTER TABLE `address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `admin`
--

DROP TABLE IF EXISTS `admin`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `admin` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录名，唯一（后台用）',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加密后的密码（BCrypt）',
  `real_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '真实姓名',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号（可选）',
  `email` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱（可选）',
  `role` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'ADMIN' COMMENT '角色：SUPER_ADMIN（超级管理员）、ADMIN（普通管理员）',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-在职，0-禁用',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID（初始超级管理员为0-系统，后续为 SUPER_ADMIN 的 id）',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人ID（关联 admin.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_admin_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='管理员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `admin`
--

LOCK TABLES `admin` WRITE;
/*!40000 ALTER TABLE `admin` DISABLE KEYS */;
INSERT INTO `admin` VALUES (1,'superadmin','$2a$10$Wp0RMuW6OBgJDDtGBhCbyOF9sI/26PeXiZEnfekdoJt5AI8sIk81W','系统管理员','13800000001',NULL,'SUPER_ADMIN',1,'2026-06-10 22:57:33',0,NULL,'2026-05-31 20:37:12','2026-05-31 20:37:12'),(2,'admin01','$2a$10$GHe7vGuCSlZGx23T8H8H6.jeEBY9NbF1C23FF9iqrUwtt8voWUk/K','张管理','13800000002',NULL,'ADMIN',0,NULL,1,NULL,'2026-05-31 20:37:12','2026-05-31 20:37:12'),(3,'admin02','$2a$10$IxyTGCx/pHN04k6oBSv0i.JOadhOFBJ3efgSucSR0.nx1mGwRCJYq','李管理','13800000003',NULL,'ADMIN',1,NULL,1,NULL,'2026-05-31 20:37:12','2026-05-31 20:37:12');
/*!40000 ALTER TABLE `admin` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ai_chat_session`
--

DROP TABLE IF EXISTS `ai_chat_session`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ai_chat_session` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `session_id` varchar(64) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会话标识UUID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `title` varchar(200) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '会话标题',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_by` bigint DEFAULT NULL COMMENT '更新人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_session_id` (`session_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='AI客服会话记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ai_chat_session`
--

LOCK TABLES `ai_chat_session` WRITE;
/*!40000 ALTER TABLE `ai_chat_session` DISABLE KEYS */;
/*!40000 ALTER TABLE `ai_chat_session` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `cart`
--

DROP TABLE IF EXISTS `cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `cart` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `user_id` bigint NOT NULL COMMENT '用户ID（买家）',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `product_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称（冗余，加入时快照）',
  `product_image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品主图（冗余，快照）',
  `price` decimal(10,2) NOT NULL COMMENT '加入时的单价（冗余，避免下单时价格变动争议）',
  `quantity` int NOT NULL DEFAULT '1' COMMENT '购买数量（≥1）',
  `selected` tinyint DEFAULT '1' COMMENT '是否勾选：1-选中，0-未选',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID（关联 user.id）',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人ID（关联 user.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`,`product_id`) COMMENT '同一用户对同一商品只能有一条购物车记录',
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB AUTO_INCREMENT=25 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='购物车表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `cart`
--

LOCK TABLES `cart` WRITE;
/*!40000 ALTER TABLE `cart` DISABLE KEYS */;
INSERT INTO `cart` VALUES (4,2,2,'华为 Mate 60 Pro 昆仑玻璃 卫星通话',NULL,7999.00,1,1,2,2,'2026-05-28 16:00:00','2026-05-28 16:00:00'),(5,4,3,'MacBook Pro 14英寸 M3 Pro 18GB 512GB',NULL,14999.00,1,1,4,4,'2026-05-27 10:00:00','2026-05-27 10:00:00');
/*!40000 ALTER TABLE `cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `category`
--

DROP TABLE IF EXISTS `category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `category` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '分类名称（如"手机"、"电脑"）',
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
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `category`
--

LOCK TABLES `category` WRITE;
/*!40000 ALTER TABLE `category` DISABLE KEYS */;
INSERT INTO `category` VALUES (1,'数码产品',0,10,1,0,1,1,'2026-01-01 10:00:00','2026-01-01 10:00:00'),(2,'手机',1,13,1,0,1,1,'2026-01-01 10:01:00','2026-06-08 17:30:26'),(3,'电脑',1,12,1,0,1,1,'2026-01-01 10:02:00','2026-01-01 10:02:00'),(4,'服饰鞋包',0,20,1,0,1,1,'2026-01-01 10:03:00','2026-01-01 10:03:00'),(5,'男装',4,21,1,0,1,1,'2026-01-01 10:04:00','2026-01-01 10:04:00'),(6,'女装',4,22,1,0,1,1,'2026-01-01 10:05:00','2026-01-01 10:05:00'),(7,'食品饮料',0,30,1,0,1,1,'2026-01-01 10:06:00','2026-01-01 10:06:00'),(8,'休闲零食',7,31,1,0,1,1,'2026-01-01 10:07:00','2026-01-01 10:07:00');
/*!40000 ALTER TABLE `category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `courier`
--

DROP TABLE IF EXISTS `courier`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `courier` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录名',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加密密码',
  `nick_name` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '昵称',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-在岗，0-禁用',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID（关联 admin.id）',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人ID（关联 admin.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_courier_username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='配送员表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `courier`
--

LOCK TABLES `courier` WRITE;
/*!40000 ALTER TABLE `courier` DISABLE KEYS */;
INSERT INTO `courier` VALUES (1,'courier01','$2a$10$u487dg1hQaDCCBwpdNyTRuuzdZQd4M4gxhtbWiVz43TGRTwGtpa1e','快递小王','13900001004',1,1,1,'2026-01-15 10:00:00','2026-06-01 10:51:36'),(2,'courier02','$2a$10$dMMpH8ANajWqGECMUBlyp.nSAwg4ok7iMbhYpPu.9G/ksE8eg3q7.','配送小李','13900001002',1,1,1,'2026-02-01 10:00:00','2026-02-01 10:00:00'),(3,'courier03','$2a$10$OAEg.N6JufrUioA07P34neRAWt6dAFo5NnWIqqQazsY/v2p5SUhMq','速递小张','13900001003',1,1,1,'2026-03-10 10:00:00','2026-03-10 10:00:00'),(4,'curier04','$2a$10$HF50PApTmOOZxHKFlG6.MOvvyPAkpN.N8JWXmfrcnDR/2Fs.WBlXu','niksi','13945678535',1,NULL,NULL,'2026-06-01 10:52:02','2026-06-01 10:52:02');
/*!40000 ALTER TABLE `courier` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order`
--

DROP TABLE IF EXISTS `order`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号（唯一，对外展示）',
  `user_id` bigint NOT NULL COMMENT '买家ID',
  `total_amount` decimal(10,2) NOT NULL COMMENT '商品总金额（未减优惠）',
  `discount_amount` decimal(10,2) DEFAULT '0.00' COMMENT '优惠金额（默认0）',
  `freight_amount` decimal(10,2) DEFAULT '3.00' COMMENT '运费（默认3）',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '实付金额（total - discount + freight）',
  `payment_method` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '支付方式：BALANCE-余额支付，MOCK_PAY-模拟支付',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'PENDING_PAYMENT' COMMENT '订单状态',
  `cancel_time` datetime DEFAULT NULL COMMENT '取消时间',
  `cancel_reason` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '取消原因',
  `courier_id` bigint DEFAULT NULL COMMENT '配送员ID（初始为空）',
  `receiver_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人姓名',
  `receiver_phone` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '收货人手机号',
  `receiver_province` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '省',
  `receiver_city` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '市',
  `receiver_district` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '区/县',
  `receiver_detail` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '详细地址',
  `delivery_time` datetime DEFAULT NULL COMMENT '发货时间',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID（关联 user.id，买家自己下单）',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人ID（关联 admin.id 或 courier.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_courier_id` (`courier_id`)
) ENGINE=InnoDB AUTO_INCREMENT=35 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order`
--

LOCK TABLES `order` WRITE;
/*!40000 ALTER TABLE `order` DISABLE KEYS */;
INSERT INTO `order` VALUES (1,'2026053010001',1,9999.00,0.00,0.00,9999.00,NULL,NULL,'PENDING_PAYMENT',NULL,NULL,NULL,'张三','13812348881','北京市','北京市','朝阳区','建国路88号 SOHO现代城A座1508室',NULL,1,1,'2026-05-30 10:00:00','2026-05-30 10:00:00',0),(2,'2026052910002',2,7999.00,0.00,3.00,8002.00,'MOCK_PAY','2026-05-29 14:30:00','PENDING_DELIVERY',NULL,NULL,NULL,'李四','13812348882','上海市','上海市','浦东新区','张江高科技园区博云路2号 浦软大厦1601',NULL,2,2,'2026-05-29 14:00:00','2026-05-29 14:30:00',0),(3,'2026052810003',1,15197.00,200.00,0.00,14997.00,'MOCK_PAY','2026-05-28 11:20:00','IN_TRANSIT',NULL,NULL,1,'张三','13812348881','北京市','北京市','朝阳区','建国路88号 SOHO现代城A座1508室','2026-05-28 14:30:00',1,1,'2026-05-28 11:00:00','2026-05-28 14:30:00',0),(4,'2026052510004',5,256.00,5.00,0.00,251.00,'BALANCE','2026-05-25 16:45:00','COMPLETED',NULL,NULL,2,'孙七','13812348885','浙江省','杭州市','西湖区','文三路478号 华星科技大厦12楼','2026-05-25 17:30:00',5,5,'2026-05-25 16:30:00','2026-05-25 18:00:00',0),(5,'2026052010005',4,4590.00,0.00,3.00,4593.00,NULL,NULL,'CANCELLED','2026-05-20 12:00:00','不想要了',NULL,'赵六','13812348884','广东省','深圳市','南山区','科技园南区粤兴二道 武汉大学深圳产学研大楼',NULL,4,4,'2026-05-20 10:00:00','2026-06-05 22:02:14',1),(6,'ORD202606011551161534',1,349.00,0.00,0.00,349.00,'MOCK_PAY','2026-06-01 15:51:16','PENDING_DELIVERY',NULL,NULL,NULL,'张三','13812348881','北京市','北京市','朝阳区','建国路88号 SOHO现代城A座1508室',NULL,1,1,'2026-06-01 15:51:16','2026-06-01 15:51:16',0),(7,'ORD202606011621035013',1,1297.40,0.00,0.00,1297.40,'MOCK_PAY','2026-06-01 16:21:04','PENDING_DELIVERY',NULL,NULL,NULL,'张三','13812348881','北京市','北京市','朝阳区','建国路88号 SOHO现代城A座1508室',NULL,1,1,'2026-06-01 16:21:03','2026-06-01 16:21:03',0),(8,'ORD202606011628128557',1,337.80,0.00,0.00,337.80,'MOCK_PAY','2026-06-01 16:28:13','ASSIGNED',NULL,NULL,2,'张三','13812348881','北京市','北京市','朝阳区','建国路88号 SOHO现代城A座1508室',NULL,1,1,'2026-06-01 16:28:12','2026-06-01 16:28:12',0),(9,'ORD202606011645222444',1,49995.00,0.00,0.00,49995.00,'MOCK_PAY','2026-06-01 16:45:23','PENDING_DELIVERY',NULL,NULL,NULL,'张三','13812348881','北京市','北京市','朝阳区','建国路88号 SOHO现代城A座1508室',NULL,1,1,'2026-06-01 16:45:22','2026-06-01 16:45:22',0),(10,'ORD202606021926064138',1,51.90,0.00,3.00,54.90,'MOCK_PAY','2026-06-02 19:26:07','PENDING_DELIVERY',NULL,NULL,NULL,'张三','13812348881','北京市','北京市','朝阳区','建国路88号 SOHO现代城A座1508室',NULL,1,1,'2026-06-02 19:26:06','2026-06-02 19:26:06',0),(11,'ORD202606021926298049',1,39.80,0.00,3.00,42.80,'MOCK_PAY','2026-06-02 19:26:30','COMPLETED',NULL,NULL,1,'张三','13812348881','北京市','北京市','朝阳区','建国路88号 SOHO现代城A座1508室',NULL,1,1,'2026-06-02 19:26:29','2026-06-02 19:26:29',0),(12,'ORD202606021926389625',1,349.00,0.00,0.00,349.00,'MOCK_PAY','2026-06-02 19:26:39','COMPLETED',NULL,NULL,1,'张三','13812348881','北京市','北京市','朝阳区','建国路88号 SOHO现代城A座1508室',NULL,1,1,'2026-06-02 19:26:38','2026-06-02 19:26:38',0),(13,'ORD202606021926551911',1,3088.00,0.00,0.00,3088.00,'MOCK_PAY','2026-06-02 19:26:56','IN_TRANSIT',NULL,NULL,1,'张三','13812348881','北京市','北京市','朝阳区','建国路88号 SOHO现代城A座1508室',NULL,1,1,'2026-06-02 19:26:55','2026-06-02 19:26:55',0),(14,'ORD202606021927075335',1,248.00,0.00,0.00,248.00,'MOCK_PAY','2026-06-02 19:27:08','COMPLETED',NULL,NULL,1,'张三','13812348881','北京市','北京市','朝阳区','建国路88号 SOHO现代城A座1508室',NULL,1,1,'2026-06-02 19:27:07','2026-06-02 19:27:07',0),(16,'AI1780633731286',1,9999.00,0.00,3.00,10002.00,NULL,NULL,'CANCELLED','2026-06-05 22:03:02',NULL,NULL,'AI下单用户','待补充',NULL,NULL,NULL,'AI自动创建',NULL,1,1,'2026-06-05 12:28:51','2026-06-05 12:28:51',1),(17,'AI1780634076729',1,9999.00,0.00,3.00,10002.00,'MOCK_PAY','2026-06-05 12:35:31','CANCELLED','2026-06-05 22:03:06','用户退单',NULL,'待补充','待补充',NULL,NULL,NULL,'AI自动下单，请完善收货信息',NULL,1,1,'2026-06-05 12:34:37','2026-06-05 12:34:37',1),(18,'ORD202606051235572144',1,39.80,0.00,3.00,42.80,'MOCK_PAY','2026-06-05 12:35:58','CANCELLED','2026-06-05 21:47:21','用户申请退单',NULL,'张三','13812348881','北京市','北京市','朝阳区','建国路88号 SOHO现代城A座1508室',NULL,1,1,'2026-06-05 12:35:57','2026-06-05 22:02:14',1),(19,'AI1780664730536',1,7999.00,0.00,3.00,8002.00,'MOCK_PAY',NULL,'CANCELLED','2026-06-05 21:07:58','用户想换购更高配置的手机',NULL,'待补充','待补充',NULL,NULL,NULL,'AI自动下单，请完善收货信息',NULL,1,1,'2026-06-05 21:05:31','2026-06-05 21:07:58',1),(20,'ORD202606052113045386',1,9999.00,0.00,0.00,9999.00,'MOCK_PAY','2026-06-05 21:13:05','CANCELLED','2026-06-05 21:47:11','用户申请退单',NULL,'张三','13812348881','北京市','北京市','朝阳区','建国路88号 SOHO现代城A座1508室',NULL,1,1,'2026-06-05 21:13:04','2026-06-05 21:13:04',1),(21,'AI1780665486937',1,7999.00,0.00,3.00,8002.00,'MOCK_PAY',NULL,'CANCELLED','2026-06-05 21:19:46','用户想升级购买更贵的手机',NULL,'待补充','待补充',NULL,NULL,NULL,'AI自动下单，请完善收货信息',NULL,1,1,'2026-06-05 21:18:07','2026-06-05 21:19:46',1),(22,'AI1780666310394',1,7999.00,0.00,3.00,8002.00,'MOCK_PAY',NULL,'CANCELLED','2026-06-05 21:33:13','用户主动取消，重新考虑购买',NULL,'待补充','待补充',NULL,NULL,NULL,'AI自动下单，请完善收货信息',NULL,1,1,'2026-06-05 21:31:50','2026-06-05 21:33:13',1),(23,'AI1780667650870',1,7999.00,0.00,3.00,8002.00,'MOCK_PAY',NULL,'CANCELLED','2026-06-05 21:58:44','用户因家人建议决定不购买，申请取消订单',NULL,'待补充','待补充',NULL,NULL,NULL,'AI自动下单，请完善收货信息',NULL,1,1,'2026-06-05 21:54:11','2026-06-05 22:02:14',1),(27,'ORD202606081737230709',1,4998.00,0.00,0.00,4998.00,'MOCK_PAY','2026-06-08 17:37:23','COMPLETED',NULL,NULL,1,'张先生','13812348881','北京市','北京市','海淀区','中关村大街1号 银谷大厦1206',NULL,1,1,'2026-06-08 17:37:23','2026-06-08 17:37:23',0),(29,'AI1780912574435',1,9999.00,0.00,3.00,10002.00,'MOCK_PAY','2026-06-10 22:56:51','DELIVERED',NULL,NULL,1,'待补充','待补充',NULL,NULL,NULL,'AI自动下单，请完善收货信息',NULL,1,1,'2026-06-08 17:56:14','2026-06-08 17:56:14',0),(32,'ORD202606102305478814',1,39.80,0.00,3.00,42.80,'MOCK_PAY','2026-06-10 23:05:48','ASSIGNED',NULL,NULL,3,'张三','13812348881','北京市','北京市','朝阳区','建国路88号 SOHO现代城A座1508室',NULL,1,1,'2026-06-10 23:05:47','2026-06-10 23:05:47',0),(34,'ORD202606102318527325',1,101.00,0.00,0.00,101.00,'MOCK_PAY','2026-06-10 23:18:53','COMPLETED',NULL,NULL,1,'张三','13812348881','北京市','北京市','朝阳区','建国路88号 SOHO现代城A座1508室',NULL,1,1,'2026-06-10 23:18:52','2026-06-10 23:18:52',0);
/*!40000 ALTER TABLE `order` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_item`
--

DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键自增',
  `order_id` bigint NOT NULL COMMENT '订单ID（关联 order.id）',
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号（冗余，方便单独查询明细）',
  `product_id` bigint NOT NULL COMMENT '商品ID（溯源用）',
  `product_name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品名称（下单时快照）',
  `product_image` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '商品主图（下单时快照）',
  `price` decimal(10,2) NOT NULL COMMENT '下单时单价',
  `quantity` int NOT NULL COMMENT '购买数量',
  `total_price` decimal(10,2) NOT NULL COMMENT '小计（price * quantity）',
  `spec_info` json DEFAULT NULL COMMENT '下单时的规格快照（如{"颜色":"黑色","尺寸":"M"}）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`),
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=40 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单明细表（只读快照，无操作人字段）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item`
--

LOCK TABLES `order_item` WRITE;
/*!40000 ALTER TABLE `order_item` DISABLE KEYS */;
INSERT INTO `order_item` VALUES (1,1,'2026053010001',1,'iPhone 15 Pro Max 256GB 原色钛金属',NULL,9999.00,1,9999.00,'{\"存储\": \"256GB\", \"颜色\": \"原色钛金属\"}','2026-05-30 10:00:00'),(2,2,'2026052910002',2,'华为 Mate 60 Pro 昆仑玻璃',NULL,7999.00,1,7999.00,'{\"存储\": \"256GB\", \"颜色\": \"白沙银\"}','2026-05-29 14:00:00'),(3,3,'2026052810003',3,'MacBook Pro 14英寸 M3 Pro 18GB',NULL,14999.00,1,14999.00,'{\"存储\": \"512GB\", \"芯片\": \"M3 Pro\"}','2026-05-28 11:00:00'),(4,3,'2026052810003',5,'纯棉圆领T恤 男女同款',NULL,99.00,2,198.00,'{\"尺码\": \"L\", \"颜色\": \"白色\"}','2026-05-28 11:00:00'),(5,4,'2026052510004',7,'三只松鼠 每日坚果礼盒750g',NULL,68.00,1,68.00,NULL,'2026-05-25 16:30:00'),(6,4,'2026052510004',8,'良品铺子 肉干大礼包 500g',NULL,49.90,2,99.80,NULL,'2026-05-25 16:30:00'),(7,4,'2026052510004',15,'《人类群星闪耀时》精装版',NULL,39.80,1,39.80,NULL,'2026-05-25 16:30:00'),(8,5,'2026052010005',13,'戴森 V15 Detect 无线吸尘器',NULL,4590.00,1,4590.00,'{\"型号\": \"V15 Detect\"}','2026-05-20 10:00:00'),(9,6,'ORD202606011551161534',14,'乐高 兰博基尼 Sián FKP 37 科技系列',NULL,349.00,1,349.00,NULL,'2026-06-01 15:51:16'),(10,7,'ORD202606011621035013',9,'资生堂 红腰子精华 75ml 限定版',NULL,589.00,2,1178.00,NULL,'2026-06-01 16:21:03'),(11,7,'ORD202606011621035013',15,'《人类群星闪耀时》斯蒂芬·茨威格 精装版',NULL,39.80,3,119.40,NULL,'2026-06-01 16:21:03'),(12,8,'ORD202606011628128557',11,'武夷山大红袍 特级岩茶 礼盒装500g',NULL,298.00,1,298.00,NULL,'2026-06-01 16:28:12'),(13,8,'ORD202606011628128557',15,'《人类群星闪耀时》斯蒂芬·茨威格 精装版',NULL,39.80,1,39.80,NULL,'2026-06-01 16:28:12'),(14,9,'ORD202606011645222444',1,'iPhone 15 Pro Max 256GB 原色钛金属',NULL,9999.00,5,49995.00,NULL,'2026-06-01 16:45:22'),(15,10,'ORD202606021926064138',8,'良品铺子 肉干大礼包 混合装 500g',NULL,51.90,1,51.90,NULL,'2026-06-02 19:26:06'),(16,11,'ORD202606021926298049',15,'《人类群星闪耀时》斯蒂芬·茨威格 精装版',NULL,39.80,1,39.80,NULL,'2026-06-02 19:26:29'),(17,12,'ORD202606021926389625',14,'乐高 兰博基尼 Sián FKP 37 科技系列',NULL,349.00,1,349.00,NULL,'2026-06-02 19:26:38'),(18,13,'ORD202606021926551911',9,'资生堂 红腰子精华 75ml 限定版',NULL,589.00,1,589.00,NULL,'2026-06-02 19:26:55'),(19,13,'ORD202606021926551911',12,'Sony WH-1000XM5 无线降噪耳机',NULL,2499.00,1,2499.00,NULL,'2026-06-02 19:26:55'),(20,14,'ORD202606021927075335',10,'无印良品 超声波香薰机 400ml 静音设计',NULL,248.00,1,248.00,NULL,'2026-06-02 19:27:07'),(21,15,'ORD202606021927285073',4,'ThinkPad X1 Carbon Gen 11 i7 16GB 512GB',NULL,12999.00,1,12999.00,NULL,'2026-06-02 19:27:28'),(22,17,'AI1780634076729',1,'iPhone 15 Pro Max 256GB 原色钛金属手机',NULL,9999.00,1,9999.00,NULL,'2026-06-05 12:34:37'),(23,18,'ORD202606051235572144',15,'《人类群星闪耀时》斯蒂芬·茨威格 精装版',NULL,39.80,1,39.80,NULL,'2026-06-05 12:35:57'),(24,19,'AI1780664730536',2,'华为 Mate 60 Pro 昆仑玻璃 卫星通话手机',NULL,7999.00,1,7999.00,NULL,'2026-06-05 21:05:31'),(25,20,'ORD202606052113045386',1,'iPhone 15 Pro Max 256GB 原色钛金属手机',NULL,9999.00,1,9999.00,NULL,'2026-06-05 21:13:04'),(26,21,'AI1780665486937',2,'华为 Mate 60 Pro 昆仑玻璃 卫星通话手机',NULL,7999.00,1,7999.00,NULL,'2026-06-05 21:18:07'),(27,22,'AI1780666310394',2,'华为 Mate 60 Pro 昆仑玻璃 卫星通话手机',NULL,7999.00,1,7999.00,NULL,'2026-06-05 21:31:50'),(28,23,'AI1780667650870',2,'华为 Mate 60 Pro 昆仑玻璃 卫星通话手机',NULL,7999.00,1,7999.00,NULL,'2026-06-05 21:54:11'),(29,24,'AI1780668368711',2,'华为 Mate 60 Pro 昆仑玻璃 卫星通话手机',NULL,7999.00,1,7999.00,NULL,'2026-06-05 22:06:09'),(30,25,'AI1780668493627',1,'iPhone 15 Pro Max 256GB 原色钛金属手机',NULL,9999.00,1,9999.00,NULL,'2026-06-05 22:08:14'),(31,26,'AI1780668881277',2,'华为 Mate 60 Pro 昆仑玻璃 卫星通话手机',NULL,7999.00,1,7999.00,NULL,'2026-06-05 22:14:41'),(32,27,'ORD202606081737230709',12,'Sony WH-1000XM5 无线降噪耳机','https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/4e768a0a0ad6411da2e8fd518106311a.webp',2499.00,2,4998.00,NULL,'2026-06-08 17:37:23'),(33,28,'AI1780912356965',2,'华为 Mate 60卫星通话手机','https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/027584970f5b4a22a66c57bc0c2dc7e6.webp',7999.00,1,7999.00,NULL,'2026-06-08 17:52:37'),(34,29,'AI1780912574435',1,'iPhone 15 ProMax 原色钛金属手机','https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/6af9ed49fb04442f88a82d5258073b42.webp',9999.00,1,9999.00,NULL,'2026-06-08 17:56:14'),(35,30,'AI1781103290600',2,'华为 Mate 60卫星通话手机','https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/027584970f5b4a22a66c57bc0c2dc7e6.webp',7999.00,1,7999.00,NULL,'2026-06-10 22:54:51'),(36,31,'AI1781103833780',2,'华为 Mate 60卫星通话手机','https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/027584970f5b4a22a66c57bc0c2dc7e6.webp',7999.00,1,7999.00,NULL,'2026-06-10 23:03:54'),(37,32,'ORD202606102305478814',15,'《人类群星闪耀时》斯蒂芬·茨威格书籍','https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/3d7b4b8056f349078c8885c9450de0b2.webp',39.80,1,39.80,NULL,'2026-06-10 23:05:47'),(38,33,'AI1781104620881',2,'华为 Mate 60卫星通话手机','https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/027584970f5b4a22a66c57bc0c2dc7e6.webp',7999.00,1,7999.00,NULL,'2026-06-10 23:17:01'),(39,34,'ORD202606102318527325',5,'纯棉圆领T恤 男女同款','https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/fba0490e036148dc9ae2e15336f880da.jpg',101.00,1,101.00,NULL,'2026-06-10 23:18:52');
/*!40000 ALTER TABLE `order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment`
--

DROP TABLE IF EXISTS `payment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `order_no` varchar(32) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '订单号（冗余）',
  `pay_amount` decimal(10,2) NOT NULL COMMENT '支付金额',
  `payment_method` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT 'MOCK_PAY' COMMENT '支付方式：BALANCE-余额支付，MOCK_PAY-模拟支付',
  `pay_status` tinyint DEFAULT '0' COMMENT '1-支付成功 0-失败/待支付',
  `remark` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '备注（记录失败原因等）',
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付记录表（追加写入，无操作人字段）';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment`
--

LOCK TABLES `payment` WRITE;
/*!40000 ALTER TABLE `payment` DISABLE KEYS */;
INSERT INTO `payment` VALUES (1,2,'2026052910002',8002.00,'MOCK_PAY',1,'模拟支付成功','2026-05-29 14:30:00','2026-05-29 14:30:00'),(2,3,'2026052810003',14997.00,'MOCK_PAY',1,'模拟支付成功','2026-05-28 11:20:00','2026-05-28 11:20:00'),(3,4,'2026052510004',251.00,'BALANCE',1,'余额支付成功','2026-05-25 16:45:00','2026-05-25 16:45:00'),(4,6,'ORD202606011551161534',349.00,'MOCK_PAY',1,'模拟支付成功','2026-06-01 15:51:16','2026-06-01 15:51:16'),(5,7,'ORD202606011621035013',1297.40,'MOCK_PAY',1,'模拟支付成功','2026-06-01 16:21:04','2026-06-01 16:21:03'),(6,8,'ORD202606011628128557',337.80,'MOCK_PAY',1,'模拟支付成功','2026-06-01 16:28:13','2026-06-01 16:28:13'),(7,9,'ORD202606011645222444',49995.00,'MOCK_PAY',1,'模拟支付成功','2026-06-01 16:45:23','2026-06-01 16:45:22'),(8,10,'ORD202606021926064138',54.90,'MOCK_PAY',1,'模拟支付成功','2026-06-02 19:26:07','2026-06-02 19:26:07'),(9,11,'ORD202606021926298049',42.80,'MOCK_PAY',1,'模拟支付成功','2026-06-02 19:26:30','2026-06-02 19:26:29'),(10,12,'ORD202606021926389625',349.00,'MOCK_PAY',1,'模拟支付成功','2026-06-02 19:26:39','2026-06-02 19:26:38'),(11,13,'ORD202606021926551911',3088.00,'MOCK_PAY',1,'模拟支付成功','2026-06-02 19:26:56','2026-06-02 19:26:55'),(12,14,'ORD202606021927075335',248.00,'MOCK_PAY',1,'模拟支付成功','2026-06-02 19:27:08','2026-06-02 19:27:07'),(13,15,'ORD202606021927285073',12999.00,'MOCK_PAY',1,'模拟支付成功','2026-06-02 19:27:29','2026-06-02 19:27:29'),(14,17,'AI1780634076729',10002.00,'MOCK_PAY',1,'模拟支付成功','2026-06-05 12:35:31','2026-06-05 12:35:31'),(15,18,'ORD202606051235572144',42.80,'MOCK_PAY',1,'模拟支付成功','2026-06-05 12:35:58','2026-06-05 12:35:57'),(16,20,'ORD202606052113045386',9999.00,'MOCK_PAY',1,'模拟支付成功','2026-06-05 21:13:05','2026-06-05 21:13:05'),(17,27,'ORD202606081737230709',4998.00,'MOCK_PAY',1,'模拟支付成功','2026-06-08 17:37:23','2026-06-08 17:37:23'),(18,29,'AI1780912574435',10002.00,'MOCK_PAY',1,'模拟支付成功','2026-06-10 22:56:51','2026-06-10 22:56:51'),(19,32,'ORD202606102305478814',42.80,'MOCK_PAY',1,'模拟支付成功','2026-06-10 23:05:48','2026-06-10 23:05:48'),(20,34,'ORD202606102318527325',101.00,'MOCK_PAY',1,'模拟支付成功','2026-06-10 23:18:53','2026-06-10 23:18:52');
/*!40000 ALTER TABLE `payment` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '商品主键，自增',
  `name` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '商品标题（用户端展示）',
  `category_id` bigint NOT NULL COMMENT '所属分类ID（关联分类表）',
  `brand` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '品牌',
  `price` decimal(10,2) NOT NULL COMMENT '销售价格（必填）',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价/划线价（用于促销展示）',
  `stock` int NOT NULL DEFAULT '0' COMMENT '当前库存数量',
  `sales` int NOT NULL DEFAULT '0' COMMENT '累计销量（默认0，下单时递增）',
  `images` json DEFAULT NULL COMMENT '商品图片列表（JSON数组，第一张为主图）',
  `description` text COLLATE utf8mb4_unicode_ci COMMENT '商品详细描述（文本）',
  `specs` json DEFAULT NULL COMMENT '商品规格定义（JSON）',
  `keywords` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '搜索关键词，逗号分隔',
  `status` tinyint DEFAULT '1' COMMENT '状态：1-上架，0-下架',
  `is_deleted` tinyint DEFAULT '0' COMMENT '逻辑删除：0-未删除，1-已删除',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID（关联 admin.id）',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人ID（关联 admin.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=16 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (1,'iPhone 15 ProMax 原色钛金属手机',2,'Apple',9999.00,11999.00,94,356,'[\"https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/6af9ed49fb04442f88a82d5258073b42.webp\"]','搭载 A17 Pro 芯片，48MP 主摄系统，钛金属设计，USB-C 接口。支持 5G 全网通，双卡双待。','[{\"name\": \"颜色\", \"values\": [\"原色钛金属\", \"白色钛金属\", \"黑色钛金属\", \"蓝色钛金属\"]}, {\"name\": \"存储\", \"values\": [\"256GB\", \"512GB\", \"1TB\"]}]','iPhone,苹果,手机,5G',1,0,1,1,'2026-01-10 10:00:00','2026-06-05 22:39:00'),(2,'华为 Mate 60卫星通话手机',2,'华为',7999.00,16999.00,80,280,'[\"https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/027584970f5b4a22a66c57bc0c2dc7e6.webp\"]','麒麟 9000S 芯片，卫星通话功能，昆仑玻璃面板，XMAGE 影像系统。','[{\"name\": \"颜色\", \"values\": [\"白沙银\", \"雅丹黑\", \"南糯紫\", \"雅川青\"]}, {\"name\": \"存储\", \"values\": [\"256GB\", \"512GB\", \"1TB\"]}]','华为,手机,卫星通话,5G',1,0,1,1,'2026-01-15 10:00:00','2026-06-05 22:39:00'),(3,'MacBook Pro笔记本',3,'Apple',14999.00,16999.00,50,180,'[\"https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/b6eddf33e7444683bbbc09bdd3e13d4f.jpg\"]','M3 Pro 芯片，18GB 统一内存，512GB 固态硬盘，Liquid Retina XDR 显示屏。','[{\"name\": \"芯片\", \"values\": [\"M3 Pro\", \"M3 Max\"]}, {\"name\": \"内存\", \"values\": [\"18GB\", \"36GB\"]}, {\"name\": \"存储\", \"values\": [\"512GB\", \"1TB\", \"2TB\"]}]','MacBook,苹果,笔记本,M3',1,0,1,1,'2026-01-20 10:00:00','2026-06-05 22:39:00'),(4,'ThinkPad X1 Carbon Gen笔记本',3,'联想',12999.00,11999.00,29,96,'[\"https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/ea2e9555496842fd8266a615d1d53e95.webp\"]','第13代 Intel Core i7 处理器，16GB LPDDR5 内存，512GB SSD，14英寸 2.8K OLED 屏。','[{\"name\": \"屏幕\", \"values\": [\"2.8K OLED\", \"1920x1200 IPS\"]}, {\"name\": \"内存\", \"values\": [\"16GB\", \"32GB\"]}]','ThinkPad,联想,商务本,i7',1,0,1,1,'2026-02-01 10:00:00','2026-06-05 22:47:00'),(5,'纯棉圆领T恤 男女同款',5,'优衣库',101.00,199.00,199,1201,'[\"https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/fba0490e036148dc9ae2e15336f880da.jpg\"]','100% 纯棉面料，宽松版型，舒适透气。黑白灰基础色，四季百搭单品。','[{\"name\": \"颜色\", \"values\": [\"白色\", \"黑色\", \"藏青\"]}, {\"name\": \"尺码\", \"values\": [\"S\", \"M\", \"L\", \"XL\", \"XXL\"]}]','T恤,纯棉,男装,基础款',1,0,1,1,'2026-02-10 10:00:00','2026-06-05 22:47:00'),(6,'碎花连衣裙 法式复古风裙',6,'ZARA',200.00,399.00,150,860,'[\"https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/c002d00a4ae841f69577bda1a3a86af9.webp\"]','法式碎花元素，高腰设计显腿长，A字裙摆遮肉显瘦。适合春夏出游。','[{\"name\": \"颜色\", \"values\": [\"碎花蓝底\", \"碎花粉底\"]}, {\"name\": \"尺码\", \"values\": [\"S\", \"M\", \"L\", \"XL\"]}]','连衣裙,碎花,女装,法式',1,0,1,1,'2026-02-15 10:00:00','2026-06-05 22:47:00'),(8,'良品铺子 肉干大礼包 混合装 500g',8,'良品铺子',51.90,89.00,299,3801,'[\"https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/00ae63c565c44cfaa86c33aa902007fe.jpg\"]','牛肉干+猪肉脯+鸡肉条混合装，独立小包装。追剧零食首选。','[{\"name\": \"颜色\", \"values\": [\"白色\", \"黑色\", \"藏青\"]}, {\"name\": \"尺码\", \"values\": [\"S\", \"M\", \"L\", \"XL\", \"XXL\"]}]','肉干,零食,大礼包,良品铺子',1,0,1,1,'2026-02-25 10:00:00','2026-05-28 15:00:00'),(9,'资生堂 红腰子精华口红',6,'资生堂',589.00,890.00,77,683,'[\"https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/6dab2e36096043d4bbafb81d9eb3743c.webp\"]','明星红腰子精华，提升肌肤免疫力，修护维稳。限定版 75ml 大容量。','[{\"name\": \"容量\", \"values\": [\"75ml\", \"50ml\", \"30ml\"]}]','精华,资生堂,护肤,红腰子',1,0,1,1,'2026-03-01 10:00:00','2026-05-28 15:00:00'),(10,'无印良品 超声波香薰机',1,'无印良品',248.00,350.00,119,421,'[\"https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/36d9035ddc4946969096b937882ad621.webp\"]','超声波雾化技术，安静运行。400ml 大容量，可定时关闭。LED 暖光照明。','[{\"name\": \"颜色\", \"values\": [\"白色\", \"深灰\"]}]','香薰机,无印良品,家居,超声波',1,0,1,1,'2026-03-05 10:00:00','2026-05-28 15:00:00'),(11,'武夷山大红袍 特级岩茶 礼盒装500g',8,'武夷星',298.00,498.00,59,321,'[\"https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/1fa068ef080943a5abdc625e99f1f1fa.webp\"]','武夷山正岩产区，传统碳焙工艺。条索紧结，岩韵悠长。送礼自饮皆宜。','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"铂金银\", \"午夜蓝\"]}]','大红袍,岩茶,礼盒,武夷山',1,0,1,1,'2026-03-10 10:00:00','2026-05-28 15:00:00'),(12,'Sony WH-1000XM5 无线降噪耳机',1,'Sony',2499.00,2999.00,67,563,'[\"https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/4e768a0a0ad6411da2e8fd518106311a.webp\"]','行业顶级降噪，30小时续航，多点连接。舒适佩戴，Hi-Res 高解析音质。','[{\"name\": \"颜色\", \"values\": [\"黑色\", \"铂金银\", \"午夜蓝\"]}]','耳机,降噪,Sony,无线',1,0,1,1,'2026-03-15 10:00:00','2026-05-28 15:00:00'),(13,'戴森 V15 Detect 无线吸尘器',1,'戴森',4590.00,4990.00,40,230,'[\"https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/4d219be3a4e844efa04d6bf3570ced90.webp\"]','激光探测技术，智能灰尘感应。260AW 强劲吸力，整机 HEPA 过滤。60 分钟续航。','[{\"name\": \"型号\", \"values\": [\"V15 Detect\", \"V12 Detect Slim\"]}]','吸尘器,戴森,无线,家用',1,0,1,1,'2026-03-20 10:00:00','2026-05-28 15:00:00'),(14,'乐高 兰博基尼 Sián FKP 37 科技系列',1,'乐高',349.00,449.00,198,892,'[\"https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/f8ae077859e34847872e5a9f747b79f2.webp\"]','1:16 比例复刻，可动 V8 引擎，蝴蝶门设计。463 颗粒，适合 9+ 岁。','[{\"name\": \"颜色\", \"values\": [\"白色\", \"黑色\", \"藏青\"]}, {\"name\": \"尺码\", \"values\": [\"S\", \"M\", \"L\", \"XL\", \"XXL\"]}]','乐高,兰博基尼,科技系列,积木',1,0,1,1,'2026-04-01 10:00:00','2026-05-28 15:00:00'),(15,'《人类群星闪耀时》斯蒂芬·茨威格书籍',1,'果麦文化',39.80,59.80,494,1506,'[\"https://yueyun-e.oss-cn-beijing.aliyuncs.com/images/product/2026/06/3d7b4b8056f349078c8885c9450de0b2.webp\"]','茨威格传世之作，14 个改变人类命运的历史瞬间。全新精装典藏版。','[{\"name\": \"颜色\", \"values\": [\"白色\", \"黑色\", \"藏青\"]}, {\"name\": \"尺码\", \"values\": [\"S\", \"M\", \"L\", \"XL\", \"XXL\"]}]','茨威格,历史,经典,精装书',1,0,1,1,'2026-04-10 10:00:00','2026-06-05 22:47:00');
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `review`
--

DROP TABLE IF EXISTS `review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `review` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_id` bigint NOT NULL COMMENT '订单ID',
  `user_id` bigint NOT NULL COMMENT '评价用户ID',
  `product_id` bigint NOT NULL COMMENT '商品ID',
  `rating` tinyint NOT NULL COMMENT '评分 1-5',
  `content` text COLLATE utf8mb4_unicode_ci COMMENT '评价内容',
  `images` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '评价图片，JSON数组',
  `is_anonymous` tinyint DEFAULT '0' COMMENT '是否匿名 0-否 1-是',
  `status` tinyint DEFAULT '1' COMMENT '审核状态：1-显示，0-隐藏',
  `create_by` bigint DEFAULT NULL COMMENT '创建人ID（关联 user.id）',
  `update_by` bigint DEFAULT NULL COMMENT '最后操作人ID（关联 admin.id）',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '评价创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_user_product` (`order_id`,`user_id`,`product_id`) COMMENT '同一用户对同一订单中的同一商品只能评价一次',
  KEY `idx_product_id` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评价表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `review`
--

LOCK TABLES `review` WRITE;
/*!40000 ALTER TABLE `review` DISABLE KEYS */;
INSERT INTO `review` VALUES (1,4,5,7,5,'坚果品质不错，种类丰富，包装精美。送给父母的，他们很满意！',NULL,0,1,5,5,'2026-05-26 10:00:00','2026-05-26 10:00:00'),(2,4,5,8,4,'肉干味道很好，就是份量稍微少了点。整体还不错，会回购。',NULL,0,1,5,5,'2026-05-26 10:05:00','2026-05-26 10:05:00'),(3,4,5,15,5,'茨威格的作品百读不厌，这本书的装帧也很精美，值得收藏。',NULL,1,1,5,5,'2026-05-27 08:00:00','2026-06-01 10:52:32'),(4,3,1,5,5,'纯棉材质很舒服，白色百搭。买的 L 码刚好合适，推荐购买！',NULL,0,1,1,1,'2026-05-29 09:00:00','2026-06-01 10:52:32'),(5,27,1,12,5,'还行',NULL,0,1,1,1,'2026-06-09 11:39:37','2026-06-09 11:39:37'),(6,34,1,5,5,'用的很不错',NULL,0,1,1,1,'2026-06-10 23:22:16','2026-06-10 23:22:16');
/*!40000 ALTER TABLE `review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '主键，自增',
  `username` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '登录名，唯一',
  `password` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '加密后密码（BCrypt）',
  `nickname` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '用户昵称/显示名',
  `avatar` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '头像 URL',
  `phone` varchar(20) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '手机号，可用于登录或短信通知',
  `email` varchar(50) COLLATE utf8mb4_unicode_ci DEFAULT NULL COMMENT '邮箱',
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
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user`
--

LOCK TABLES `user` WRITE;
/*!40000 ALTER TABLE `user` DISABLE KEYS */;
INSERT INTO `user` VALUES (1,'zhangsan','$2a$10$YNGgbdsInfW0EtS0Vf4jd.0DKze47rz4LUQBWfSMOQwdFZpoN/HD.','张三',NULL,'13812348881','zhangsan@example.com',1,'1995-06-15',1,'2026-06-11 11:37:41',0,0,1,'2026-01-10 10:00:00','2026-05-31 09:15:00'),(2,'lisi','$2a$10$o4gTGZyKYpbQHPRfgjf3z.zYQW3NwNJaioVcYxmUJh6lb1Kf7oVFK','李四',NULL,'13812348882','lisi@qq.com',2,'1998-03-22',1,'2026-05-30 14:30:00',0,0,1,'2026-03-15 09:00:00','2026-05-30 14:30:00'),(3,'wangwu','$2a$10$S7A1I.CKkGedqAA7.qEp6uN/4qDWi/Xpu5AVqScKhgghZb8t.XtQy','王五',NULL,'13812348883','wangwu@163.com',1,'1992-11-08',0,'2026-05-25 18:00:00',0,0,2,'2025-06-20 08:30:00','2026-05-26 10:00:00'),(4,'zhaoliu','$2a$10$s/0KbF.xChuxEUogOf/Yw.0B6/T0vtoyCAtE/cSP7tdl3YsxQUdS6','赵六',NULL,'13812348884','zhaoliu@gmail.com',1,'2000-01-01',1,'2026-05-29 11:20:00',0,0,1,'2025-09-01 14:00:00','2026-05-29 11:20:00'),(5,'sunqi','$2a$10$QyJq5kA0qGTFHBdRv2mu0uRh0kE8DlH8BZYUsRqyUqEUpXMY0PkP2','孙七',NULL,'13812348885','sunqi@example.com',1,'1997-07-30',1,'2026-05-28 20:15:00',0,0,1,'2026-04-01 11:00:00','2026-05-28 20:15:00'),(6,'chenmeng','$2a$10$nL3YlBhubMZz.J/QduXvDuqt9RU21qnMnD9xviQqoMsV9.hpNJlu6','陈萌',NULL,'13812348886','chenmeng@sina.com',2,'1999-12-12',1,'2026-05-27 16:45:00',0,0,1,'2026-04-15 10:00:00','2026-05-27 16:45:00');
/*!40000 ALTER TABLE `user` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-06-12 11:42:00
