-- 积分兑换系统数据库表结构
-- 这些表将添加到 gacha_system 数据库中

USE `gacha_system`;

-- 1. 兑换物品表
CREATE TABLE IF NOT EXISTS `exchange_item` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '物品 ID',
    `name` VARCHAR(100) NOT NULL COMMENT '物品名称',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '物品描述',
    `icon_url` VARCHAR(500) DEFAULT NULL COMMENT '物品图标 URL',
    `required_points` INT(11) NOT NULL COMMENT '所需积分',
    `total_stock` INT(11) NOT NULL COMMENT '每日库存总量',
    `current_stock` INT(11) NOT NULL COMMENT '当前剩余库存',
    `enabled` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否上架：0-下架，1-上架',
    `sort_weight` INT(11) NOT NULL DEFAULT 0 COMMENT '排序权重（数字越大越靠前）',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_enabled_sort` (`enabled`, `sort_weight`, `id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='兑换物品表';

-- 2. 兑换记录表
CREATE TABLE IF NOT EXISTS `exchange_record` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '兑换记录 ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户 ID',
    `item_id` BIGINT(20) NOT NULL COMMENT '物品 ID',
    `used_points` INT(11) NOT NULL COMMENT '消耗积分',
    `status` TINYINT(4) NOT NULL DEFAULT 0 COMMENT '兑换状态：0-待填写地址，1-已填写地址，2-已发货，3-已完成，-1-已取消',
    `exchange_date` DATE NOT NULL COMMENT '兑换日期',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_item_id` (`item_id`),
    KEY `idx_exchange_date` (`exchange_date`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='兑换记录表';

-- 3. 收货地址表
CREATE TABLE IF NOT EXISTS `delivery_address` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '地址 ID',
    `exchange_record_id` BIGINT(20) NOT NULL COMMENT '兑换记录 ID',
    `user_id` BIGINT(20) NOT NULL COMMENT '用户 ID',
    `recipient_name` VARCHAR(50) NOT NULL COMMENT '收货人姓名',
    `phone_number` VARCHAR(20) NOT NULL COMMENT '联系电话',
    `province` VARCHAR(50) NOT NULL COMMENT '省份',
    `city` VARCHAR(50) NOT NULL COMMENT '城市',
    `district` VARCHAR(50) NOT NULL COMMENT '区县',
    `detail_address` VARCHAR(500) NOT NULL COMMENT '详细地址',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_exchange_record_id` (`exchange_record_id`),
    KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='收货地址表';

-- 插入测试数据 - 兑换物品
INSERT INTO `exchange_item` (`name`, `description`, `icon_url`, `required_points`, `total_stock`, `current_stock`, `enabled`, `sort_weight`) VALUES
('机械键盘', 'Cherry 轴体，RGB 背光，游戏办公两用', 'https://example.com/images/keyboard.jpg', 5000, 10, 10, 1, 100),
('无线鼠标', '罗技 G 系列，高精度传感器，长续航', 'https://example.com/images/mouse.jpg', 3000, 20, 20, 1, 90),
('显卡 RTX 4060', 'NVIDIA GeForce RTX 4060，8GB GDDR6', 'https://example.com/images/gpu.jpg', 50000, 2, 2, 1, 200),
('智能手机', '最新款旗舰手机，256GB 存储', 'https://example.com/images/phone.jpg', 80000, 1, 1, 1, 300),
('耳机', '降噪头戴式游戏耳机', 'https://example.com/images/headset.jpg', 2000, 30, 30, 1, 80),
('鼠标垫', '超大号游戏鼠标垫', 'https://example.com/images/mousepad.jpg', 500, 50, 50, 1, 50);

-- 示例查询
-- 查询所有可兑换物品
-- SELECT * FROM exchange_item WHERE enabled = 1 ORDER BY sort_weight DESC, id ASC;

-- 查询用户兑换记录
-- SELECT * FROM exchange_record WHERE user_id = ? ORDER BY exchange_date DESC, created_at DESC;

-- 查询某日兑换记录
-- SELECT * FROM exchange_record WHERE exchange_date = '2026-04-01';
