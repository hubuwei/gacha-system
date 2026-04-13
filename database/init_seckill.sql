-- ============================================
-- 抢购系统数据库初始化脚本
-- 创建时间：2026-04-02
-- ============================================

-- 使用数据库
USE gacha_system;

-- 1. 创建抢购商品表
CREATE TABLE IF NOT EXISTS `seckill_products` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    `name` VARCHAR(200) NOT NULL COMMENT '商品名称',
    `description` VARCHAR(500) NOT NULL COMMENT '商品描述',
    `original_price` DECIMAL(10,2) NOT NULL COMMENT '原价',
    `seckill_points` INT NOT NULL COMMENT '抢购所需积分',
    `total_stock` INT NOT NULL COMMENT '总库存',
    `remaining_stock` INT NOT NULL COMMENT '剩余库存',
    `max_per_user` INT NOT NULL COMMENT '每个用户限购数量',
    `interval_hours` INT NOT NULL COMMENT '抢购间隔（小时）',
    `start_time` DATETIME NOT NULL COMMENT '开始时间',
    `end_time` DATETIME NOT NULL COMMENT '结束时间',
    `is_active` TINYINT(1) NOT NULL DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抢购商品表';

-- 2. 创建抢购记录表
CREATE TABLE IF NOT EXISTS `seckill_records` (
    `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键 ID',
    `user_id` BIGINT NOT NULL COMMENT '用户 ID',
    `product_id` BIGINT NOT NULL COMMENT '商品 ID',
    `points` INT NOT NULL COMMENT '使用的积分',
    `status` VARCHAR(50) NOT NULL COMMENT '状态：SUCCESS-成功，FAILED-失败',
    `seckill_time` DATETIME NOT NULL COMMENT '抢购时间',
    `remark` VARCHAR(500) COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_product (`user_id`, `product_id`),
    INDEX idx_seckill_time (`seckill_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抢购记录表';

-- 3. 插入测试数据 - iPhone 17 Pro Max 抢购商品
INSERT INTO `seckill_products` (
    `name`, `description`, `original_price`, `seckill_points`, 
    `total_stock`, `remaining_stock`, `max_per_user`, `interval_hours`,
    `start_time`, `end_time`, `is_active`
) VALUES (
    'Apple iPhone 17 Pro Max (256GB)',
    '最新款 iPhone，A18 芯片，钛金属边框，4800 万像素摄像头',
    9999.00,
    1,
    10,
    10,
    1,
    2,
    '2026-04-01 00:00:00',
    '2026-12-31 23:59:59',
    1
);

-- 4. 查询验证
SELECT '=== 抢购商品表数据 ===' AS info;
SELECT * FROM seckill_products;

SELECT '=== 表创建完成 ===' AS info;
