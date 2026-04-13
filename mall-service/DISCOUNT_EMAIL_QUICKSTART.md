# 折扣通知邮件 - 快速开始

## 🚀 5分钟快速体验

### 前置条件

1. ✅ mall-service 正在运行（端口 8081）
2. ✅ MySQL、Redis、RabbitMQ 已启动
3. ✅ 至少有一个用户绑定了邮箱并添加了愿望单

---

## 📝 步骤1：准备测试数据

### 1.1 确保用户有邮箱

```sql
-- 查询用户邮箱
SELECT id, username, email FROM users WHERE id = 1;

-- 如果没有邮箱，更新一个
UPDATE users SET email = 'test@example.com' WHERE id = 1;
```

### 1.2 添加游戏到愿望单并开启通知

**方法一：通过前端界面**
1. 登录用户账号
2. 进入游戏详情页
3. 点击"添加到愿望单"
4. 勾选"折扣通知"选项

**方法二：直接调用API**
```bash
curl -X POST "http://localhost:8081/api/wishlist?userId=1&gameId=1&notifyDiscount=true"
```

### 1.3 验证愿望单设置

```sql
SELECT w.*, g.title 
FROM wishlists w
JOIN games g ON w.game_id = g.id
WHERE w.user_id = 1 AND w.notify_discount = 1;
```

应该看到类似结果：
```
+----+---------+---------+----------+-----------------+----------------+------------+---------------------+---------------------+-----------+
| id | user_id | game_id | priority | notify_discount | notify_release | created_at | updated_at          | title               |
+----+---------+---------+----------+-----------------+----------------+------------+---------------------+---------------------+-----------+
|  1 |       1 |       1 |        1 |               1 |              1 | ...        | ...                 | 赛博朋克2077         |
+----+---------+---------+----------+-----------------+----------------+------------+---------------------+---------------------+-----------+
```

---

## 🔧 步骤2：配置邮件服务

### 2.1 检查当前配置

打开 `mall-service/src/main/resources/application.yml`，确认邮件配置：

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 587
    username: 1712133303@qq.com
    password: xwxqwrqawznddaij  # ⚠️ 需要替换为你的SMTP授权码
```

### 2.2 获取QQ邮箱SMTP授权码

1. 登录 [QQ邮箱](https://mail.qq.com)
2. 点击 **设置** → **账户**
3. 找到 **POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务**
4. 开启 **IMAP/SMTP服务**
5. 点击 **生成授权码**
6. 复制16位授权码

### 2.3 更新配置

将授权码填入配置文件：

```yaml
spring:
  mail:
    password: 你的16位授权码  # 替换这里
```

或者使用环境变量（推荐）：

```bash
# 创建或编辑 .env 文件
echo "MAIL_PASSWORD=你的16位授权码" >> mall-service/.env
```

然后修改 `application.yml`：

```yaml
spring:
  mail:
    password: ${MAIL_PASSWORD}
```

---

## 🎯 步骤3：测试折扣更新

### 方法一：使用测试脚本（推荐）

```powershell
cd mall-service
.\test-discount-notification.ps1
```

脚本会自动：
- ✅ 查询游戏当前信息
- ✅ 更新折扣（从20%提升到50%）
- ✅ 触发邮件通知
- ✅ 提示检查结果

### 方法二：手动调用API

```bash
# 更新游戏ID为1的折扣
curl -X PUT http://localhost:8081/api/admin/games/1/discount \
  -H "Content-Type: application/json" \
  -d '{
    "discountRate": 50,
    "currentPrice": 149.00
  }'
```

**预期响应：**
```json
{
  "code": 200,
  "message": "折扣更新成功，已通知相关用户",
  "data": null
}
```

---

## 📬 步骤4：验证邮件发送

### 4.1 检查应用日志

```powershell
# PowerShell
Get-Content mall-service\logs\mall-startup.log -Tail 50

# 或使用 grep
grep "折扣" mall-service/logs/mall-startup.log
```

**成功日志示例：**
```
2026-04-11 10:30:15 [task-1] INFO  c.c.m.s.EmailNotificationService - 检测到折扣提升，准备发送通知: gameId=1, oldDiscount=20%, newDiscount=50%
2026-04-11 10:30:16 [task-1] INFO  c.c.m.s.EmailNotificationService - 折扣邮件发送成功: test@example.com
2026-04-11 10:30:16 [task-1] INFO  c.c.m.s.EmailNotificationService - 折扣通知邮件发送完成，游戏ID: 1, 折扣: 50%
```

### 4.2 检查邮箱

登录用户的邮箱，查找主题为 **"🎮 【折扣提醒】赛博朋克2077 现在打折啦！"** 的邮件。

**邮件内容应包含：**
- 🎮 游戏名称
- 💰 原价和现价
- ✨ 折扣率
- 💵 节省金额
- 🔗 购买链接

### 4.3 检查站内通知

用户登录后，在通知中心应能看到折扣通知：

```
类型：💰 折扣提醒
标题：您收藏的游戏打折了！
内容：《赛博朋克2077》限时5折优惠，快来购买！
```

---

## ❓ 常见问题

### Q1: 邮件发送失败？

**检查清单：**
- [ ] SMTP授权码是否正确
- [ ] QQ邮箱是否开启了SMTP服务
- [ ] 防火墙是否阻止587端口
- [ ] 网络连接是否正常

**测试SMTP连接：**
```bash
telnet smtp.qq.com 587
```

### Q2: 没有触发邮件？

**可能原因：**
- ❌ 新折扣率 ≤ 旧折扣率（必须提升）
- ❌ 新折扣率 = 0（必须大于0）
- ❌ 没有用户将该游戏加入愿望单
- ❌ 所有用户都关闭了折扣通知

**检查SQL：**
```sql
-- 查看有多少用户会收到通知
SELECT COUNT(*) as notify_count
FROM wishlists w
JOIN users u ON w.user_id = u.id
WHERE w.game_id = 1 
  AND w.notify_discount = 1 
  AND u.email IS NOT NULL 
  AND u.email != '';
```

### Q3: 邮件进入垃圾箱？

**解决方案：**
1. 将发件人邮箱加入白名单
2. 检查邮件内容是否包含敏感词
3. 使用企业邮箱代替个人邮箱
4. 配置SPF/DKIM记录（生产环境）

---

## 🎉 完成！

如果一切正常，你现在应该：
- ✅ 能够更新游戏折扣
- ✅ 自动发送邮件给相关用户
- ✅ 在日志中看到发送记录
- ✅ 在邮箱中收到折扣提醒

---

## 📚 下一步

- 📖 阅读完整文档：[折扣通知邮件功能说明.md](./折扣通知邮件功能说明.md)
- 🔧 自定义邮件模板
- 📊 添加邮件发送统计
- 🛡️ 实现管理员权限控制

---

**有问题？** 查看日志文件或联系技术支持。
