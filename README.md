# 悦选智能商城 (YueXuan Mall)

> 基于 Spring Boot 3.3 + Vue 3 的电商单体系统，集成 AI 智能客服、多角色权限体系，覆盖完整线上购物与后台管理流程。

---

## 目录

- [项目简介](#项目简介)
- [功能特性](#功能特性)
- [技术栈](#技术栈)
- [项目结构](#项目结构)
- [快速启动](#快速启动)
- [配置说明](#配置说明)
- [默认账号](#默认账号)
- [订单状态流转](#订单状态流转)
- [角色权限](#角色权限)
- [API 文档](#api-文档)
- [安全注意事项](#安全注意事项)

---

## 项目简介

悦选商城是一个全功能电商系统，包含三个前端终端和一套后端服务：

| 终端 | 技术 | 说明 |
|------|------|------|
| 用户 Web 端 | Vue 3 + TypeScript + Vite + Pinia | 商品浏览、购物车、下单、评价、AI 客服 |
| 后台管理端 | Vue 3 + Element Plus + ECharts | 商品/订单/用户/配送管理、数据统计 |
| 配送员小程序 | UniApp (Vue 2) 微信小程序 | 接单、取货、配送、完成操作 |

后端为 Spring Boot 3.3 单体架构，内嵌 AI 多智能体客服系统。

---

## 功能特性

### 用户端功能
- **用户注册与登录**：用户名/手机号注册登录，JWT 无状态认证
- **商品浏览**：分类浏览、商品列表、商品详情、多规格展示
- **购物车**：添加/删除/修改商品数量、勾选结算
- **收货地址管理**：新增/编辑/删除地址，支持默认地址
- **下单与支付**：创建订单、模拟支付、余额支付
- **订单管理**：订单列表/详情、取消订单、确认收货、删除订单
- **商品评价**：1-5 星评分、文字评价
- **AI 智能客服**：多智能体路由对话、商品推荐、订单查询、售后处理

### 管理端功能
- **工作台 Dashboard**：今日订单、待处理数据总览、订单趋势图
- **用户管理**：用户列表/搜索、冻结/解冻、信息修改
- **管理员管理**（超级管理员）：管理员 CRUD、启用/禁用
- **分类管理**：树形分类、无限级子分类
- **商品管理**：商品 CRUD、上下架、批量上下架
- **订单管理**：订单搜索/详情、指派配送员
- **配送员管理**：配送员 CRUD、启用/禁用、密码重置
- **支付记录**：支付流水查看（只读）
- **评价管理**：评价列表搜索、隐藏/显示、批量操作
- **数据统计**：数据概览、月度订单趋势、热销商品 Top 10

### 配送员端功能（微信小程序）
- **登录**：用户名密码登录 / 微信 code 登录
- **任务列表**：待取货/配送中/已送达/已完成
- **任务详情**：取货地址、配送地址、商品明细
- **取货/送达操作**：状态流转确认
- **配送统计**：今日订单、完成数、收入统计

### AI 客服亮点
- **多智能体路由**：IntentRouterAgent 分析用户意图，分发给对应专家 Agent（商品推荐、订单查询、物流、售后、闲聊）
- **Function Calling**：注册 7 个 Tool 函数（商品搜索、下单、查订单、取消订单、退货等），AI 自主决策调用
- **流式 SSE 输出**：1001 文本块 → 1003 商品卡片 → 1002 结束事件，打字机效果
- **RAG 知识库**：基于商品知识库的语义检索增强
- **Redis 记忆存储**：对话上下文持久化，7 天有效
- **自动标题生成**：首轮对话后 AI 自动生成会话标题

---

## 技术栈

### 后端核心

| 层次 | 技术 | 用途 |
|------|------|------|
| 基础框架 | Spring Boot 3.3.5 + Java 17 + Maven | 项目骨架、依赖管理 |
| ORM | MyBatis-Plus 3.5.7 | 增强型 ORM、分页插件 |
| 数据库 | MySQL 8.0 + HikariCP 连接池 | 主业务数据库 |
| 缓存 | Redis 7.x + Spring Data Redis (Lettuce) | 热点缓存、购物车、AI 记忆 |
| 安全 | Spring Security + JWT (jjwt 0.12.6) | 无状态认证，四角色体系 |
| AI 集成 | Spring AI 1.0.0-M6 + DashScope SDK 2.16.2 | 多智能体路由 + Function Calling |
| LLM | DeepSeek Chat（OpenAI 兼容协议） | 意图路由、对话生成、标题生成 |
| 文件存储 | 阿里云 OSS | 商品图片、评价图片、用户头像 |
| API 文档 | SpringDoc OpenAPI 2.3.0 (Swagger UI) | 自动生成接口文档 |
| 工具库 | Hutool 5.8.34, Lombok, Jackson | 工具类、代码简化、JSON 序列化 |
| 定时任务 | Spring @Scheduled | 超时取消订单、清理旧会话 |

### 前端终端

| 终端 | 技术 |
|------|------|
| 用户 Web 端 | Vue 3.4 + TypeScript + Vite 5 + Pinia + Axios |
| 后台管理端 | Vue 3.4 + Element Plus 2.7 + ECharts 5.5 + Vite 5 + Pinia + Axios |
| 配送员小程序 | UniApp (Vue 2) + 微信小程序 |

---

## 项目结构

```
cluadestack/
├── README.md                          # 本文件
│
└── yueyun_fronted/                    # 后端单体核心（Spring Boot + Maven）
    ├── pom.xml                        # Maven 构建（Spring Boot 3.3.5, Java 17）
    ├── .gitignore                     # Git 忽略规则（敏感配置不提交）
    ├── src/main/
    │   ├── java/com/yuexuan/mall/
    │   │   ├── YueXuanMallApplication.java    # 启动入口
    │   │   ├── common/                        # 通用工具（统一响应 R、异常处理、ResultCode）
    │   │   ├── config/                        # 配置类（CORS、MyBatis-Plus、Redis、AI、OSS 等）
    │   │   │   └── DataInitializer.java       # 首次启动自动创建默认管理员/配送员账号
    │   │   ├── security/                      # 安全认证（JWT 令牌、Spring Security 配置、过滤链）
    │   │   ├── controller/                    # REST 控制器
    │   │   │   ├── user/                      # 用户端 API（商品、购物车、地址、下单、评价）
    │   │   │   ├── admin/                     # 管理端 API（商品管理、订单管理、用户管理等）
    │   │   │   ├── courier/                   # 配送员端 API（任务列表、状态流转）
    │   │   │   ├── AiCustomerController.java  # AI 客服流式对话（SSE）
    │   │   │   └── FileController.java        # 文件上传（OSS）
    │   │   ├── entity/po/                     # 数据实体（User、Admin、Product、Order 等 11 张表）
    │   │   ├── mapper/                        # MyBatis Mapper 接口
    │   │   └── service/                       # 服务接口 + 实现
    │   │       └── impl/
    │   │           ├── IntentRouterAgent.java  # AI 多智能体意图路由
    │   │           ├── AgentPromptFactory.java # AI Agent 提示词工厂
    │   │           └── ...                     # 各模块 ServiceImpl
    │   └── resources/
    │       ├── application.yml                 # 公共配置（服务端口、Spring AI、Jackson）
    │       ├── application-dev.yml             # ⚠ 已加入 .gitignore，需自行创建
    │       ├── application-dev.yml.example     # 配置模板文件，复制后修改即可
    │       ├── init.sql                        # 数据库建表脚本（MySQL）
    │       ├── mapper/                         # MyBatis XML 映射文件
    │       └── logback-spring.xml              # 日志配置
    │
    ├── yueyun_AdminUI/                # 管理后台前端（Vue 3 + Element Plus + ECharts）
    │   ├── package.json
    │   └── src/views/                 # 订单管理、用户管理、商品管理、配送管理、数据统计等
    │
    ├── yueyun_userUI/                 # 用户前端（Vue 3 + 自定义 UI）
    │   ├── package.json
    │   └── src/views/                 # 首页、商品列表/详情、购物车、下单、订单详情等
    │
    └── yueyun_curUniapp/              # 配送员小程序（UniApp + 微信小程序）
        └── ...                        # 页面：登录、任务列表、任务详情、配送统计
```

### 数据库表结构（共 11 张表）

| 表名 | 说明 |
|------|------|
| `user` | 用户表（买家） |
| `admin` | 管理员表（SUPER_ADMIN / ADMIN） |
| `category` | 商品分类（树形，无限级子分类） |
| `product` | 商品表（价格、库存、规格 JSON、图片 JSON） |
| `cart` | 购物车（用户-商品唯一索引） |
| `address` | 收货地址（支持多地址、默认地址） |
| `order` | 订单表（完整的状态流转） |
| `order_item` | 订单明细（只读快照） |
| `payment` | 支付记录（追加写入） |
| `courier` | 配送员表 |
| `review` | 商品评价（1-5 星评分） |

完整建表 SQL 见 [`yueyun_fronted/src/main/resources/init.sql`](yueyun_fronted/src/main/resources/init.sql)。

---

## 快速启动

### 环境要求

| 工具 | 版本要求 |
|------|---------|
| JDK | 17+ |
| Maven | 3.8+ |
| MySQL | 8.0+ |
| Redis | 7.x |
| Node.js | 18+ |

### 1. 创建数据库

执行建表脚本：

```sql
SOURCE yueyun_fronted/src/main/resources/init.sql;
```

或登录 MySQL 后直接执行：

```sql
CREATE DATABASE IF NOT EXISTS `yueyun`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE `yueyun`;
-- 然后执行 init.sql 中的建表语句
```

### 2. 配置后端（重要）

`application-dev.yml` 包含数据库密码、Redis 密码、JWT 密钥等敏感信息，**已加入 `.gitignore`，不会被提交到 Git**，因此第一次克隆后需要手动创建。

复制模板文件并编辑：

```bash
cd yueyun_fronted
cp src/main/resources/application-dev.yml.example src/main/resources/application-dev.yml
```

然后用你的本地配置修改 `application-dev.yml`，至少需要修改：

```yaml
spring:
  datasource:
    username: root
    password: 你的MySQL密码        # ← 必须修改
  data:
    redis:
      password: 你的Redis密码       # ← 如无密码则留空

jwt:
  secret: 一个随机字符串            # ← 建议修改为随机字符串
```

### 3. 设置环境变量

后端运行需要以下环境变量：

| 变量 | 说明 | 必需 |
|------|------|------|
| `DEEPSEEK_API_KEY` | DeepSeek API 密钥（AI 功能） | AI 功能需要 |
| `OSS_ACCESS_KEY_ID` | 阿里云 OSS AccessKey ID（文件上传） | 上传功能需要 |
| `OSS_ACCESS_KEY_SECRET` | 阿里云 OSS AccessKey Secret | 上传功能需要 |
| `WECHAT_APP_ID` | 微信小程序 AppID（配送员端） | 可选 |
| `WECHAT_APP_SECRET` | 微信小程序 Secret | 可选 |

设置方式（Linux/Mac）：

```bash
export DEEPSEEK_API_KEY=your_deepseek_api_key
export OSS_ACCESS_KEY_ID=your_oss_key
export OSS_ACCESS_KEY_SECRET=your_oss_secret
```

如暂无 AI 或 OSS 密钥，可在 `application-dev.yml` 中临时注释相关配置，不影响基本电商功能。

### 4. 启动后端

```bash
cd yueyun_fronted
mvn clean install -DskipTests
mvn spring-boot:run
```

后端默认运行在 **http://localhost:8080**

Swagger 接口文档：http://localhost:8080/swagger-ui.html

### 5. 启动管理后台前端

```bash
cd yueyun_fronted/yueyun_AdminUI
npm install
npm run dev
```

运行在 **http://localhost:3000**，自动代理 `/api` 请求到后端 8080 端口。

### 6. 启动用户前端

```bash
cd yueyun_fronted/yueyun_userUI
npm install
npm run dev
```

运行在 **http://localhost:3001**，同样代理 `/api` 到后端。

### 7. 配送员小程序（开发模式）

使用 HBuilderX 打开 `yueyun_fronted/yueyun_curUniapp` 目录，运行到微信开发者工具。

---

## 配置说明

### 关键配置项（application-dev.yml）

```yaml
spring.datasource.url: jdbc:mysql://localhost:3306/yueyun?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
spring.datasource.username: root
spring.datasource.password: ${DB_PASSWORD:your_password_here}
spring.data.redis.host: localhost
spring.data.redis.port: 6379
spring.data.redis.password: ${REDIS_PASSWORD:your_password_here}
jwt.secret: ${JWT_SECRET:your_jwt_secret_here}
jwt.expiration: 10800000            # 3 小时（毫秒）
spring.ai.openai.api-key: ${DEEPSEEK_API_KEY}
spring.ai.openai.base-url: https://api.deepseek.com
aliyun.oss.endpoint: https://oss-cn-beijing.aliyuncs.com
aliyun.oss.bucket-name: yueyun-e
```

### 配置方式说明

所有敏感配置项支持两种注入方式：
- **直接填写**：在 `application-dev.yml` 中填入真实值（不提交 Git）
- **环境变量**：使用 `${VAR_NAME:default}` 格式，运行时通过环境变量注入（推荐）

模板文件 `application-dev.yml.example` 已包含完整的配置项结构，复制后只需替换密码即可。

---

## 默认账号

项目首次启动时（数据库无数据），`DataInitializer.java` 会自动创建以下默认账号：

### 管理员

| 用户名 | 密码 | 角色 |
|--------|------|------|
| `superadmin` | `admin123` | 超级管理员（拥有全部权限） |
| `admin01` | `admin123` | 普通管理员 |
| `admin02` | `admin123` | 普通管理员 |

### 配送员

| 用户名 | 密码 | 昵称 |
|--------|------|------|
| `courier01` | `123456` | 快递小王 |
| `courier02` | `123456` | 快递小李 |

> 用户（买家）需通过前端注册页面自行注册，无系统预设账号。

---

## 订单状态流转

```
PENDING_PAYMENT ──→ PENDING_DELIVERY ──→ ASSIGNED ──→ IN_TRANSIT ──→ DELIVERED ──→ COMPLETED
      │                    │                                                │
      ▼                    ▼                                                ▼
   CANCELLED            CANCELLED                                       REFUNDING
```

| 步骤 | 操作方 | 说明 |
|------|--------|------|
| PENDING_PAYMENT | 用户 | 下单后待支付，超时自动取消 |
| PENDING_DELIVERY | 管理员 | 支付成功，待发货 |
| ASSIGNED | 管理员 | 已指派配送员 |
| IN_TRANSIT | 配送员 | 配送中 |
| DELIVERED | 配送员 | 已送达 |
| COMPLETED | 用户 | 确认收货，流程结束 |
| CANCELLED | 用户/系统 | 支付前可取消，超时自动取消 |
| REFUNDING | 用户 | 售后退款（配送完成后） |

---

## 角色权限

| 角色 | 说明 | 可访问接口 |
|------|------|-----------|
| ROLE_USER | 普通用户（买家） | 用户端全部 API |
| ROLE_ADMIN | 普通管理员 | 管理端 API（除管理员管理） |
| ROLE_SUPER_ADMIN | 超级管理员 | 全部管理端 API |
| ROLE_COURIER | 配送员 | 配送员端 API |
| 匿名（未登录） | 游客 | 商品浏览、AI 对话 |

---

## API 文档

启动后端后访问 http://localhost:8080/swagger-ui.html 查看完整 API 文档。

### 主要 API 概览

| 模块 | 方法 | 路径 | 说明 |
|------|------|------|------|
| 认证 | POST | `/api/user/login` | 用户登录 |
| 认证 | POST | `/api/user/register` | 用户注册 |
| 商品 | GET | `/api/product/list` | 商品列表（支持分类筛选） |
| 商品 | GET | `/api/product/detail/{id}` | 商品详情 |
| 购物车 | GET | `/api/cart/list` | 购物车列表 |
| 购物车 | POST | `/api/cart/add` | 加入购物车 |
| 地址 | GET/POST/PUT/DELETE | `/api/address/*` | 地址 CRUD |
| 订单 | POST | `/api/order/create` | 创建订单 |
| 订单 | GET | `/api/order/list` | 订单列表 |
| 评价 | POST | `/api/review/create` | 创建评价 |
| 评价 | GET | `/api/review/list/{productId}` | 评价列表 |
| AI 客服 | POST | `/api/ai/chat` | 流式对话（SSE） |
| AI 客服 | GET | `/api/ai/hot-questions` | 热点问题 |
| 管理登录 | POST | `/api/admin/login` | 管理员登录 |
| 统计 | GET | `/api/admin/statistics/overview` | 数据概览 |
| 配送登录 | POST | `/api/courier/login` | 配送员登录 |

---

## 安全注意事项

### 已加入 .gitignore 的敏感文件

以下文件不会被提交到 Gitee：

| 文件 | 原因 |
|------|------|
| `application-dev.yml` | 包含数据库密码、Redis 密码、JWT 密钥 |
| `application-prod.yml` / `*.local.yml` | 生产/本地环境配置 |
| `target/` | Maven 构建产物 |
| `node_modules/` | npm 依赖包 |
| `logs/` | 运行日志含业务数据 |
| `.idea/` | IDE 配置含数据库连接信息 |

### 首次克隆后的必要操作

1. **创建 `application-dev.yml`** — 从 `application-dev.yml.example` 复制并填入本地配置
2. **设置环境变量** — 配置 DEEPSEEK_API_KEY、OSS 密钥等
3. **创建数据库并建表** — 执行 `init.sql`
4. **默认账号** — 项目首次启动会自动创建管理员和配送员账号，无需手动插入数据

### 关于 .gitignore

`.gitignore` 文件位于 `yueyun_fronted/.gitignore`（不在根目录），覆盖了 Maven 构建产物、npm 依赖、IDE 配置、日志文件和环境配置等。如果需要在根目录添加额外忽略规则，请自行在项目根目录创建 `.gitignore` 文件。
