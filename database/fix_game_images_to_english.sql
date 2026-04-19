-- ============================================
-- 修复游戏图片路径 - 使用英文文件名
-- 解决中文文件名导致的404和乱码问题
-- ============================================

USE gacha_system_prod;

-- 更新所有游戏的cover_image为英文文件名
UPDATE games SET cover_image = '/GamePapers/original-god.jpg' WHERE title = 'Genshin Impact' OR title LIKE '%原神%';
UPDATE games SET cover_image = '/GamePapers/honor-of-kings.jpg' WHERE title = 'Honor of Kings' OR title LIKE '%王者荣耀%';
UPDATE games SET cover_image = '/GamePapers/pubg.jpg' WHERE title = 'PUBG' OR title LIKE '%绝地求生%' OR title LIKE '%和平精英%';
UPDATE games SET cover_image = '/GamePapers/league-of-legends.jpg' WHERE title = 'League of Legends' OR title LIKE '%英雄联盟%';
UPDATE games SET cover_image = '/GamePapers/black-myth-wukong.jpg' WHERE title LIKE '%Black Myth%' OR title LIKE '%黑神话%';
UPDATE games SET cover_image = '/GamePapers/chinese-ghost-story.jpg' WHERE title LIKE '%倩女幽魂%';

-- 对于还没有英文文件名的游戏，暂时使用占位图或保持原样
-- 后续需要为这些游戏创建对应的英文文件名图片

-- 验证更新结果
SELECT id, title, cover_image FROM games WHERE cover_image LIKE '/GamePapers/%' LIMIT 20;

SELECT '游戏图片路径已更新为英文文件名！' AS message;
