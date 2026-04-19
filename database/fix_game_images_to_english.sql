-- ============================================
-- 修复游戏封面图片路径 - 使用英文文件名
-- 避免中文文件名导致的编码问题
-- ============================================

USE gacha_system_prod;

-- 根据数据库中的实际游戏ID更新为对应的英文文件名
UPDATE games SET cover_image = '/GamePapers/black-myth-wukong.jpg' WHERE id = 1;  -- 黑神话：悟空
UPDATE games SET cover_image = '/GamePapers/original-god.jpg' WHERE id = 2;        -- 原神
UPDATE games SET cover_image = '/GamePapers/honor-of-kings.jpg' WHERE id = 3;      -- 王者荣耀
UPDATE games SET cover_image = '/GamePapers/league-of-legends.jpg' WHERE id = 4;   -- 英雄联盟
UPDATE games SET cover_image = '/GamePapers/pubg.jpg' WHERE id = 5;                -- 绝地求生

-- 验证更新结果
SELECT id, title, cover_image FROM games ORDER BY id;

SELECT '✅ 游戏图片路径已更新为英文文件名！' AS message;
