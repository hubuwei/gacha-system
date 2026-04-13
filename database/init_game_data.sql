-- ============================================
-- 游戏商城 - 游戏数据初始化
-- ============================================

-- 清空现有数据(可选)
-- DELETE FROM game_tag_mapping;
-- DELETE FROM game_category_mapping;
-- DELETE FROM game_system_requirements;
-- DELETE FROM games;

-- 插入游戏数据
INSERT INTO `games` (
    `title`, `short_description`, `full_description`, 
    `cover_image`, `banner_image`, `screenshots`, `trailer_url`,
    `base_price`, `current_price`, `discount_rate`, 
    `is_featured`, `is_on_sale`,
    `developer`, `publisher`,
    `rating`, `rating_count`, `total_sales`, `total_reviews`,
    `file_size`, `version`
) VALUES
('罪恶都市传奇', '开放世界犯罪动作游戏经典之作', '在充满犯罪与腐败的罪恶都市中,体验最刺激的开放世界冒险。驾驶各种车辆,完成危险任务,建立你的犯罪帝国。', '/images/games/gta_vc.jpg', '/images/banners/gta_vc_banner.jpg', '["/images/screenshots/1.jpg", "/images/screenshots/2.jpg"]', 'https://www.youtube.com/watch?v=example1', 399.00, 299.00, 25, 1, 1, 'Rockstar Games', 'Rockstar Games', 9.20, 15234, 50000, 8923, 15360, '1.0.5'),
('迈阿密风云', '极速赛车竞技体验', '感受迈阿密街头的极速狂飙,超过50种豪华跑车等你驾驭。多人在线竞技,展示你的驾驶技术。', '/images/games/miami_racing.jpg', '/images/banners/miami_racing_banner.jpg', '["/images/screenshots/3.jpg", "/images/screenshots/4.jpg"]', 'https://www.youtube.com/watch?v=example2', 199.00, 199.00, 0, 1, 1, 'Speed Studios', 'Racing Games Inc', 8.50, 8921, 32000, 5234, 25600, '2.1.0'),
('热带风暴', '生存探索冒险大作', '在神秘的热带岛屿上求生,探索未知领域,揭开古老文明的秘密。精美的画面,扣人心弦的剧情。', '/images/games/tropical_storm.jpg', '/images/banners/tropical_storm_banner.jpg', '["/images/screenshots/5.jpg", "/images/screenshots/6.jpg"]', 'https://www.youtube.com/watch?v=example3', 259.00, 159.00, 39, 0, 1, 'Adventure Works', 'Explorer Games', 8.80, 12456, 28000, 7123, 30720, '1.3.2'),
('街头枪战', '第一人称射击竞技', '现代战争背景下的团队竞技FPS,多种模式,丰富武器库。公平竞技,技术为王。', '/images/games/street_gunfight.jpg', '/images/banners/street_gunfight_banner.jpg', '["/images/screenshots/7.jpg", "/images/screenshots/8.jpg"]', 'https://www.youtube.com/watch?v=example4', 279.00, 279.00, 0, 1, 1, 'Shooter Studio', 'FPS Games Ltd', 9.00, 18234, 65000, 12456, 40960, '3.0.1'),
('海滨大亨', '休闲经营模拟游戏', '打造你的海滨度假胜地,管理酒店、餐厅、娱乐设施。轻松休闲,适合所有年龄段玩家。', '/images/games/beach_tycoon.jpg', '/images/banners/beach_tycoon_banner.jpg', '["/images/screenshots/9.jpg", "/images/screenshots/10.jpg"]', 'https://www.youtube.com/watch?v=example5', 189.00, 129.00, 32, 0, 1, 'Sim Games', 'Casual Entertainment', 7.90, 6234, 18000, 3456, 5120, '1.5.0'),
('霓虹之夜', '赛博朋克风格RPG', '在未来的赛博朋克世界中,扮演一名雇佣兵,探索霓虹闪烁的城市,做出影响剧情走向的选择。', '/images/games/neon_night.jpg', '/images/banners/neon_night_banner.jpg', '["/images/screenshots/11.jpg", "/images/screenshots/12.jpg"]', 'https://www.youtube.com/watch?v=example6', 249.00, 249.00, 0, 1, 1, 'Cyber Studios', 'RPG Masters', 9.50, 23456, 75000, 15678, 51200, '1.2.3'),
('极限摩托', '特技摩托车竞速', '挑战极限的特技摩托车比赛,真实物理引擎,惊险刺激的特技动作。成为最顶尖的特技车手。', '/images/games/extreme_moto.jpg', '/images/banners/extreme_moto_banner.jpg', '["/images/screenshots/13.jpg", "/images/screenshots/14.jpg"]', 'https://www.youtube.com/watch?v=example7', 149.00, 99.00, 34, 0, 1, 'Moto Games', 'Extreme Sports Inc', 8.20, 7123, 22000, 4567, 10240, '2.0.0'),
('黑帮帝国', '黑帮策略经营游戏', '建立你的黑帮帝国,招募手下,扩张地盘,与其他帮派竞争。策略与经营的完美结合。', '/images/games/gangster_empire.jpg', '/images/banners/gangster_empire_banner.jpg', '["/images/screenshots/15.jpg", "/images/screenshots/16.jpg"]', 'https://www.youtube.com/watch?v=example8', 329.00, 329.00, 0, 1, 1, 'Strategy Works', 'Gangster Games', 8.70, 9876, 30000, 6234, 20480, '1.8.5');

-- 插入游戏分类关联
INSERT INTO `game_category_mapping` (`game_id`, `category_id`) VALUES
(1, 1), (1, 10), -- 罪恶都市传奇: 动作, 开放世界
(2, 3), (2, 11), -- 迈阿密风云: 赛车, 竞速
(3, 2),          -- 热带风暴: 冒险
(4, 4), (4, 12), -- 街头枪战: 射击, FPS
(5, 7),          -- 海滨大亨: 模拟
(6, 5),          -- 霓虹之夜: 角色扮演
(7, 3), (7, 11), -- 极限摩托: 赛车, 竞速
(8, 6);          -- 黑帮帝国: 策略

-- 插入游戏标签关联
INSERT INTO `game_tag_mapping` (`game_id`, `tag_id`) VALUES
(1, 1), (1, 2), (1, 3),  -- 罪恶都市传奇: 开放世界, 犯罪, 经典
(2, 4), (2, 5), (2, 6),  -- 迈阿密风云: 竞速, 多人, 体育
(3, 7), (3, 8), (3, 9),  -- 热带风暴: 生存, 探索, 剧情
(4, 10), (4, 5), (4, 11), -- 街头枪战: FPS, 多人, 竞技
(5, 12), (5, 13), (5, 14), -- 海滨大亨: 经营, 休闲, 策略
(6, 15), (6, 9), (6, 1),  -- 霓虹之夜: 赛博朋克, 剧情, 开放世界
(7, 17), (7, 4), (7, 6),  -- 极限摩托: 特技, 竞速, 体育
(8, 16), (8, 12), (8, 5); -- 黑帮帝国: 黑帮, 经营, 多人

-- 插入游戏系统配置要求
INSERT INTO `game_system_requirements` (
    `game_id`, 
    `os_min`, `os_recommended`,
    `cpu_min`, `cpu_recommended`,
    `ram_min`, `ram_recommended`,
    `gpu_min`, `gpu_recommended`,
    `storage_min`, `storage_recommended`
) VALUES
(1, 'Windows 7 64-bit', 'Windows 10 64-bit', 'Intel Core i5-3470', 'Intel Core i7-4770', '8 GB', '16 GB', 'NVIDIA GTX 660 2GB', 'NVIDIA GTX 1060 6GB', '15 GB', '15 GB SSD'),
(2, 'Windows 7 64-bit', 'Windows 10 64-bit', 'Intel Core i3-4160', 'Intel Core i5-6600', '6 GB', '12 GB', 'NVIDIA GTX 750 Ti', 'NVIDIA GTX 1050 Ti', '25 GB', '25 GB SSD'),
(3, 'Windows 7 64-bit', 'Windows 10 64-bit', 'Intel Core i5-4460', 'Intel Core i7-6700K', '8 GB', '16 GB', 'NVIDIA GTX 960', 'NVIDIA GTX 1070', '30 GB', '30 GB SSD'),
(4, 'Windows 7 64-bit', 'Windows 10 64-bit', 'Intel Core i5-2500K', 'Intel Core i7-7700', '8 GB', '16 GB', 'NVIDIA GTX 970', 'NVIDIA GTX 1070 Ti', '40 GB', '40 GB SSD'),
(5, 'Windows 7', 'Windows 10', 'Intel Core i3-3220', 'Intel Core i5-4460', '4 GB', '8 GB', 'NVIDIA GTX 650', 'NVIDIA GTX 1050', '5 GB', '5 GB'),
(6, 'Windows 10 64-bit', 'Windows 10 64-bit', 'Intel Core i5-6600', 'Intel Core i7-8700K', '12 GB', '16 GB', 'NVIDIA GTX 1060 6GB', 'NVIDIA RTX 2070', '50 GB', '50 GB SSD'),
(7, 'Windows 7 64-bit', 'Windows 10 64-bit', 'Intel Core i3-4130', 'Intel Core i5-6400', '6 GB', '12 GB', 'NVIDIA GTX 750', 'NVIDIA GTX 1050', '10 GB', '10 GB SSD'),
(8, 'Windows 7 64-bit', 'Windows 10 64-bit', 'Intel Core i5-4590', 'Intel Core i7-7700', '8 GB', '16 GB', 'NVIDIA GTX 970', 'NVIDIA GTX 1070', '20 GB', '20 GB SSD');

-- 查询验证
SELECT COUNT(*) as '游戏总数' FROM games;
SELECT COUNT(*) as '分类关联数' FROM game_category_mapping;
SELECT COUNT(*) as '标签关联数' FROM game_tag_mapping;
SELECT COUNT(*) as '配置要求数' FROM game_system_requirements;
