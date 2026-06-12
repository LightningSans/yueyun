# 悦选配送 - 微信小程序启动指南

## 前提条件

1. **后端已启动**（Spring Boot 运行在 localhost:8080）
   - MySQL 8.0 + Redis 7.x 需先启动
   - 后端有默认配送员数据：`courier01` / `123456`

2. **微信开发者工具**已安装

---

## 方式一：直接使用编译后代码（推荐）

项目中的 `dist/dev/mp-weixin/` 已经是编译好的微信小程序格式。

**操作步骤：**

1. 打开 **微信开发者工具**
2. 点击 **"导入项目"**（不是"打开项目"）
3. 目录选择：`yueyun_curUniapp/dist/dev/mp-weixin/`
4. AppID 选择 **"测试号"**（使用测试号无需真实 appid）
   - 或填入你的真实微信小程序 AppID
5. 点击 **"导入"**
6. 项目启动后，会自动显示登录页面
7. 默认账号：`courier01` / 密码：`123456`

> ⚠️ 如果后端没有启动，小程序请求会失败，显示"网络异常"提示

---

## 方式二：使用 HBuilderX 编译（有源码可修改）

如果你需要修改源码（`.vue` 文件），使用此方式：

1. 打开 **HBuilderX**
2. 菜单 → **文件** → **导入** → 选择 `yueyun_curUniapp` 目录
3. 菜单 → **运行** → **运行到小程序模拟器** → **微信开发者工具**
4. HBuilderX 会自动编译并打开微信开发者工具
5. 之后修改 `.vue` 文件，保存即自动刷新

---

## 配置说明

| 配置项 | 文件 | 说明 |
|-------|------|------|
| API 地址 | `utils/config.js` `BASE_URL` | 默认 `http://localhost:8080/api` |
| 微信 appid | `manifest.json` `mp-weixin.appid` | 为空时使用 mock 登录 |
| 后端微信 | `application-dev.yml` `wechat.*` | 为空时自动 mock 登录 |
| 支付方式 | 后端 `payment_method: MOCK_PAY` | 模拟支付，无需真实 API |

---

## 后端 API 验证

启动后端后，可用 curl 验证：

```bash
# 配送员登录
curl -X POST http://localhost:8080/api/courier/login \
  -H "Content-Type: application/json" \
  -d '{"username":"courier01","password":"123456"}'

# 返回示例（含 JWT token）
{"code":200,"msg":"success","data":{"token":"eyJ...","courierInfo":{...}}}
```

---

## 常见问题

**Q: 微信开发者工具打开后白屏 / 无反应？**
A: 确认你导入的是 `dist/dev/mp-weixin/` 目录，而不是源码根目录。看看控制台有没有报错。

**Q: 登录失败，显示"网络错误"？**
A: 先单独启动后端（Spring Boot），确认 localhost:8080 可访问。使用 curl 测试。

**Q: 提示"appid 无效"？**
A: 在 `project.config.json` 中修改 `appid` 为你的测试号 appid，或者在导入时选择"测试号"。

**Q: 想要在手机上预览？**
A: 需要将后端的 localhost 替换为局域网 IP（如 `http://192.168.x.x:8080/api`），然后在 `utils/config.js` 中修改 `BASE_URL`，重新编译。
