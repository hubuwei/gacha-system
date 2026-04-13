# Redis 键空间通知配置说明

## 问题
订单超时自动取消功能依赖 Redis 的键空间通知（Keyspace Notifications），需要在 Redis 服务器上启用。

## 配置方法

### 方法1：通过 redis.conf 配置文件（推荐）

1. 找到 Redis 配置文件 `redis.conf`
2. 找到或添加以下配置：
   ```conf
   notify-keyspace-events Ex
   ```
   
   参数说明：
   - `E`: 启用键事件通知
   - `x`: 启用过期事件通知

3. 重启 Redis 服务：
   ```bash
   # Windows
   redis-server.exe --service-stop
   redis-server.exe --service-start
   
   # Linux/Mac
   sudo systemctl restart redis
   ```

### 方法2：通过命令行临时配置

```bash
# 连接到 Redis
redis-cli

# 执行配置命令
CONFIG SET notify-keyspace-events Ex

# 验证配置
CONFIG GET notify-keyspace-events
```

**注意**：这种方式在 Redis 重启后会失效，需要重新配置。

## 验证配置

### 1. 检查配置是否生效

```bash
redis-cli CONFIG GET notify-keyspace-events
```

应该返回：
```
1) "notify-keyspace-events"
2) "Ex"
```

### 2. 测试键过期通知

```bash
# 终端1：订阅过期事件
redis-cli --csv psubscribe '__keyevent@0__:expired'

# 终端2：设置一个5秒后过期的键
redis-cli SET test_key "test_value" EX 5

# 等待5秒，终端1应该会收到通知
```

### 3. 查看应用日志

启动 mall-service 后，应该看到日志：
```
Redis 键空间通知监听器已启动
```

创建订单后，应该看到：
```
订单超时监控已设置: orderNo=ORD20260410123456789, 超时时间=15分钟
```

15分钟后，应该看到：
```
Redis Key 过期: order:timeout:ORD20260410123456789
订单超时，准备取消: orderNo=ORD20260410123456789
开始取消过期订单: orderNo=ORD20260410123456789
订单已自动取消: orderNo=ORD20260410123456789, userId=1
订单已成功取消: orderNo=ORD20260410123456789
```

## 常见问题

### Q1: Redis 键空间通知会影响性能吗？

A: 会有轻微的性能影响，但对于订单超时这种低频场景，影响可以忽略不计。

### Q2: 如果 Redis 重启了怎么办？

A: 
- 如果使用 `redis.conf` 配置，重启后会自动生效
- 如果使用命令行配置，需要重新执行 `CONFIG SET` 命令
- 建议使用方法1（配置文件）

### Q3: 定时任务和键空间通知哪个更好？

A: 
- **键空间通知**：实时性高，性能好，但依赖 Redis 配置
- **定时任务**：可靠性高，不依赖额外配置，但有延迟
- **最佳实践**：两者结合使用，键空间通知为主，定时任务为备用

### Q4: 如何禁用键空间通知？

A: 将配置改为：
```conf
notify-keyspace-events ""
```
然后重启 Redis。

## 备用方案：定时任务

如果无法配置 Redis 键空间通知，可以使用定时任务作为备用方案。

在 `OrderService.java` 中已经有一个注释掉的定时任务方法：

```java
@Scheduled(fixedRate = 5 * 60 * 1000) // 每5分钟执行
@Transactional
public void cancelExpiredOrders() {
    LocalDateTime expireTime = LocalDateTime.now().minusMinutes(15);
    List<Order> expiredOrders = orderRepository
        .findByOrderStatusAndCreatedAtBefore("pending", expireTime);
    
    for (Order order : expiredOrders) {
        order.setOrderStatus("cancelled");
        orderRepository.save(order);
    }
}
```

要启用它：
1. 在 `MallServiceApplication.java` 添加 `@EnableScheduling` 注解
2. 取消注释上述方法

## 总结

✅ **推荐配置**：
1. 修改 `redis.conf`，添加 `notify-keyspace-events Ex`
2. 重启 Redis
3. 启动 mall-service，自动注册监听器
4. 前端自动过滤已过期订单

🎯 **效果**：
- 订单创建时设置 15 分钟 TTL
- Redis Key 过期时自动触发回调
- 后端自动取消订单并记录日志
- 前端实时显示倒计时，过期后自动从列表移除
