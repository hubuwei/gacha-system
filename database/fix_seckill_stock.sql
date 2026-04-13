-- ============================================
-- 修复抢购商品库存数据
-- 创建时间：2026-04-02
-- ============================================

USE gacha_system;

-- 检查是否有数据
SELECT '=== 当前抢购商品数据 ===' AS info;
SELECT id, name, total_stock, remaining_stock FROM seckill_products;

-- 如果没有数据或库存为 NULL，更新数据
UPDATE seckill_products 
SET 
    total_stock = 10,
    remaining_stock = 10,
    seckill_points = 1,
    max_per_user = 1,
    interval_hours = 2,
    is_active = 1
WHERE id = 1;

-- 如果 ID=1 的数据不存在，插入数据
INSERT IGNORE INTO seckill_products (
    id, name, description, original_price, seckill_points, 
    total_stock, remaining_stock, max_per_user, interval_hours,
    start_time, end_time, is_active
) VALUES (
    1,
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

-- 验证更新后的数据
SELECT '=== 更新后的抢购商品数据 ===' AS info;
SELECT id, name, total_stock, remaining_stock, seckill_points, max_per_user, interval_hours, is_active 
FROM seckill_products 
WHERE id = 1;
