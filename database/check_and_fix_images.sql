-- 查询黑神话悟空的图片路径
SELECT id, title, cover_image FROM games WHERE id = 1;

-- 更新为英文文件名
UPDATE games SET cover_image = '/GamePapers/black-myth-wukong.jpg' WHERE id = 1;

-- 验证
SELECT id, title, cover_image FROM games WHERE id = 1;
