-- ====================================
-- 游戏商城新功能数据库表
-- ====================================

USE gacha_system;

-- 1. 订单表
CREATE TABLE IF NOT EXISTS `orders` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单ID',
  `order_no` VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `total_amount` DECIMAL(10,2) NOT NULL COMMENT '订单总额',
  `discount_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT '优惠金额',
  `actual_amount` DECIMAL(10,2) NOT NULL COMMENT '实付金额',
  `payment_method` VARCHAR(20) COMMENT '支付方式',
  `payment_status` VARCHAR(20) DEFAULT 'pending' COMMENT '支付状态',
  `payment_time` DATETIME COMMENT '支付时间',
  `order_status` VARCHAR(20) DEFAULT 'pending' COMMENT '订单状态',
  `refund_time` DATETIME COMMENT '退款时间',
  `refund_reason` VARCHAR(500) COMMENT '退款原因',
  `remark` VARCHAR(500) COMMENT '备注',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_order_no` (`order_no`),
  INDEX `idx_order_status` (`order_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- 2. 订单详情表
CREATE TABLE IF NOT EXISTS `order_items` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '订单详情ID',
  `order_id` BIGINT NOT NULL COMMENT '订单ID',
  `game_id` BIGINT NOT NULL COMMENT '游戏ID',
  `game_title` VARCHAR(200) NOT NULL COMMENT '游戏名称',
  `game_cover` VARCHAR(500) COMMENT '游戏封面',
  `quantity` INT NOT NULL DEFAULT 1 COMMENT '数量',
  `original_price` DECIMAL(10,2) NOT NULL COMMENT '原价',
  `actual_price` DECIMAL(10,2) NOT NULL COMMENT '实际价格',
  `discount_rate` INT DEFAULT 0 COMMENT '折扣率',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_order_id` (`order_id`),
  INDEX `idx_game_id` (`game_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单详情表';

-- 3. 交易记录表
CREATE TABLE IF NOT EXISTS `transactions` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '交易ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `transaction_type` VARCHAR(20) NOT NULL COMMENT '交易类型: recharge/purchase/refund/withdraw',
  `amount` DECIMAL(10,2) NOT NULL COMMENT '交易金额',
  `balance_before` DECIMAL(10,2) COMMENT '交易前余额',
  `balance_after` DECIMAL(10,2) COMMENT '交易后余额',
  `related_order_id` BIGINT COMMENT '关联订单ID',
  `related_order_no` VARCHAR(50) COMMENT '关联订单号',
  `description` VARCHAR(500) COMMENT '交易描述',
  `payment_method` VARCHAR(20) COMMENT '支付方式',
  `transaction_status` VARCHAR(20) DEFAULT 'completed' COMMENT '交易状态',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_transaction_type` (`transaction_type`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='交易记录表';

-- 4. 通知表
CREATE TABLE IF NOT EXISTS `notifications` (
  `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `user_id` BIGINT NOT NULL COMMENT '用户ID',
  `type` VARCHAR(20) NOT NULL COMMENT '通知类型: discount/release/system/order',
  `title` VARCHAR(200) NOT NULL COMMENT '通知标题',
  `content` TEXT NOT NULL COMMENT '通知内容',
  `is_read` TINYINT(1) DEFAULT 0 COMMENT '是否已读',
  `related_game_id` BIGINT COMMENT '关联游戏ID',
  `related_order_id` BIGINT COMMENT '关联订单ID',
  `related_type` VARCHAR(20) COMMENT '关联类型: game/order',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`),
  INDEX `idx_user_id` (`user_id`),
  INDEX `idx_is_read` (`is_read`),
  INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='通知表';

-- 插入测试数据（可选）
INSERT INTO `notifications` (`user_id`, `type`, `title`, `content`, `is_read`, `related_game_id`, `related_type`) VALUES
(1, 'discount', '限时折扣', '《赛博朋克2077》限时5折优惠，快来购买！', 0, 1, 'game'),
(1, 'system', '系统公告', '游戏商城新版本上线，新增多种功能', 0, NULL, NULL),
(1, 'order', '订单通知', '您的订单 ORD202604071234567890 已完成', 1, NULL, 'order');

SELECT '数据库表创建完成！' AS message;
