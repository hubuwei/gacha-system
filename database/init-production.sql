-- ============================================
-- 游戏商城生产环境数据库初始化脚本
-- 数据库：gacha_system_prod
-- 适用系统：Ubuntu 24.04
-- 创建时间：2026-04-11
-- ============================================

-- 1. 创建数据库
CREATE DATABASE IF NOT EXISTS gacha_system_prod 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE gacha_system_prod;

-- ============================================
-- 用户相关表
-- ============================================

-- 2. 用户表
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
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

-- 3. 用户钱包表
DROP TABLE IF EXISTS `user_wallets`;
CREATE TABLE `user_wallets` (
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

-- 4. 用户钱包流水表
DROP TABLE IF EXISTS `wallet_transactions`;
CREATE TABLE `wallet_transactions` (
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

-- ============================================
-- 游戏相关表
-- ============================================

-- 5. 游戏分类表
DROP TABLE IF EXISTS `game_categories`;
CREATE TABLE `game_categories` (
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

-- 6. 游戏标签表
DROP TABLE IF EXISTS `game_tags`;
CREATE TABLE `game_tags` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT '标签 ID',
  `name` VARCHAR(50) NOT NULL COMMENT '标签名称',
  `color` VARCHAR(20) COMMENT '标签颜色',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏标签表';

-- 7. 游戏表
DROP TABLE IF EXISTS `games`;
CREATE TABLE `games` (
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

-- 8. 游戏分类关联表
DROP TABLE IF EXISTS `game_category_mapping`;
CREATE TABLE `game_category_mapping` (
  `game_id` BIGINT NOT NULL COMMENT '游戏 ID',
  `category_id` INT NOT NULL COMMENT '分类 ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`game_id`, `category_id`),
  FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`category_id`) REFERENCES `game_categories`(`id`) ON DELETE CASCADE,
  INDEX `idx_game` (`game_id`),
  INDEX `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏分类关联表';

-- 9. 游戏标签关联表
DROP TABLE IF EXISTS `game_tag_mapping`;
CREATE TABLE `game_tag_mapping` (
  `game_id` BIGINT NOT NULL COMMENT '游戏 ID',
  `tag_id` INT NOT NULL COMMENT '标签 ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`game_id`, `tag_id`),
  FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`tag_id`) REFERENCES `game_tags`(`id`) ON DELETE CASCADE,
  INDEX `idx_game` (`game_id`),
  INDEX `idx_tag` (`tag_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏标签关联表';

-- 10. 购物车表
DROP TABLE IF EXISTS `shopping_cart`;
CREATE TABLE `shopping_cart` (
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
DROP TABLE IF EXISTS `wishlists`;
CREATE TABLE `wishlists` (
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
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单 ID',
  `order_no` VARCHAR(50) UNIQUE NOT NULL COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总额',
  `actual_amount` DECIMAL(10,2) NOT NULL COMMENT '实际支付金额',
  `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
  `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '订单状态：pending-待支付 paid-已支付 completed-已完成 cancelled-已取消 refunded-已退款',
  `payment_method` VARCHAR(20) COMMENT '支付方式：wechat-微信 alipay-支付宝 balance-余额',
  `payment_time` DATETIME COMMENT '支付时间',
  `completed_time` DATETIME COMMENT '完成时间',
  `cancelled_time` DATETIME COMMENT '取消时间',
  `remark` VARCHAR(500) COMMENT '备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  INDEX `idx_order_no` (`order_no`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_status` (`status`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 13. 订单明细表
DROP TABLE IF EXISTS `order_items`;
CREATE TABLE `order_items` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `order_id` BIGINT NOT NULL COMMENT '订单 ID',
  `game_id` BIGINT NOT NULL COMMENT '游戏 ID',
  `game_title` VARCHAR(200) NOT NULL COMMENT '游戏标题（快照）',
  `game_cover` VARCHAR(500) COMMENT '游戏封面（快照）',
  `unit_price` DECIMAL(10,2) NOT NULL COMMENT '单价',
  `quantity` INT DEFAULT 1 COMMENT '数量',
  `subtotal` DECIMAL(10,2) NOT NULL COMMENT '小计',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE,
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_game_id` (`game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

-- 14. 用户游戏库表（已购买的游戏）
DROP TABLE IF EXISTS `user_game_library`;
CREATE TABLE `user_game_library` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `game_id` BIGINT NOT NULL COMMENT '游戏 ID',
  `order_id` BIGINT COMMENT '订单 ID',
  `purchase_price` DECIMAL(10,2) NOT NULL COMMENT '购买价格',
  `purchase_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '购买时间',
  `play_hours` DECIMAL(10,2) DEFAULT 0.00 COMMENT '游戏时长（小时）',
  `last_play_time` DATETIME COMMENT '最后游玩时间',
  `is_favorite` TINYINT(1) DEFAULT 0 COMMENT '是否收藏',
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE SET NULL,
  UNIQUE KEY `uk_user_game` (`user_id`, `game_id`),
  INDEX `idx_user` (`user_id`),
  INDEX `idx_game` (`game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户游戏库表';

-- 15. 轮播图表
DROP TABLE IF EXISTS `banners`;
CREATE TABLE `banners` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `title` VARCHAR(200) COMMENT '标题',
  `image_url` VARCHAR(500) NOT NULL COMMENT '图片 URL',
  `target_type` VARCHAR(20) COMMENT '目标类型：game-游戏 link-链接',
  `target_id` BIGINT COMMENT '目标 ID',
  `target_url` VARCHAR(500) COMMENT '目标链接',
  `sort_order` INT DEFAULT 0 COMMENT '排序',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT '是否启用',
  `start_time` DATETIME COMMENT '开始时间',
  `end_time` DATETIME COMMENT '结束时间',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  INDEX `idx_active` (`is_active`),
  INDEX `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='轮播图表';

-- 16. 通知表
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `user_id` BIGINT NOT NULL COMMENT '用户 ID',
  `type` VARCHAR(20) NOT NULL COMMENT '通知类型：promotion-促销 system-系统 order-订单',
  `title` VARCHAR(200) NOT NULL COMMENT '标题',
  `message` TEXT NOT NULL COMMENT '消息内容',
  `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读',
  `related_id` BIGINT COMMENT '关联 ID（如订单 ID、游戏 ID）',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  FOREIGN KEY (`user_id`) REFERENCES `users`(`id`) ON DELETE CASCADE,
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_is_read` (`is_read`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- ============================================
-- 插入初始数据
-- ============================================

-- 17. 插入游戏分类
INSERT INTO `game_categories` (`name`, `parent_id`, `icon`, `sort_order`, `is_active`) VALUES
('动作', NULL, '⚔️', 1, 1),
('冒险', NULL, '🗺️', 2, 1),
('赛车', NULL, '🏎️', 3, 1),
('射击', NULL, '🔫', 4, 1),
('RPG', NULL, '🎭', 5, 1),
('模拟', NULL, '🎮', 6, 1),
('策略', NULL, '♟️', 7, 1),
('体育', NULL, '⚽', 8, 1),
('休闲', NULL, '🎯', 9, 1),
('独立', NULL, '💡', 10, 1);

-- 18. 插入游戏标签
INSERT INTO `game_tags` (`name`, `color`, `sort_order`) VALUES
('开放世界', '#FF6B6B', 1),
('多人在线', '#4ECDC4', 2),
('单人剧情', '#45B7D1', 3),
('经典怀旧', '#FFA07A', 4),
('高难度', '#DDA0DD', 5),
('竞技', '#98D8C8', 6),
('合作', '#F7DC6F', 7),
('沙盒', '#BB8FCE', 8),
('生存', '#85C1E2', 9),
('建造', '#F8B739', 10),
('恐怖', '#6C3483', 11),
('解谜', '#1ABC9C', 12),
('格斗', '#E74C3C', 13),
('角色扮演', '#3498DB', 14),
('即时战略', '#9B59B6', 15),
('回合制', '#16A085', 16),
(' Roguelike', '#E67E22', 17),
('像素风', '#BDC3C7', 18),
('二次元', '#FF69B4', 19),
('写实', '#7F8C8D', 20);

-- 19. 插入热门游戏数据（部分示例，完整版请使用 insert_game_data.sql）
INSERT INTO `games` (`title`, `short_description`, `full_description`, `cover_image`, `base_price`, `current_price`, `discount_rate`, `is_featured`, `is_on_sale`, `release_date`, `developer`, `publisher`, `rating`, `total_sales`, `file_size`) VALUES
('黑神话:悟空', '中国神话动作RPG', '基于《西游记》的史诗级动作角色扮演游戏，体验孙悟空的传奇之旅', '/images/games/黑神话悟空.jpg', 268.00, 268.00, 0, 1, 1, '2024-08-20', 'Game Science', 'Game Science', 9.50, 20000000, 130000),
('原神', '开放世界动作RPG', '在提瓦特大陆展开冒险，探索七国，结识伙伴', '/images/games/原神.jpg', 0.00, 0.00, 0, 1, 1, '2020-09-28', 'miHoYo', 'miHoYo', 9.20, 500000000, 20000),
('王者荣耀', '国民级MOBA手游', '5v5公平竞技，随时随地开黑', '/images/games/王者荣耀.jpg', 0.00, 0.00, 0, 1, 1, '2015-11-26', 'TiMi Studio', 'Tencent', 9.00, 800000000, 4000),
('英雄联盟', '全球最流行的MOBA', '5v5团队竞技，超过150位英雄', '/images/games/英雄联盟.jpg', 0.00, 0.00, 0, 1, 1, '2009-10-27', 'Riot Games', 'Riot Games', 9.00, 500000000, 15000),
('绝地求生', '战术竞技手游', '百人跳伞，活到最后', '/images/games/绝地求生.jpg', 98.00, 49.00, 50, 1, 1, '2017-03-23', 'PUBG Corporation', 'Krafton', 8.60, 75000000, 35000),
('我的世界', '史上最畅销游戏', '创造无限可能的沙盒世界', '/images/games/我的世界.jpg', 169.00, 135.20, 20, 1, 1, '2011-11-18', 'Mojang', 'Microsoft', 9.80, 300000000, 1000),
('赛博朋克2077', '夜之城开放世界RPG', '在未来都市中书写你的传奇', '/images/games/赛博朋克2077.jpg', 298.00, 149.00, 50, 1, 1, '2020-12-10', 'CD Projekt Red', 'CD Projekt', 8.50, 25000000, 100000),
('艾尔登法环', '开放世界魂系RPG', '在交界地探索，成为艾尔登之王', '/images/games/艾尔登法环.jpg', 298.00, 238.40, 20, 1, 1, '2022-02-25', 'FromSoftware', 'Bandai Namco', 9.80, 25000000, 60000),
('只狼:影逝二度', '忍者动作游戏', '在战国时代化身独臂忍者', '/images/games/只狼.jpg', 268.00, 134.00, 50, 1, 1, '2019-03-22', 'FromSoftware', 'Activision', 9.60, 12000000, 35000),
('巫师3:狂猎', '当代最伟大的RPG之一', '扮演猎魔人杰洛特，寻找养女希里', '/images/games/巫师3.jpg', 199.00, 99.50, 50, 1, 1, '2015-05-19', 'CD Projekt Red', 'CD Projekt', 9.90, 50000000, 35000);

-- 20. 为游戏添加分类关联
INSERT INTO `game_category_mapping` (`game_id`, `category_id`) VALUES
(1, 1), (1, 5),  -- 黑神话:悟空 - 动作、RPG
(2, 2), (2, 5),  -- 原神 - 冒险、RPG
(3, 7),          -- 王者荣耀 - 策略
(4, 7),          -- 英雄联盟 - 策略
(5, 4),          -- 绝地求生 - 射击
(6, 6),          -- 我的世界 - 模拟
(7, 2), (7, 5),  -- 赛博朋克2077 - 冒险、RPG
(8, 2), (8, 5),  -- 艾尔登法环 - 冒险、RPG
(9, 1),          -- 只狼 - 动作
(10, 2), (10, 5); -- 巫师3 - 冒险、RPG

-- 21. 为游戏添加标签关联
INSERT INTO `game_tag_mapping` (`game_id`, `tag_id`) VALUES
(1, 1), (1, 3),   -- 黑神话:悟空 - 开放世界、单人剧情
(2, 1), (2, 2),   -- 原神 - 开放世界、多人在线
(3, 2), (3, 13),  -- 王者荣耀 - 多人在线、竞技
(4, 2), (4, 13),  -- 英雄联盟 - 多人在线、竞技
(5, 2), (5, 9),   -- 绝地求生 - 多人在线、生存
(6, 8), (6, 10),  -- 我的世界 - 沙盒、建造
(7, 1), (7, 3),   -- 赛博朋克2077 - 开放世界、单人剧情
(8, 1), (8, 5),   -- 艾尔登法环 - 开放世界、高难度
(9, 5), (9, 13),  -- 只狼 - 高难度、格斗
(10, 1), (10, 3); -- 巫师3 - 开放世界、单人剧情

-- 22. 插入轮播图数据
INSERT INTO `banners` (`title`, `image_url`, `target_type`, `target_id`, `sort_order`, `is_active`) VALUES
('黑神话:悟空 火热发售中', '/images/games/黑神话悟空.jpg', 'game', 1, 1, 1),
('原神 新版本上线', '/images/games/原神.jpg', 'game', 2, 2, 1),
('限时特惠 最高5折', '/images/games/巫师3.jpg', 'game', 10, 3, 1);

-- ============================================
-- 完成提示
-- ============================================
SELECT '========================================' AS '';
SELECT '数据库初始化完成！' AS message;
SELECT '数据库名: gacha_system_prod' AS '';
SELECT '========================================' AS '';
SELECT CONCAT('游戏数量: ', COUNT(*)) AS stats FROM games;
SELECT CONCAT('分类数量: ', COUNT(*)) AS stats FROM game_categories;
SELECT CONCAT('标签数量: ', COUNT(*)) AS stats FROM game_tags;
SELECT '========================================' AS '';
