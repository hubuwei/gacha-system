-- ============================================
-- 游戏商城数据库快速使用指南
-- ============================================

-- ============================================
-- 1. 数据库创建与初始化
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `gacha_system` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `gacha_system`;

-- 执行主脚本
-- source E:\CFDemo\gacha-system\database\game_mall_schema.sql;

-- ============================================
-- 2. 常用查询示例
-- ============================================

-- 2.1 查询所有游戏（含分类和标签）
SELECT * FROM v_game_detail;

-- 2.2 查询正在打折的游戏
SELECT * FROM games 
WHERE is_on_sale = 1 
  AND discount_rate > 0 
  AND discount_end > NOW();

-- 2.3 查询用户的购物车
SELECT 
  sc.id,
  g.title,
  g.cover_image,
  g.current_price,
  g.discount_rate,
  sc.added_at
FROM shopping_cart sc
JOIN games g ON sc.game_id = g.id
WHERE sc.user_id = 1
  AND sc.checked = 1;

-- 2.4 查询用户的愿望单（有折扣的）
SELECT 
  w.id,
  g.title,
  g.base_price,
  g.current_price,
  g.discount_rate,
  w.priority,
  w.notify_discount
FROM wishlists w
JOIN games g ON w.game_id = g.id
WHERE w.user_id = 1
  AND g.discount_rate > 0
  AND w.notify_discount = 1;

-- 2.5 查询游戏的评论（含回复）
SELECT 
  gr.*,
  (SELECT COUNT(*) FROM game_reviews WHERE parent_id = gr.id) AS reply_count
FROM game_reviews gr
WHERE gr.game_id = 1
  AND gr.status = 1
  AND gr.parent_id IS NULL
ORDER BY gr.created_at DESC;

-- 2.6 查询用户的订单列表
SELECT 
  o.*,
  COUNT(oi.id) AS game_count
FROM orders o
LEFT JOIN order_items oi ON o.id = oi.order_id
WHERE o.user_id = 1
GROUP BY o.id
ORDER BY o.created_at DESC;

-- 2.7 查询用户的余额和流水
SELECT 
  u.username,
  uw.balance,
  uw.frozen_balance,
  uw.total_recharge,
  uw.total_consumed
FROM users u
JOIN user_wallets uw ON u.id = uw.user_id
WHERE u.id = 1;

SELECT * FROM wallet_transactions
WHERE user_id = 1
ORDER BY created_at DESC
LIMIT 20;

-- 2.8 查询未读通知
SELECT * FROM system_notifications
WHERE user_id = 1
  AND is_read = 0
ORDER BY created_at DESC;

-- 2.9 查询热门游戏（按销量）
SELECT 
  id,
  title,
  current_price,
  discount_rate,
  total_sales,
  rating
FROM games
WHERE is_on_sale = 1
ORDER BY total_sales DESC
LIMIT 10;

-- 2.10 查询精选游戏
SELECT * FROM games
WHERE is_featured = 1
  AND is_on_sale = 1
ORDER BY created_at DESC;

-- ============================================
-- 3. 插入数据示例
-- ============================================

-- 3.1 插入新用户
INSERT INTO users (
  username, 
  password_hash, 
  phone, 
  email,
  nickname,
  account_status
) VALUES (
  'testuser',
  '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8QeN3jYpCvnFzJHkH9gPZQwLxK5iG',
  '13800138000',
  'test@example.com',
  '测试用户',
  1
);

-- 3.2 初始化用户钱包
INSERT INTO user_wallets (user_id, balance)
SELECT id, 0.00 FROM users WHERE username = 'testuser';

-- 3.3 插入游戏
INSERT INTO games (
  title,
  short_description,
  base_price,
  current_price,
  discount_rate,
  is_on_sale,
  is_featured,
  developer,
  publisher,
  rating
) VALUES (
  '迈阿密风云',
  '在迈阿密展开的街头冒险',
  299.00,
  199.00,
  33,
  1,
  1,
  'Rockstar Games',
  'Rockstar Games',
  9.2
);

-- 3.4 插入游戏配置要求
INSERT INTO game_system_requirements (
  game_id,
  os_min,
  os_recommended,
  cpu_min,
  cpu_recommended,
  ram_min,
  ram_recommended,
  gpu_min,
  gpu_recommended,
  storage_min,
  storage_recommended
) VALUES (
  1,
  'Windows 10 64-bit',
  'Windows 11 64-bit',
  'Intel Core i5-3570K',
  'Intel Core i7-8700K',
  '8 GB',
  '16 GB',
  'NVIDIA GTX 970',
  'NVIDIA RTX 2070',
  '100 GB',
  '150 GB'
);

-- 3.5 添加游戏到分类
INSERT INTO game_category_mapping (game_id, category_id) VALUES
(1, 1), -- 动作
(1, 2); -- 冒险

-- 3.6 添加游戏标签
INSERT INTO game_tag_mapping (game_id, tag_id) VALUES
(1, 1), -- 开放世界
(1, 2), -- 犯罪
(1, 5); -- 多人

-- 3.7 添加到购物车
INSERT INTO shopping_cart (user_id, game_id, quantity) VALUES
(1, 1, 1)
ON DUPLICATE KEY UPDATE quantity = quantity + 1;

-- 3.8 添加到愿望单
INSERT INTO wishlists (user_id, game_id, priority, notify_discount) VALUES
(1, 1, 2, 1);

-- 3.9 创建订单
INSERT INTO orders (
  order_no,
  user_id,
  total_amount,
  discount_amount,
  actual_amount,
  payment_method
) VALUES (
  'ORD20260403001',
  1,
  299.00,
  100.00,
  199.00,
  'balance'
);

-- 3.10 插入订单详情
INSERT INTO order_items (
  order_id,
  game_id,
  game_title,
  game_cover,
  quantity,
  original_price,
  actual_price,
  discount_rate
) VALUES (
  1,
  1,
  '迈阿密风云',
  'cover.jpg',
  1,
  299.00,
  199.00,
  33
);

-- 3.11 发表游戏评论
INSERT INTO game_reviews (
  user_id,
  username,
  user_avatar,
  game_id,
  rating,
  title,
  content,
  pros,
  cons,
  play_hours,
  is_verified_purchase
) VALUES (
  1,
  'testuser',
  'avatar.jpg',
  1,
  9,
  '非常棒的游戏！',
  '画面精美，剧情丰富，玩法多样',
  '画面好，剧情棒，音乐出色',
  '优化一般，加载较慢',
  25.5,
  1
);

-- 3.12 发送系统通知
INSERT INTO system_notifications (
  user_id,
  title,
  content,
  type,
  related_game_id
) VALUES (
  1,
  '您收藏的游戏打折了！',
  '《迈阿密风云》限时特惠，仅需 199 元！',
  'discount',
  1
);

-- ============================================
-- 4. 更新操作示例
-- ============================================

-- 4.1 更新用户余额
UPDATE user_wallets 
SET balance = balance - 199.00
WHERE user_id = 1;

-- 4.2 记录余额流水
INSERT INTO wallet_transactions (
  user_id,
  transaction_type,
  amount,
  balance_before,
  balance_after,
  description,
  related_order_id
) VALUES (
  1,
  'purchase',
  -199.00,
  500.00,
  301.00,
  '购买游戏《迈阿密风云》',
  1
);

-- 4.3 更新游戏销量
UPDATE games 
SET total_sales = total_sales + 1,
    current_price = 199.00
WHERE id = 1;

-- 4.4 更新订单状态
UPDATE orders 
SET payment_status = 'paid',
    order_status = 'completed',
    payment_time = NOW()
WHERE id = 1;

-- 4.5 标记通知为已读
UPDATE system_notifications 
SET is_read = 1,
    read_at = NOW()
WHERE user_id = 1 AND id = 1;

-- 4.6 更新用户最后登录信息
UPDATE users 
SET last_login_time = NOW(),
    last_login_ip = '192.168.1.100',
    login_type = 'password'
WHERE id = 1;

-- ============================================
-- 5. 删除操作示例
-- ============================================

-- 5.1 从购物车删除
DELETE FROM shopping_cart 
WHERE user_id = 1 AND game_id = 1;

-- 5.2 从愿望单删除
DELETE FROM wishlists 
WHERE user_id = 1 AND game_id = 1;

-- 5.3 取消订单（逻辑删除）
UPDATE orders 
SET order_status = 'cancelled'
WHERE id = 1;

-- ============================================
-- 6. 统计查询
-- ============================================

-- 6.1 用户消费统计
SELECT 
  u.username,
  COUNT(DISTINCT o.id) AS total_orders,
  SUM(o.actual_amount) AS total_spent,
  COUNT(DISTINCT oi.game_id) AS total_games
FROM users u
LEFT JOIN orders o ON u.id = o.user_id AND o.payment_status = 'paid'
LEFT JOIN order_items oi ON o.id = oi.order_id
WHERE u.id = 1
GROUP BY u.id;

-- 6.2 游戏销售统计
SELECT 
  g.id,
  g.title,
  g.total_sales,
  SUM(oi.quantity) AS sold_count,
  SUM(oi.actual_price * oi.quantity) AS total_revenue
FROM games g
LEFT JOIN order_items oi ON g.id = oi.game_id
LEFT JOIN orders o ON oi.order_id = o.id AND o.payment_status = 'paid'
GROUP BY g.id
ORDER BY total_revenue DESC;

-- 6.3 评论统计
SELECT 
  g.id,
  g.title,
  COUNT(gr.id) AS review_count,
  AVG(gr.rating) AS avg_rating,
  SUM(CASE WHEN gr.rating >= 8 THEN 1 ELSE 0 END) AS positive_count,
  SUM(CASE WHEN gr.rating <= 5 THEN 1 ELSE 0 END) AS negative_count
FROM games g
LEFT JOIN game_reviews gr ON g.id = gr.game_id AND gr.status = 1
GROUP BY g.id;

-- ============================================
-- 7. 事务示例
-- ============================================

-- 购买游戏事务
DELIMITER //
CREATE PROCEDURE purchase_game(
  IN p_user_id BIGINT,
  IN p_game_id BIGINT,
  IN p_order_no VARCHAR(50)
)
BEGIN
  DECLARE v_price DECIMAL(10,2);
  DECLARE v_balance DECIMAL(10,2);
  DECLARE v_order_id BIGINT;
  
  -- 开启事务
  START TRANSACTION;
  
  -- 获取游戏价格
  SELECT current_price INTO v_price 
  FROM games 
  WHERE id = p_game_id;
  
  -- 获取用户余额
  SELECT balance INTO v_balance 
  FROM user_wallets 
  WHERE user_id = p_user_id
  FOR UPDATE;
  
  -- 检查余额
  IF v_balance < v_price THEN
    SIGNAL SQLSTATE '45000' 
    SET MESSAGE_TEXT = '余额不足';
  END IF;
  
  -- 创建订单
  INSERT INTO orders (
    order_no, user_id, total_amount, 
    discount_amount, actual_amount, 
    payment_method, payment_status, order_status
  ) VALUES (
    p_order_no, p_user_id, v_price, 
    0, v_price, 
    'balance', 'paid', 'completed'
  );
  
  SET v_order_id = LAST_INSERT_ID();
  
  -- 插入订单详情
  INSERT INTO order_items (
    order_id, game_id, game_title, 
    quantity, original_price, actual_price
  ) VALUES (
    v_order_id, p_game_id, '游戏名称',
    1, v_price, v_price
  );
  
  -- 扣除余额
  UPDATE user_wallets 
  SET balance = balance - v_price,
      total_consumed = total_consumed + v_price
  WHERE user_id = p_user_id;
  
  -- 记录流水
  INSERT INTO wallet_transactions (
    user_id, transaction_type, amount,
    balance_before, balance_after,
    description, related_order_id
  ) VALUES (
    p_user_id, 'purchase', -v_price,
    v_balance, v_balance - v_price,
    '购买游戏', v_order_id
  );
  
  -- 更新游戏销量
  UPDATE games 
  SET total_sales = total_sales + 1
  WHERE id = p_game_id;
  
  -- 提交事务
  COMMIT;
END //
DELIMITER ;

-- 调用存储过程
-- CALL purchase_game(1, 1, 'ORD20260403001');

-- ============================================
-- 8. 触发器示例
-- ============================================

-- 订单支付后自动更新游戏销量
DELIMITER //
CREATE TRIGGER after_order_paid
AFTER UPDATE ON orders
FOR EACH ROW
BEGIN
  IF NEW.payment_status = 'paid' AND OLD.payment_status != 'paid' THEN
    UPDATE games g
    INNER JOIN order_items oi ON g.id = oi.game_id
    SET g.total_sales = g.total_sales + oi.quantity
    WHERE oi.order_id = NEW.id;
  END IF;
END //
DELIMITER ;

-- ============================================
-- 9. 权限管理
-- ============================================

-- 创建只读用户
CREATE USER 'gacha_readonly'@'localhost' 
IDENTIFIED BY 'readonly_password';

GRANT SELECT ON gacha_system.* TO 'gacha_readonly'@'localhost';

-- 创建应用用户
CREATE USER 'gacha_app'@'localhost' 
IDENTIFIED BY 'app_password';

GRANT SELECT, INSERT, UPDATE, DELETE ON gacha_system.* TO 'gacha_app'@'localhost';
GRANT EXECUTE ON PROCEDURE gacha_system.purchase_game TO 'gacha_app'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;

-- ============================================
-- 10. 数据库维护
-- ============================================

-- 查看表大小
SELECT 
  table_name,
  ROUND(((data_length + index_length) / 1024 / 1024), 2) AS 'Size (MB)'
FROM information_schema.TABLES
WHERE table_schema = 'gacha_system'
ORDER BY (data_length + index_length) DESC;

-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query_log';
SHOW VARIABLES LIKE 'long_query_time';

-- 优化表
OPTIMIZE TABLE games;
OPTIMIZE TABLE orders;
OPTIMIZE TABLE game_reviews;

-- 分析表
ANALYZE TABLE games;
ANALYZE TABLE orders;

-- ============================================
-- 结束
-- ============================================
