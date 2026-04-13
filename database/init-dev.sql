# ============================================
# 游戏商城数据库 - 开发环境初始化脚本
# 数据库：gacha_system_dev
# 创建时间：2026-04-09
# 说明：包含表结构和测试数据，用于开发和测试
# 用途：开发环境快速搭建
# ============================================

-- 首先执行生产版本表结构
SOURCE schema-production.sql;

-- ============================================
-- 初始化测试数据
-- ============================================

-- 插入默认管理员 (密码：admin123)
INSERT INTO `admins` (`username`, `password_hash`, `real_name`, `role`) VALUES
('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8QeN3jYpCvnFzJHkH9gPZQwLxK5iG', '系统管理员', 'super_admin');

-- 插入测试用户 (密码：user123)
INSERT INTO `users` (`username`, `password_hash`, `nickname`, `email`, `account_status`, `user_level`) VALUES
('testuser1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8QeN3jYpCvnFzJHkH9gPZQwLxK5iG', '测试用户1', 'test1@example.com', 1, 5),
('testuser2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8QeN3jYpCvnFzJHkH9gPZQwLxK5iG', '测试用户2', 'test2@example.com', 1, 3);

-- 插入用户钱包数据
INSERT INTO `user_wallets` (`user_id`, `balance`, `total_recharge`, `total_consumed`) VALUES
(1, 1000.00, 1000.00, 0.00),
(2, 500.00, 500.00, 0.00);

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
-- 说明
-- ============================================
-- 1. 此脚本会先创建所有表结构（通过 SOURCE 命令）
-- 2. 然后插入测试数据
-- 3. 测试账号：
--    - 管理员：admin / admin123
--    - 普通用户：testuser1 / user123
--    - 普通用户：testuser2 / user123
-- 4. 仅在开发环境使用，生产环境请勿执行此脚本
