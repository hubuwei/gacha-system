-- ============================================
-- 游戏商城数据库表结构
-- 数据库：gacha_system
-- 创建时间：2026-04-03
-- 说明：包含用户、游戏、购物车、订单等完整功能
-- ============================================

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户 ID',
  `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `phone` VARCHAR(20) UNIQUE COMMENT '手机号',
  `phone_verified` TINYINT(1) DEFAULT 0 COMMENT '手机号是否验证',
  `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
  `email_verified` TINYINT(1) DEFAULT 0 COMMENT '邮箱是否验证',
  `avatar_url` VARCHAR(500) COMMENT '头像 URL',
  `nickname` VARCHAR(50) COMMENT '昵称',
  `signature` VARCHAR(200) COMMENT '个性签名',
  `account_status` TINYINT DEFAULT 1 COMMENT '账号状态：0-封禁 1-正常 2-冻结',
  `user_level` INT DEFAULT 1 COMMENT '用户等级',
  `experience_points` INT DEFAULT 0 COMMENT '经验值',
  `last_login_time` DATETIME COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) COMMENT '最后登录 IP',
  `login_type` VARCHAR(20) COMMENT '登录方式：password-密码 sms-短信 qr-二维码',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_username` (`username`),
  INDEX `idx_phone` (`phone`),
  INDEX `idx_email` (`email`),
  INDEX `idx_status` (`account_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 用户余额表
CREATE TABLE IF NOT EXISTS `user_wallets` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT UNIQUE NOT NULL COMMENT '用户 ID',
  `balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '可用余额',
  `frozen_balance` DECIMAL(10,2) DEFAULT 0.00 COMMENT '冻结余额',
  `total_recharge` DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计充值',
  `total_consumed` DECIMAL(10,2) DEFAULT 0.00 COMMENT '累计消费',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户钱包表';

-- 3. 用户余额流水表
CREATE TABLE IF NOT EXISTS `wallet_transactions` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `transaction_type` VARCHAR(20) NOT NULL COMMENT '交易类型：recharge-充值 purchase-购买 refund-退款 withdraw-提现',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '金额',
  `balance_before` DECIMAL(10,2) NOT NULL COMMENT '交易前余额',
  `balance_after` DECIMAL(10,2) NOT NULL COMMENT '交易后余额',
  `description` VARCHAR(200) COMMENT '交易描述',
  `related_order_id` BIGINT COMMENT '关联订单 ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_type` (`transaction_type`),
  INDEX `idx_order_id` (`related_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户钱包流水表';

-- 4. 游戏分类表
CREATE TABLE IF NOT EXISTS `game_categories` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '分类 ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
  `parent_id` INT DEFAULT NULL COMMENT '父分类 ID',
  `icon` VARCHAR(100) COMMENT '图标',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`parent_id`) REFERENCES `game_categories`(`id`) ON DELETE SET NULL,
  INDEX `idx_parent` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏分类表';

-- 5. 游戏标签表
CREATE TABLE IF NOT EXISTS `game_tags` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '标签 ID',
  `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `color` VARCHAR(20) COMMENT '标签颜色',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏标签表';

-- 6. 游戏表
CREATE TABLE IF NOT EXISTS `games` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '游戏 ID',
  `title` VARCHAR(200) NOT NULL COMMENT '游戏标题',
  `short_description` VARCHAR(500) COMMENT '简短描述',
  `full_description` TEXT COMMENT '完整描述',
  `cover_image` VARCHAR(500) COMMENT '封面图片 URL',
  `banner_image` VARCHAR(500) COMMENT '横幅图片 URL',
  `screenshots` JSON COMMENT '截图 JSON 数组',
  `trailer_url` VARCHAR(500) COMMENT '预告片 URL',
  `base_price` DECIMAL(10,2) NOT NULL COMMENT '基础价格',
  `current_price` DECIMAL(10,2) NOT NULL COMMENT '当前价格',
  `discount_rate` INT DEFAULT 0 COMMENT '折扣率 (0-100)',
  `discount_start` DATETIME COMMENT '折扣开始时间',
  `discount_end` DATETIME COMMENT '折扣结束时间',
  `is_featured` TINYINT(1) DEFAULT 0 COMMENT '是否精选',
  `is_on_sale` TINYINT(1) DEFAULT 0 COMMENT '是否在售',
  `release_date` DATE COMMENT '发布日期',
  `developer` VARCHAR(100) COMMENT '开发商',
  `publisher` VARCHAR(100) COMMENT '发行商',
  `rating` DECIMAL(3,2) DEFAULT 0.00 COMMENT '评分 (0-10)',
  `rating_count` INT DEFAULT 0 COMMENT '评分人数',
  `total_sales` INT DEFAULT 0 COMMENT '总销量',
  `total_reviews` INT DEFAULT 0 COMMENT '总评论数',
  `download_count` INT DEFAULT 0 COMMENT '下载量',
  `file_size` BIGINT COMMENT '文件大小 (MB)',
  `version` VARCHAR(20) COMMENT '当前版本',
  `last_update` DATETIME COMMENT '最后更新时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_title` (`title`),
  INDEX `idx_price` (`current_price`),
  INDEX `idx_featured` (`is_featured`),
  INDEX `idx_on_sale` (`is_on_sale`),
  INDEX `idx_rating` (`rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏表';

-- 7. 游戏分类关联表
CREATE TABLE IF NOT EXISTS `game_category_mapping` (
  `game_id` BIGINT NOT NULL COMMENT '游戏 ID',
  `category_id` INT NOT NULL COMMENT '分类 ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`game_id`, `category_id`),
  FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`category_id`) REFERENCES `game_categories`(`id`) ON DELETE CASCADE,
  INDEX `idx_game` (`game_id`),
  INDEX `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏分类关联表';

-- 8. 游戏标签关联表
CREATE TABLE IF NOT EXISTS `game_tag_mapping` (
  `game_id` BIGINT NOT NULL COMMENT '游戏 ID',
  `tag_id` INT NOT NULL COMMENT '标签 ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`game_id`, `tag_id`),
  FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`tag_id`) REFERENCES `game_tags`(`id`) ON DELETE CASCADE,
  INDEX `idx_game` (`game_id`),
  INDEX `idx_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏标签关联表';

-- 9. 游戏配置要求表
CREATE TABLE IF NOT EXISTS `game_system_requirements` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `game_id` BIGINT UNIQUE NOT NULL COMMENT '游戏 ID',
  `os_min` VARCHAR(200) COMMENT '最低操作系统',
  `os_recommended` VARCHAR(200) COMMENT '推荐操作系统',
  `cpu_min` VARCHAR(200) COMMENT '最低 CPU',
  `cpu_recommended` VARCHAR(200) COMMENT '推荐 CPU',
  `ram_min` VARCHAR(50) COMMENT '最低内存',
  `ram_recommended` VARCHAR(50) COMMENT '推荐内存',
  `gpu_min` VARCHAR(200) COMMENT '最低显卡',
  `gpu_recommended` VARCHAR(200) COMMENT '推荐显卡',
  `directx_min` VARCHAR(50) COMMENT '最低 DirectX',
  `directx_recommended` VARCHAR(50) COMMENT '推荐 DirectX',
  `storage_min` VARCHAR(50) NOT NULL COMMENT '最低存储空间',
  `storage_recommended` VARCHAR(50) COMMENT '推荐存储空间',
  `network_min` VARCHAR(100) COMMENT '最低网络要求',
  `network_recommended` VARCHAR(100) COMMENT '推荐网络要求',
  `sound_card_min` VARCHAR(100) COMMENT '最低声卡',
  `sound_card_recommended` VARCHAR(100) COMMENT '推荐声卡',
  `additional_notes` TEXT COMMENT '额外说明',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE,
  INDEX `idx_game` (`game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏配置要求表';

-- 10. 购物车表
CREATE TABLE IF NOT EXISTS `shopping_cart` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `game_id` BIGINT NOT NULL COMMENT '游戏 ID',
  `quantity` INT DEFAULT 1 COMMENT '数量',
  `added_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `checked` TINYINT(1) DEFAULT 1 COMMENT '是否选中',
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE,
  UNIQUE KEY `uk_user_game` (`user_id`, `game_id`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_game` (`game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

-- 11. 愿望单表
CREATE TABLE IF NOT EXISTS `wishlists` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `game_id` BIGINT NOT NULL COMMENT '游戏 ID',
  `priority` INT DEFAULT 1 COMMENT '优先级：1-普通 2-重要 3-非常想要',
  `notify_discount` TINYINT(1) DEFAULT 1 COMMENT '是否通知折扣',
  `notify_release` TINYINT(1) DEFAULT 1 COMMENT '是否通知发售',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE,
  UNIQUE KEY `uk_user_game` (`user_id`, `game_id`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_game` (`game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='愿望单表';

-- 12. 订单表
CREATE TABLE IF NOT EXISTS `orders` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单 ID',
  `order_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '订单编号',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
  `actual_amount` DECIMAL(10,2) NOT NULL COMMENT '实际支付金额',
  `payment_method` VARCHAR(20) COMMENT '支付方式：balance-余额 alipay-支付宝 wechat-微信',
  `payment_status` VARCHAR(20) DEFAULT 'pending' COMMENT '支付状态：pending-待支付 paid-已支付 failed-失败 refunded-已退款',
  `payment_time` DATETIME COMMENT '支付时间',
  `order_status` VARCHAR(20) DEFAULT 'pending' COMMENT '订单状态：pending-待支付 completed-已完成 cancelled-已取消',
  `refund_time` DATETIME COMMENT '退款时间',
  `refund_reason` VARCHAR(500) COMMENT '退款原因',
  `remark` VARCHAR(500) COMMENT '备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  INDEX `idx_order_no` (`order_no`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_status` (`payment_status`),
  INDEX `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 13. 订单详情表
CREATE TABLE IF NOT EXISTS `order_items` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `order_id` BIGINT NOT NULL COMMENT '订单 ID',
  `game_id` BIGINT NOT NULL COMMENT '游戏 ID',
  `game_title` VARCHAR(200) NOT NULL COMMENT '游戏名称 (快照)',
  `game_cover` VARCHAR(500) COMMENT '游戏封面 (快照)',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `original_price` DECIMAL(10,2) NOT NULL COMMENT '原价',
  `actual_price` DECIMAL(10,2) NOT NULL COMMENT '实际价格',
  `discount_rate` INT DEFAULT 0 COMMENT '折扣率',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE,
  INDEX `idx_order` (`order_id`),
  INDEX `idx_game` (`game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单详情表';

-- 14. 游戏评论表
CREATE TABLE IF NOT EXISTS `game_reviews` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '评论 ID',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名 (快照)',
  `user_avatar` VARCHAR(500) COMMENT '用户头像 (快照)',
  `game_id` BIGINT NOT NULL COMMENT '游戏 ID',
  `rating` INT NOT NULL COMMENT '评分 1-10',
  `title` VARCHAR(200) COMMENT '评论标题',
  `content` TEXT NOT NULL COMMENT '评论内容',
  `pros` TEXT COMMENT '优点',
  `cons` TEXT COMMENT '缺点',
  `play_hours` DECIMAL(10,2) COMMENT '游戏时长 (小时)',
  `is_verified_purchase` TINYINT(1) DEFAULT 0 COMMENT '是否已验证购买',
  `helpful_count` INT DEFAULT 0 COMMENT '有用数量',
  `not_helpful_count` INT DEFAULT 0 COMMENT '无用数量',
  `parent_id` BIGINT DEFAULT NULL COMMENT '父评论 ID (回复)',
  `is_official` TINYINT(1) DEFAULT 0 COMMENT '是否官方回复',
  `status` TINYINT DEFAULT 1 COMMENT '状态：0-待审核 1-显示 2-隐藏',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`parent_id`) REFERENCES `game_reviews`(`id`) ON DELETE CASCADE,
  INDEX `idx_game` (`game_id`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_rating` (`rating`),
  INDEX `idx_status` (`status`),
  INDEX `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏评论表';

-- 15. 评论图片表
CREATE TABLE IF NOT EXISTS `review_images` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `review_id` BIGINT NOT NULL COMMENT '评论 ID',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片 URL',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`review_id`) REFERENCES `game_reviews`(`id`) ON DELETE CASCADE,
  INDEX `idx_review` (`review_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='评论图片表';

-- 16. 系统通知表
CREATE TABLE IF NOT EXISTS `system_notifications` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '通知 ID',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` TEXT NOT NULL COMMENT '通知内容',
  `type` VARCHAR(20) NOT NULL COMMENT '类型：discount-折扣 release-发售 system-系统 order-订单',
  `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读',
  `read_at` DATETIME COMMENT '阅读时间',
  `related_game_id` BIGINT COMMENT '关联游戏 ID',
  `related_order_id` BIGINT COMMENT '关联订单 ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  INDEX `idx_user` (`user_id`),
  INDEX `idx_type` (`type`),
  INDEX `idx_read` (`is_read`),
  INDEX `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统通知表';

-- 17. 登录日志表
CREATE TABLE IF NOT EXISTS `login_logs` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `login_type` VARCHAR(20) NOT NULL COMMENT '登录方式：password sms qr',
  `ip_address` VARCHAR(50) COMMENT 'IP 地址',
  `user_agent` VARCHAR(500) COMMENT '用户代理',
  `login_status` TINYINT DEFAULT 1 COMMENT '状态：0-失败 1-成功',
  `fail_reason` VARCHAR(200) COMMENT '失败原因',
  `device_info` VARCHAR(200) COMMENT '设备信息',
  `location` VARCHAR(100) COMMENT '登录地点',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  INDEX `idx_user` (`user_id`),
  INDEX `idx_created` (`created_at`),
  INDEX `idx_ip` (`ip_address`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='登录日志表';

-- 18. 管理员表
CREATE TABLE IF NOT EXISTS `admins` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '管理员 ID',
  `username` VARCHAR(50) UNIQUE NOT NULL COMMENT '用户名',
  `password_hash` VARCHAR(255) NOT NULL COMMENT '密码哈希',
  `real_name` VARCHAR(50) COMMENT '真实姓名',
  `email` VARCHAR(100) UNIQUE COMMENT '邮箱',
  `phone` VARCHAR(20) COMMENT '手机号',
  `avatar_url` VARCHAR(500) COMMENT '头像',
  `role` VARCHAR(20) DEFAULT 'admin' COMMENT '角色：super_admin-超级管理员 admin-管理员 operator-操作员',
  `permissions` JSON COMMENT '权限列表',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `last_login_time` DATETIME COMMENT '最后登录时间',
  `last_login_ip` VARCHAR(50) COMMENT '最后登录 IP',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_username` (`username`),
  INDEX `idx_role` (`role`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员表';

-- 19. 管理员操作日志表
CREATE TABLE IF NOT EXISTS `admin_operation_logs` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `admin_id` BIGINT NOT NULL COMMENT '管理员 ID',
  `operation_type` VARCHAR(50) NOT NULL COMMENT '操作类型',
  `target_table` VARCHAR(50) COMMENT '目标表',
  `target_id` BIGINT COMMENT '目标 ID',
  `operation_desc` VARCHAR(500) COMMENT '操作描述',
  `old_value` JSON COMMENT '旧值',
  `new_value` JSON COMMENT '新值',
  `ip_address` VARCHAR(50) COMMENT 'IP 地址',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  FOREIGN KEY (`admin_id`) REFERENCES `admins`(`id`) ON DELETE CASCADE,
  INDEX `idx_admin` (`admin_id`),
  INDEX `idx_type` (`operation_type`),
  INDEX `idx_created` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='管理员操作日志表';

-- 20. 轮播图表
CREATE TABLE IF NOT EXISTS `banners` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `title` VARCHAR(200) COMMENT '标题',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片 URL',
  `target_type` VARCHAR(20) COMMENT '目标类型：game-游戏 link-链接',
  `target_id` BIGINT COMMENT '目标 ID',
  `target_url` VARCHAR(500) COMMENT '目标 URL',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `start_time` DATETIME COMMENT '开始时间',
  `end_time` DATETIME COMMENT '结束时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_active` (`is_active`),
  INDEX `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- ============================================
-- 初始化数据
-- ============================================

-- 插入默认管理员 (密码：admin123)
INSERT INTO `admins` (`username`, `password_hash`, `real_name`, `role`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8QeN3jYpCvnFzJHkH9gPZQwLxK5iG', '系统管理员', 'super_admin');

-- 插入游戏分类
INSERT INTO `game_categories` (`name`, `parent_id`, `icon`, `sort_order`) VALUES
('动作', NULL, '⚔️', 1),
('冒险', NULL, '🗺️', 2),
('赛车', NULL, '🏎️', 3),
('射击', NULL, '🔫', 4),
('角色扮演', NULL, '🎭', 5),
('策略', NULL, '♟️', 6),
('模拟', NULL, '🎮', 7),
('体育', NULL, '⚽', 8),
('休闲', NULL, '🎯', 9),
('开放世界', 1, '🌍', 1),
('竞速', 3, '🏁', 1),
('FPS', 4, '🎯', 1);

-- 插入游戏标签
INSERT INTO `game_tags` (`name`, `color`, `sort_order`) VALUES
('开放世界', '#ff6b35', 1),
('犯罪', '#ff00ff', 2),
('经典', '#00ffff', 3),
('竞速', '#4ecdc4', 4),
('多人', '#f4a261', 5),
('体育', '#2d6a4f', 6),
('生存', '#e63946', 7),
('探索', '#457b9d', 8),
('剧情', '#1d3557', 9),
('FPS', '#ff4757', 10),
('竞技', '#2ed573', 11),
('经营', '#ffa502', 12),
('休闲', '#7bed9f', 13),
('策略', '#70a1ff', 14),
('赛博朋克', '#a55eea', 15),
('黑帮', '#fd79a8', 16),
('特技', '#fdcb6e', 17),
('多人在线', '#6c5ce7', 18);

-- ============================================
-- 视图和存储过程（可选）
-- ============================================

-- 创建游戏详情视图
CREATE OR REPLACE VIEW `v_game_detail` AS
SELECT 
  g.*,
  GROUP_CONCAT(DISTINCT gc.name SEPARATOR ',') AS categories,
  GROUP_CONCAT(DISTINCT gt.name SEPARATOR ',') AS tags,
  gsr.cpu_min,
  gsr.cpu_recommended,
  gsr.ram_min,
  gsr.ram_recommended,
  gsr.gpu_min,
  gsr.gpu_recommended,
  gsr.storage_min
FROM games g
LEFT JOIN game_category_mapping gcm ON g.id = gcm.game_id
LEFT JOIN game_categories gc ON gcm.category_id = gc.id
LEFT JOIN game_tag_mapping gtm ON g.id = gtm.game_id
LEFT JOIN game_tags gt ON gtm.tag_id = gt.id
LEFT JOIN game_system_requirements gsr ON g.id = gsr.game_id
GROUP BY g.id;

-- ============================================
-- 说明
-- ============================================
-- 1. 所有表使用 InnoDB 引擎，支持事务和外键
-- 2. 使用 utf8mb4 字符集，支持 emoji 等特殊字符
-- 3. 时间字段统一使用 DATETIME 类型
-- 4. 金额字段使用 DECIMAL(10,2) 确保精度
-- 5. 重要表都有适当的索引优化查询性能
-- 6. 外键约束确保数据一致性
-- 7. 使用逻辑删除而非物理删除的表都包含 status 字段
-- 8. 需要审计的表都有 created_at 和 updated_at 字段
