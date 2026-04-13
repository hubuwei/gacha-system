-- ============================================
-- 更新游戏封面图片路径
-- 将英文路径改为指向 GamePapers 目录中的中文文件名
-- ============================================

USE gacha_system_dev;

-- 更新有对应中文图片的游戏
UPDATE games SET cover_image = '/images/games/原神.jpg' WHERE title = 'Genshin Impact';
UPDATE games SET cover_image = '/images/games/王者荣耀.jpg' WHERE title = 'Honor of Kings';
UPDATE games SET cover_image = '/images/games/和平精英.jpg' WHERE title = 'PUBG Mobile';
UPDATE games SET cover_image = '/images/games/崩坏星穹铁道.jpg' WHERE title = 'Honkai: Star Rail';
UPDATE games SET cover_image = '/images/games/冒险岛.jpg' WHERE title = 'MapleStory';
UPDATE games SET cover_image = '/images/games/我的世界.jpg' WHERE title = 'Minecraft';
UPDATE games SET cover_image = '/images/games/堡垒之夜.jpg' WHERE title = 'Fortnite';
UPDATE games SET cover_image = '/images/games/英雄联盟.jpg' WHERE title = 'League of Legends';
UPDATE games SET cover_image = '/images/games/DOTA2.jpg' WHERE title = 'Dota 2';
UPDATE games SET cover_image = '/images/games/守望先锋.jpg' WHERE title = 'Overwatch';
UPDATE games SET cover_image = '/images/games/Apex英雄.jpg' WHERE title = 'Apex Legends';
UPDATE games SET cover_image = '/images/games/绝地求生.jpg' WHERE title = 'PUBG';
UPDATE games SET cover_image = '/images/games/Valorant.jpg' WHERE title = 'Valorant';
UPDATE games SET cover_image = '/images/games/赛博朋克2077.jpg' WHERE title = 'Cyberpunk 2077';
UPDATE games SET cover_image = '/images/games/黑神话悟空.jpg' WHERE title = 'Black Myth: Wukong';
UPDATE games SET cover_image = '/images/games/明日方舟.jpg' WHERE title = 'Arknights';
UPDATE games SET cover_image = '/images/games/阴阳师.jpg' WHERE title = 'Onmyoji';
UPDATE games SET cover_image = '/images/games/最终幻想.jpg' WHERE title LIKE 'Final Fantasy%';
UPDATE games SET cover_image = '/images/games/勇者斗恶龙.jpg' WHERE title LIKE 'Dragon Quest%';
UPDATE games SET cover_image = '/images/games/魔兽世界.jpg' WHERE title = 'World of Warcraft';
UPDATE games SET cover_image = '/images/games/GTA.jpg' WHERE title LIKE 'Grand Theft Auto%';
UPDATE games SET cover_image = '/images/games/只狼.jpg' WHERE title = 'Sekiro: Shadows Die Twice';
UPDATE games SET cover_image = '/images/games/艾尔登法环.jpg' WHERE title = 'Elden Ring';
UPDATE games SET cover_image = '/images/games/塞尔达传说.jpg' WHERE title LIKE 'The Legend of Zelda%';
UPDATE games SET cover_image = '/images/games/马里奥.jpg' WHERE title LIKE '%Mario%';
UPDATE games SET cover_image = '/images/games/鬼泣.jpg' WHERE title LIKE 'Devil May Cry%';
UPDATE games SET cover_image = '/images/games/生化危机.jpg' WHERE title LIKE 'Resident Evil%';
UPDATE games SET cover_image = '/images/games/怪物猎人.jpg' WHERE title LIKE 'Monster Hunter%';
UPDATE games SET cover_image = '/images/games/巫师3.jpg' WHERE title = 'The Witcher 3: Wild Hunt';
UPDATE games SET cover_image = '/images/games/刺客信条.jpg' WHERE title LIKE 'Assassin''s Creed%';
UPDATE games SET cover_image = '/images/games/炉石传说.jpg' WHERE title = 'Hearthstone';
UPDATE games SET cover_image = '/images/games/穿越火线.jpg' WHERE title = 'CrossFire';
UPDATE games SET cover_image = '/images/games/DNF.jpg' WHERE title = 'Dungeon & Fighter';
UPDATE games SET cover_image = '/images/games/CSGO.jpg' WHERE title = 'Counter-Strike: Global Offensive';
UPDATE games SET cover_image = '/images/games/QQ飞车.jpg' WHERE title LIKE '%QQ飞车%' OR title LIKE '%QQ Speed%';
UPDATE games SET cover_image = '/images/games/倩女幽魂.jpg' WHERE title LIKE '%倩女幽魂%';
UPDATE games SET cover_image = '/images/games/剑网3.jpg' WHERE title LIKE '%剑网%' OR title LIKE '%JX3%';
UPDATE games SET cover_image = '/images/games/天涯明月刀.jpg' WHERE title LIKE '%天涯明月刀%';
UPDATE games SET cover_image = '/images/games/天龙八部.jpg' WHERE title LIKE '%天龙八部%';
UPDATE games SET cover_image = '/images/games/大话西游.jpg' WHERE title LIKE '%大话西游%';
UPDATE games SET cover_image = '/images/games/梦幻西游.jpg' WHERE title LIKE '%梦幻西游%';
UPDATE games SET cover_image = '/images/games/诛仙.jpg' WHERE title LIKE '%诛仙%';
UPDATE games SET cover_image = '/images/games/征途.jpg' WHERE title LIKE '%征途%';
UPDATE games SET cover_image = '/images/games/奇迹MU.jpg' WHERE title LIKE '%奇迹%';
UPDATE games SET cover_image = '/images/games/热血传奇.jpg' WHERE title LIKE '%传奇%';
UPDATE games SET cover_image = '/images/games/跑跑卡丁车.jpg' WHERE title LIKE '%跑跑卡丁车%';
UPDATE games SET cover_image = '/images/games/逆水寒.jpg' WHERE title LIKE '%逆水寒%';
UPDATE games SET cover_image = '/images/games/问道.jpg' WHERE title LIKE '%问道%';
UPDATE games SET cover_image = '/images/games/口袋妖怪.jpg' WHERE title LIKE '%Pokemon%' OR title LIKE '%宝可梦%';
UPDATE games SET cover_image = '/images/games/永劫无间.jpg' WHERE title LIKE '%永劫无间%';

-- 验证更新结果
SELECT title, cover_image FROM games WHERE cover_image LIKE '/images/games/%.jpg' LIMIT 20;

SELECT '游戏图片路径更新完成!' AS message;
