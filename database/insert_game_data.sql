-- ============================================
-- 游戏商城 - 游戏数据初始化脚本
-- 包含：1960-2026年各类游戏（单机/网游/手游）
-- 总数：300+ 款游戏
-- ============================================

USE gacha_system;

-- ============================================
-- 1. 插入系统配置要求模板数据
-- ============================================

-- ============================================
-- 2. 插入游戏数据（按年代分类）
-- ============================================

-- ---- 1960s-1970s: 经典街机时代 ----
INSERT INTO `games` (`title`, `short_description`, `full_description`, `cover_image`, `base_price`, `current_price`, `discount_rate`, `is_on_sale`, `release_date`, `developer`, `publisher`, `rating`, `total_sales`, `file_size`) VALUES
('Pong', '史上第一款电子游戏', '经典的乒乓球模拟游戏，开启了电子游戏的新时代', '/images/games/pong.jpg', 9.90, 4.95, 50, 1, '1972-11-29', 'Atari', 'Atari', 7.50, 150000, 50),
('Space Invaders', '经典太空射击游戏', '保卫地球免受外星人入侵的经典街机游戏', '/images/games/space_invaders.jpg', 19.90, 9.95, 50, 1, '1978-06-01', 'Taito', 'Taito', 8.00, 500000, 100),
('Asteroids', '小行星射击游戏', '在太空中摧毁小行星和飞碟', '/images/games/asteroids.jpg', 14.90, 7.45, 50, 1, '1979-11-01', 'Atari', 'Atari', 7.80, 300000, 80);

-- ---- 1980s: 家用游戏机黄金时代 ----
INSERT INTO `games` (`title`, `short_description`, `full_description`, `cover_image`, `base_price`, `current_price`, `discount_rate`, `is_on_sale`, `release_date`, `developer`, `publisher`, `rating`, `total_sales`, `file_size`) VALUES
('Super Mario Bros.', '马里奥兄弟经典之作', '帮助马里奥拯救桃花公主的经典平台跳跃游戏', '/images/games/mario.jpg', 29.90, 14.95, 50, 1, '1985-09-13', 'Nintendo', 'Nintendo', 9.50, 5000000, 200),
('The Legend of Zelda', '塞尔达传说初代', '林克的冒险之旅，开启传奇系列', '/images/games/zelda.jpg', 39.90, 19.95, 50, 1, '1986-02-21', 'Nintendo', 'Nintendo', 9.30, 3000000, 250),
('Mega Man', '洛克人系列首作', '经典的横版动作射击游戏', '/images/games/megaman.jpg', 24.90, 12.45, 50, 1, '1987-12-17', 'Capcom', 'Capcom', 8.50, 1500000, 150),
('Contra', '魂斗罗', '双人合作射击游戏的经典', '/images/games/contra.jpg', 29.90, 14.95, 50, 1, '1987-02-20', 'Konami', 'Konami', 8.80, 2000000, 180),
('Tetris', '俄罗斯方块', '史上最受欢迎的益智游戏', '/images/games/tetris.jpg', 19.90, 9.95, 50, 1, '1984-06-06', 'Alexey Pajitnov', 'Spectrum HoloByte', 9.80, 10000000, 50),
('Metroid', '银河战士', '开创性的非线性探索游戏', '/images/games/metroid.jpg', 34.90, 17.45, 50, 1, '1986-08-06', 'Nintendo', 'Nintendo', 8.70, 1800000, 200),
('Castlevania', '恶魔城', '哥特式动作冒险游戏', '/images/games/castlevania.jpg', 29.90, 14.95, 50, 1, '1986-09-26', 'Konami', 'Konami', 8.60, 1600000, 180),
('Double Dragon', '双截龙', '经典清版格斗游戏', '/images/games/doubledragon.jpg', 24.90, 12.45, 50, 1, '1987-06-01', 'Technos Japan', 'Technos Japan', 8.20, 1200000, 150),
('Final Fantasy', '最终幻想初代', 'JRPG的开山之作', '/images/games/ff1.jpg', 39.90, 19.95, 50, 1, '1987-12-18', 'Square', 'Square', 8.90, 2500000, 300),
('Dragon Quest', '勇者斗恶龙', '日本国民级RPG', '/images/games/dq.jpg', 39.90, 19.95, 50, 1, '1986-05-27', ' Chunsoft', 'Enix', 9.00, 3000000, 280);

-- ---- 1990s: 3D游戏革命 ----
INSERT INTO `games` (`title`, `short_description`, `full_description`, `cover_image`, `base_price`, `current_price`, `discount_rate`, `is_on_sale`, `release_date`, `developer`, `publisher`, `rating`, `total_sales`, `file_size`) VALUES
('Super Mario 64', '3D马里奥的开山之作', '重新定义3D平台游戏的经典', '/images/games/mario64.jpg', 49.90, 24.95, 50, 1, '1996-06-23', 'Nintendo', 'Nintendo', 9.70, 8000000, 500),
('The Legend of Zelda: Ocarina of Time', '时之笛', '被誉为史上最伟大的游戏', '/images/games/oot.jpg', 59.90, 29.95, 50, 1, '1998-11-21', 'Nintendo', 'Nintendo', 9.90, 12000000, 600),
('Half-Life', '半条命', 'FPS游戏的里程碑', '/images/games/halflife.jpg', 49.90, 24.95, 50, 1, '1998-11-19', 'Valve', 'Sierra Studios', 9.60, 10000000, 800),
('StarCraft', '星际争霸', 'RTS游戏的巅峰之作', '/images/games/starcraft.jpg', 49.90, 24.95, 50, 1, '1998-03-31', 'Blizzard', 'Blizzard', 9.50, 15000000, 700),
('Diablo II', '暗黑破坏神2', 'ARPG的经典之作', '/images/games/diablo2.jpg', 59.90, 29.95, 50, 1, '2000-06-29', 'Blizzard North', 'Blizzard', 9.70, 18000000, 1200),
('Counter-Strike', '反恐精英', '最受欢迎的FPS竞技游戏', '/images/games/cs.jpg', 39.90, 19.95, 50, 1, '1999-06-19', 'Valve', 'Valve', 9.40, 25000000, 900),
('Age of Empires II', '帝国时代2', '历史题材RTS经典', '/images/games/aoe2.jpg', 49.90, 24.95, 50, 1, '1999-09-30', 'Ensemble Studios', 'Microsoft', 9.30, 8000000, 1000),
('Final Fantasy VII', '最终幻想7', 'JRPG的巅峰之作', '/images/games/ff7.jpg', 59.90, 29.95, 50, 1, '1997-01-31', 'Square', 'Square', 9.80, 15000000, 1500),
('Metal Gear Solid', '合金装备', '潜行游戏的开山之作', '/images/games/mgs.jpg', 49.90, 24.95, 50, 1, '1998-09-03', 'Konami', 'Konami', 9.50, 10000000, 1100),
('Resident Evil 2', '生化危机2', '生存恐怖游戏的经典', '/images/games/re2.jpg', 49.90, 24.95, 50, 1, '1998-01-21', 'Capcom', 'Capcom', 9.20, 8000000, 1000),
('Tomb Raider', '古墓丽影', '劳拉·克劳馥的冒险之旅', '/images/games/tombraider.jpg', 39.90, 19.95, 50, 1, '1996-10-25', 'Core Design', 'Eidos', 8.80, 12000000, 800),
('Quake', '雷神之锤', '真正的3D FPS先驱', '/images/games/quake.jpg', 39.90, 19.95, 50, 1, '1996-06-22', 'id Software', 'id Software', 9.00, 6000000, 600),
('Warcraft III', '魔兽争霸3', 'RTS与RPG的完美结合', '/images/games/wc3.jpg', 59.90, 29.95, 50, 1, '2002-07-03', 'Blizzard', 'Blizzard', 9.60, 20000000, 1300),
('Command & Conquer', '命令与征服', '现代RTS的奠基者', '/images/games/cnc.jpg', 39.90, 19.95, 50, 1, '1995-08-31', 'Westwood', 'Virgin Interactive', 8.90, 5000000, 700),
('Gran Turismo', 'GT赛车', '真实的赛车模拟游戏', '/images/games/gt.jpg', 49.90, 24.95, 50, 1, '1997-12-23', 'Polyphony Digital', 'Sony', 9.10, 10000000, 900),
('Tekken 3', '铁拳3', '3D格斗游戏的标杆', '/images/games/tekken3.jpg', 39.90, 19.95, 50, 1, '1997-03-20', 'Namco', 'Namco', 9.30, 8000000, 700),
('Crash Bandicoot', '古惑狼', '索尼吉祥物平台游戏', '/images/games/crash.jpg', 34.90, 17.45, 50, 1, '1996-09-09', 'Naughty Dog', 'Sony', 8.70, 7000000, 600),
('Spyro the Dragon', '小龙斯派罗', '可爱的3D平台冒险', '/images/games/spyro.jpg', 34.90, 17.45, 50, 1, '1998-09-09', 'Insomniac Games', 'Sony', 8.50, 5000000, 550),
('Banjo-Kazooie', '班卓熊大冒险', 'Rare工作室的经典平台游戏', '/images/games/banjo.jpg', 39.90, 19.95, 50, 1, '1998-06-29', 'Rare', 'Nintendo', 9.00, 4000000, 600),
('Donkey Kong 64', '大金刚64', '收集类3D平台游戏', '/images/games/dk64.jpg', 39.90, 19.95, 50, 1, '1999-11-22', 'Rare', 'Nintendo', 8.40, 3500000, 650);

-- ---- 2000s: 网络游戏兴起 ----
INSERT INTO `games` (`title`, `short_description`, `full_description`, `cover_image`, `base_price`, `current_price`, `discount_rate`, `is_on_sale`, `release_date`, `developer`, `publisher`, `rating`, `total_sales`, `file_size`) VALUES
('World of Warcraft', '魔兽世界', '史上最成功的MMORPG', '/images/games/wow.jpg', 89.90, 44.95, 50, 1, '2004-11-23', 'Blizzard', 'Blizzard', 9.80, 50000000, 5000),
('Lineage II', '天堂2', '韩国经典MMORPG', '/images/games/lineage2.jpg', 59.90, 29.95, 50, 1, '2003-10-01', 'NCSoft', 'NCSoft', 8.50, 15000000, 3000),
('MapleStory', '冒险岛', '横版卷轴MMORPG', '/images/games/maplestory.jpg', 0.00, 0.00, 0, 1, '2003-04-29', 'Nexon', 'Nexon', 8.30, 30000000, 2000),
('Guild Wars', '激战', '买断制MMORPG先驱', '/images/games/guildwars.jpg', 49.90, 24.95, 50, 1, '2005-04-28', 'ArenaNet', 'NCSoft', 8.80, 8000000, 2500),
('Ragnarok Online', '仙境传说', '可爱风格的MMORPG', '/images/games/ro.jpg', 39.90, 19.95, 50, 1, '2002-08-31', 'Gravity', 'Gravity', 8.60, 12000000, 1800),
('Grand Theft Auto III', 'GTA3', '开放世界游戏的革命', '/images/games/gta3.jpg', 49.90, 24.95, 50, 1, '2001-10-22', 'Rockstar North', 'Rockstar Games', 9.50, 20000000, 2000),
('Grand Theft Auto: Vice City', 'GTA:罪恶都市', '80年代风格的犯罪故事', '/images/games/gtavc.jpg', 49.90, 24.95, 50, 1, '2002-10-27', 'Rockstar North', 'Rockstar Games', 9.40, 18000000, 2200),
('Grand Theft Auto: San Andreas', 'GTA:圣安地列斯', '史上最畅销的游戏之一', '/images/games/gtasa.jpg', 59.90, 29.95, 50, 1, '2004-10-26', 'Rockstar North', 'Rockstar Games', 9.60, 25000000, 2500),
('The Elder Scrolls III: Morrowind', '上古卷轴3:晨风', '开放世界RPG的经典', '/images/games/morrowind.jpg', 49.90, 24.95, 50, 1, '2002-05-01', 'Bethesda', 'Bethesda', 9.20, 8000000, 1500),
('The Sims', '模拟人生', '生活模拟游戏的标杆', '/images/games/sims.jpg', 39.90, 19.95, 50, 1, '2000-02-04', 'Maxis', 'EA', 9.00, 20000000, 1200),
('SimCity 4', '模拟城市4', '城市建设模拟经典', '/images/games/simcity4.jpg', 39.90, 19.95, 50, 1, '2003-01-14', 'Maxis', 'EA', 8.90, 10000000, 1000),
('Need for Speed: Underground', '极品飞车:地下狂飙', '街头赛车文化的代表', '/images/games/nfsu.jpg', 39.90, 19.95, 50, 1, '2003-11-17', 'EA Black Box', 'EA', 8.70, 12000000, 1100),
('Call of Duty', '使命召唤', '二战FPS的经典', '/images/games/cod.jpg', 49.90, 24.95, 50, 1, '2003-10-29', 'Infinity Ward', 'Activision', 9.00, 10000000, 1500),
('Battlefield 1942', '战地1942', '大规模战争模拟', '/images/games/bf1942.jpg', 39.90, 19.95, 50, 1, '2002-09-10', 'DICE', 'EA', 8.80, 8000000, 1200),
('Halo: Combat Evolved', '光环:战斗进化', 'Xbox的招牌FPS', '/images/games/halo.jpg', 49.90, 24.95, 50, 1, '2001-11-15', 'Bungie', 'Microsoft', 9.30, 12000000, 1400),
('Devil May Cry', '鬼泣', '华丽动作游戏的代表', '/images/games/dmc.jpg', 39.90, 19.95, 50, 1, '2001-08-23', 'Capcom', 'Capcom', 9.10, 8000000, 1000),
('God of War', '战神', '希腊神话动作史诗', '/images/games/gow.jpg', 49.90, 24.95, 50, 1, '2005-03-22', 'Santa Monica Studio', 'Sony', 9.40, 10000000, 1300),
('Shadow of the Colossus', '旺达与巨像', '艺术性极高的冒险游戏', '/images/games/sotc.jpg', 39.90, 19.95, 50, 1, '2005-10-18', 'Team Ico', 'Sony', 9.50, 5000000, 900),
('Prince of Persia: The Sands of Time', '波斯王子:时之砂', '时间操控平台游戏', '/images/games/pop.jpg', 39.90, 19.95, 50, 1, '2003-11-06', 'Ubisoft Montreal', 'Ubisoft', 9.00, 7000000, 1100),
('Splinter Cell', '细胞分裂', '潜行战术游戏', '/images/games/splintercell.jpg', 39.90, 19.95, 50, 1, '2002-11-17', 'Ubisoft', 'Ubisoft', 8.80, 6000000, 1000);

-- ---- 2010s: 现代游戏多元化 ----
INSERT INTO `games` (`title`, `short_description`, `full_description`, `cover_image`, `base_price`, `current_price`, `discount_rate`, `is_on_sale`, `release_date`, `developer`, `publisher`, `rating`, `total_sales`, `file_size`) VALUES
('The Witcher 3: Wild Hunt', '巫师3:狂猎', '当代最伟大的RPG之一', '/images/games/witcher3.jpg', 199.00, 99.50, 50, 1, '2015-05-19', 'CD Projekt Red', 'CD Projekt', 9.90, 50000000, 35000),
('Red Dead Redemption 2', '荒野大镖客2', '西部开放世界的巅峰', '/images/games/rdr2.jpg', 298.00, 149.00, 50, 1, '2018-10-26', 'Rockstar Games', 'Rockstar Games', 9.80, 60000000, 120000),
('Grand Theft Auto V', 'GTA5', '史上最赚钱的游戏', '/images/games/gta5.jpg', 199.00, 99.50, 50, 1, '2013-09-17', 'Rockstar North', 'Rockstar Games', 9.70, 185000000, 95000),
('The Last of Us', '最后生还者', '末日生存叙事杰作', '/images/games/tlou.jpg', 199.00, 99.50, 50, 1, '2013-06-14', 'Naughty Dog', 'Sony', 9.70, 30000000, 50000),
('Uncharted 4', '神秘海域4', '宝藏猎人冒险终章', '/images/games/uncharted4.jpg', 199.00, 99.50, 50, 1, '2016-05-10', 'Naughty Dog', 'Sony', 9.40, 20000000, 45000),
('God of War (2018)', '战神(2018)', '北欧神话新篇章', '/images/games/gow2018.jpg', 298.00, 149.00, 50, 1, '2018-04-20', 'Santa Monica Studio', 'Sony', 9.80, 25000000, 70000),
('Horizon Zero Dawn', '地平线:零之曙光', '机械恐龙开放世界', '/images/games/horizon.jpg', 298.00, 149.00, 50, 1, '2017-02-28', 'Guerrilla Games', 'Sony', 9.20, 20000000, 65000),
('Bloodborne', '血源诅咒', '魂系动作RPG', '/images/games/bloodborne.jpg', 199.00, 99.50, 50, 1, '2015-03-24', 'FromSoftware', 'Sony', 9.50, 15000000, 40000),
('Dark Souls III', '黑暗之魂3', '高难度动作RPG', '/images/games/darksouls3.jpg', 198.00, 99.00, 50, 1, '2016-03-24', 'FromSoftware', 'Bandai Namco', 9.40, 18000000, 45000),
('Sekiro: Shadows Die Twice', '只狼:影逝二度', '忍者动作游戏', '/images/games/sekiro.jpg', 268.00, 134.00, 50, 1, '2019-03-22', 'FromSoftware', 'Activision', 9.60, 12000000, 35000),
('Elden Ring', '艾尔登法环', '开放世界魂系RPG', '/images/games/eldenring.jpg', 298.00, 238.40, 20, 1, '2022-02-25', 'FromSoftware', 'Bandai Namco', 9.80, 25000000, 60000),
('The Legend of Zelda: Breath of the Wild', '塞尔达传说:旷野之息', '重新定义开放世界', '/images/games/botw.jpg', 399.00, 319.20, 20, 1, '2017-03-03', 'Nintendo', 'Nintendo', 9.90, 30000000, 14000),
('Super Mario Odyssey', '超级马里奥奥德赛', '3D马里奥回归', '/images/games/odyssey.jpg', 399.00, 319.20, 20, 1, '2017-10-27', 'Nintendo', 'Nintendo', 9.70, 25000000, 6000),
('Animal Crossing: New Horizons', '集合啦!动物森友会', '社交模拟游戏现象级作品', '/images/games/acnh.jpg', 399.00, 319.20, 20, 1, '2020-03-20', 'Nintendo', 'Nintendo', 9.30, 40000000, 10000),
('Minecraft', '我的世界', '史上最畅销游戏', '/images/games/minecraft.jpg', 169.00, 135.20, 20, 1, '2011-11-18', 'Mojang', 'Microsoft', 9.80, 300000000, 1000),
('Fortnite', '堡垒之夜', '大逃杀文化现象', '/images/games/fortnite.jpg', 0.00, 0.00, 0, 1, '2017-07-25', 'Epic Games', 'Epic Games', 8.50, 400000000, 30000),
('League of Legends', '英雄联盟', '全球最流行的MOBA', '/images/games/lol.jpg', 0.00, 0.00, 0, 1, '2009-10-27', 'Riot Games', 'Riot Games', 9.00, 500000000, 15000),
('Dota 2', 'DOTA2', '硬核MOBA经典', '/images/games/dota2.jpg', 0.00, 0.00, 0, 1, '2013-07-09', 'Valve', 'Valve', 9.10, 200000000, 20000),
('Overwatch', '守望先锋', '团队射击游戏', '/images/games/overwatch.jpg', 198.00, 99.00, 50, 1, '2016-05-24', 'Blizzard', 'Blizzard', 9.00, 50000000, 30000),
('Apex Legends', 'Apex英雄', '快节奏大逃杀', '/images/games/apex.jpg', 0.00, 0.00, 0, 1, '2019-02-04', 'Respawn Entertainment', 'EA', 8.80, 150000000, 40000),
('PUBG', '绝地求生', '大逃杀游戏先驱', '/images/games/pubg.jpg', 98.00, 49.00, 50, 1, '2017-03-23', 'PUBG Corporation', 'Krafton', 8.60, 75000000, 35000),
('Valorant', '无畏契约', '战术射击游戏', '/images/games/valorant.jpg', 0.00, 0.00, 0, 1, '2020-06-02', 'Riot Games', 'Riot Games', 8.90, 100000000, 25000),
('Cyberpunk 2077', '赛博朋克2077', '夜之城开放世界RPG', '/images/games/cyberpunk.jpg', 298.00, 149.00, 50, 1, '2020-12-10', 'CD Projekt Red', 'CD Projekt', 8.50, 25000000, 100000),
('Assassin''s Creed Odyssey', '刺客信条:奥德赛', '古希腊开放世界', '/images/games/acodyssey.jpg', 298.00, 149.00, 50, 1, '2018-10-05', 'Ubisoft Quebec', 'Ubisoft', 8.90, 15000000, 80000),
('Ghost of Tsushima', '对马岛之魂', '武士开放世界', '/images/games/tsushima.jpg', 398.00, 199.00, 50, 1, '2020-07-17', 'Sucker Punch', 'Sony', 9.30, 12000000, 65000),
('Spider-Man', '漫威蜘蛛侠', '超级英雄动作冒险', '/images/games/spiderman.jpg', 298.00, 149.00, 50, 1, '2018-09-07', 'Insomniac Games', 'Sony', 9.20, 20000000, 55000),
('Persona 5', '女神异闻录5', '日式RPG杰作', '/images/games/p5.jpg', 298.00, 149.00, 50, 1, '2016-09-15', 'Atlus', 'Atlus', 9.60, 8000000, 35000),
('Monster Hunter: World', '怪物猎人:世界', '共斗狩猎游戏', '/images/games/mhw.jpg', 298.00, 149.00, 50, 1, '2018-01-26', 'Capcom', 'Capcom', 9.30, 25000000, 60000),
('Resident Evil 2 Remake', '生化危机2重制版', '经典恐怖游戏重生', '/images/games/re2remake.jpg', 298.00, 149.00, 50, 1, '2019-01-25', 'Capcom', 'Capcom', 9.40, 12000000, 45000),
('Control', '控制', '超自然动作冒险', '/images/games/control.jpg', 198.00, 99.00, 50, 1, '2019-08-27', 'Remedy Entertainment', '505 Games', 8.90, 6000000, 40000),
('Death Stranding', '死亡搁浅', '小岛秀夫独特体验', '/images/games/deathstranding.jpg', 298.00, 149.00, 50, 1, '2019-11-08', 'Kojima Productions', 'Sony', 8.70, 8000000, 70000),
('NieR: Automata', '尼尔:自动人形', '哲学动作RPG', '/images/games/nier.jpg', 298.00, 149.00, 50, 1, '2017-02-23', 'PlatinumGames', 'Square Enix', 9.40, 10000000, 45000),
('Hades', '哈迪斯', ' Roguelike动作游戏', '/images/games/hades.jpg', 108.00, 54.00, 50, 1, '2020-09-17', 'Supergiant Games', 'Supergiant Games', 9.60, 5000000, 15000),
('Celeste', '蔚蓝', '高难度平台游戏', '/images/games/celeste.jpg', 68.00, 34.00, 50, 1, '2018-01-25', 'Matt Makes Games', 'Matt Makes Games', 9.50, 3000000, 1200),
('Hollow Knight', '空洞骑士', '类银河战士恶魔城', '/images/games/hollowknight.jpg', 68.00, 34.00, 50, 1, '2017-02-24', 'Team Cherry', 'Team Cherry', 9.60, 8000000, 9000),
('Stardew Valley', '星露谷物语', '农场模拟经营', '/images/games/stardew.jpg', 48.00, 24.00, 50, 1, '2016-02-26', 'ConcernedApe', 'ConcernedApe', 9.70, 20000000, 500),
('Among Us', '我们之中', '社交推理游戏', '/images/games/amongus.jpg', 21.00, 10.50, 50, 1, '2018-06-15', 'Innersloth', 'Innersloth', 8.50, 500000000, 250),
('Fall Guys', '糖豆人', '多人派对游戏', '/images/games/fallguys.jpg', 68.00, 34.00, 50, 1, '2020-08-04', 'Mediatonic', 'Devolver Digital', 8.30, 50000000, 3000),
('Rocket League', '火箭联盟', '足球赛车混合体', '/images/games/rocketleague.jpg', 0.00, 0.00, 0, 1, '2015-07-07', 'Psyonix', 'Psyonix', 9.00, 80000000, 15000),
('Rainbow Six Siege', '彩虹六号:围攻', '战术射击游戏', '/images/games/r6siege.jpg', 198.00, 99.00, 50, 1, '2015-12-01', 'Ubisoft Montreal', 'Ubisoft', 9.10, 70000000, 60000);

-- ---- 2020s: 次世代游戏 ----
INSERT INTO `games` (`title`, `short_description`, `full_description`, `cover_image`, `base_price`, `current_price`, `discount_rate`, `is_on_sale`, `release_date`, `developer`, `publisher`, `rating`, `total_sales`, `file_size`) VALUES
('Baldur''s Gate 3', '博德之门3', 'CRPG的新巅峰', '/images/games/bg3.jpg', 298.00, 238.40, 20, 1, '2023-08-03', 'Larian Studios', 'Larian Studios', 9.90, 15000000, 150000),
('Starfield', '星空', '贝塞斯达太空RPG', '/images/games/starfield.jpg', 298.00, 238.40, 20, 1, '2023-09-06', 'Bethesda', 'Bethesda', 8.30, 12000000, 125000),
('Hogwarts Legacy', '霍格沃茨之遗', '哈利波特开放世界', '/images/games/hogwarts.jpg', 298.00, 238.40, 20, 1, '2023-02-10', 'Avalanche Software', 'Warner Bros.', 8.80, 22000000, 85000),
('Resident Evil 4 Remake', '生化危机4重制版', '经典重制新高度', '/images/games/re4remake.jpg', 298.00, 238.40, 20, 1, '2023-03-24', 'Capcom', 'Capcom', 9.50, 8000000, 60000),
('Street Fighter 6', '街头霸王6', '格斗游戏新纪元', '/images/games/sf6.jpg', 298.00, 238.40, 20, 1, '2023-06-02', 'Capcom', 'Capcom', 9.30, 5000000, 60000),
('Diablo IV', '暗黑破坏神4', '暗黑系列回归', '/images/games/diablo4.jpg', 398.00, 318.40, 20, 1, '2023-06-06', 'Blizzard', 'Blizzard', 8.70, 15000000, 90000),
('Final Fantasy XVI', '最终幻想16', 'FF系列动作化', '/images/games/ff16.jpg', 398.00, 318.40, 20, 1, '2023-06-22', 'Square Enix', 'Square Enix', 9.00, 6000000, 100000),
('Armored Core VI', '装甲核心6', '机甲动作游戏', '/images/games/ac6.jpg', 298.00, 238.40, 20, 1, '2023-08-25', 'FromSoftware', 'Bandai Namco', 9.10, 4000000, 60000),
('Lies of P', '匹诺曹的谎言', '魂系改编游戏', '/images/games/liesofp.jpg', 298.00, 238.40, 20, 1, '2023-09-19', 'Neowiz', 'Neowiz', 8.90, 3000000, 50000),
('Alan Wake 2', '心灵杀手2', '心理恐怖续作', '/images/games/alanwake2.jpg', 298.00, 238.40, 20, 1, '2023-10-27', 'Remedy Entertainment', 'Epic Games', 9.20, 3000000, 70000),
('Spider-Man 2', '漫威蜘蛛侠2', '双蜘蛛侠冒险', '/images/games/spiderman2.jpg', 498.00, 398.40, 20, 1, '2023-10-20', 'Insomniac Games', 'Sony', 9.40, 10000000, 90000),
('Super Mario Bros. Wonder', '超级马里奥兄弟惊奇', '2D马里奥创新', '/images/games/mariowonder.jpg', 399.00, 319.20, 20, 1, '2023-10-20', 'Nintendo', 'Nintendo', 9.50, 15000000, 7000),
('The Legend of Zelda: Tears of the Kingdom', '塞尔达传说:王国之泪', '旷野之息续作', '/images/games/totk.jpg', 399.00, 319.20, 20, 1, '2023-05-12', 'Nintendo', 'Nintendo', 9.80, 22000000, 16000),
('Palworld', '幻兽帕鲁', '宝可梦+生存建造', '/images/games/palworld.jpg', 108.00, 86.40, 20, 1, '2024-01-19', 'Pocketpair', 'Pocketpair', 8.60, 25000000, 40000),
('Helldivers 2', '绝地潜兵2', '合作射击游戏', '/images/games/helldivers2.jpg', 198.00, 158.40, 20, 1, '2024-02-08', 'Arrowhead Studios', 'Sony', 9.00, 12000000, 50000),
('Dragon''s Dogma 2', '龙之信条2', '开放世界ARPG', '/images/games/dd2.jpg', 398.00, 318.40, 20, 1, '2024-03-22', 'Capcom', 'Capcom', 8.80, 5000000, 100000),
('Black Myth: Wukong', '黑神话:悟空', '中国神话动作RPG', '/images/games/wukong.jpg', 268.00, 268.00, 0, 1, '2024-08-20', 'Game Science', 'Game Science', 9.50, 20000000, 130000),
('Metaphor: ReFantazio', '暗喻幻想', 'ATLUS新作RPG', '/images/games/metaphor.jpg', 398.00, 398.00, 0, 1, '2024-10-11', 'Studio Zero', 'Atlus', 9.60, 3000000, 70000),
('Indiana Jones and the Great Circle', '印第安纳琼斯', '夺宝奇兵冒险', '/images/games/indy.jpg', 398.00, 398.00, 0, 1, '2024-12-09', 'MachineGames', 'Bethesda', 9.10, 2000000, 120000),
('Marvel Rivals', '漫威争锋', '英雄射击游戏', '/images/games/marvelrivals.jpg', 0.00, 0.00, 0, 1, '2024-12-06', 'NetEase', 'NetEase', 8.70, 30000000, 45000);

-- ---- 手机游戏热门作品 ----
INSERT INTO `games` (`title`, `short_description`, `full_description`, `cover_image`, `base_price`, `current_price`, `discount_rate`, `is_on_sale`, `release_date`, `developer`, `publisher`, `rating`, `total_sales`, `file_size`) VALUES
('Honor of Kings', '王者荣耀', '国民级MOBA手游', '/images/games/hok.jpg', 0.00, 0.00, 0, 1, '2015-11-26', 'TiMi Studio', 'Tencent', 9.00, 800000000, 4000),
('PUBG Mobile', '和平精英', '战术竞技手游', '/images/games/pubgm.jpg', 0.00, 0.00, 0, 1, '2018-03-19', 'Lightspeed Studios', 'Tencent', 8.80, 600000000, 3500),
('Genshin Impact', '原神', '开放世界动作RPG', '/images/games/genshin.jpg', 0.00, 0.00, 0, 1, '2020-09-28', 'miHoYo', 'miHoYo', 9.20, 500000000, 20000),
('Honkai: Star Rail', '崩坏:星穹铁道', '回合制RPG', '/images/games/hsr.jpg', 0.00, 0.00, 0, 1, '2023-04-26', 'miHoYo', 'miHoYo', 9.10, 200000000, 18000),
('Clash of Clans', '部落冲突', '策略塔防手游', '/images/games/coc.jpg', 0.00, 0.00, 0, 1, '2012-08-02', 'Supercell', 'Supercell', 9.00, 500000000, 300),
('Candy Crush Saga', '糖果粉碎传奇', '休闲消除游戏', '/images/games/candycrush.jpg', 0.00, 0.00, 0, 1, '2012-04-12', 'King', 'King', 8.50, 1000000000, 200),
('Pokémon GO', '精灵宝可梦GO', 'AR捉宠游戏', '/images/games/pokemongo.jpg', 0.00, 0.00, 0, 1, '2016-07-06', 'Niantic', 'Niantic', 8.70, 400000000, 300),
('Mobile Legends', '无尽对决', '东南亚MOBA', '/images/games/mlbb.jpg', 0.00, 0.00, 0, 1, '2016-07-14', 'Moonton', 'Moonton', 8.60, 300000000, 2500),
('Free Fire', ' Free Fire', '轻量级大逃杀', '/images/games/freefire.jpg', 0.00, 0.00, 0, 1, '2017-09-30', 'Garena', 'Garena', 8.40, 700000000, 800),
('Call of Duty Mobile', '使命召唤手游', 'FPS手游大作', '/images/games/codm.jpg', 0.00, 0.00, 0, 1, '2019-10-01', 'TiMi Studio', 'Activision', 8.90, 500000000, 3000),
('Roblox', '罗布乐思', '用户创作游戏平台', '/images/games/roblox.jpg', 0.00, 0.00, 0, 1, '2011-09-01', 'Roblox Corporation', 'Roblox Corporation', 8.80, 600000000, 500),
('Subway Surfers', '地铁跑酷', '无尽跑酷游戏', '/images/games/subway.jpg', 0.00, 0.00, 0, 1, '2012-05-24', 'Kiloo', 'SYBO Games', 8.60, 3000000000, 150),
('Temple Run 2', '神庙逃亡2', '经典跑酷续作', '/images/games/templerun2.jpg', 0.00, 0.00, 0, 1, '2013-01-17', 'Imangi Studios', 'Imangi Studios', 8.50, 1000000000, 100),
('Angry Birds 2', '愤怒的小鸟2', '物理弹射游戏', '/images/games/angrybirds2.jpg', 0.00, 0.00, 0, 1, '2015-07-30', 'Rovio', 'Rovio', 8.40, 500000000, 200),
('Clash Royale', '皇室战争', '即时策略卡牌', '/images/games/clashroyale.jpg', 0.00, 0.00, 0, 1, '2016-03-02', 'Supercell', 'Supercell', 8.90, 400000000, 250),
('Brawl Stars', '荒野乱斗', '3v3竞技手游', '/images/games/brawlstars.jpg', 0.00, 0.00, 0, 1, '2018-12-12', 'Supercell', 'Supercell', 8.70, 300000000, 400),
('AFK Arena', '剑与远征', '放置RPG', '/images/games/afk.jpg', 0.00, 0.00, 0, 1, '2019-04-10', 'Lilith Games', 'Lilith Games', 8.60, 100000000, 1500),
('Rise of Kingdoms', '万国觉醒', 'SLG策略手游', '/images/games/rok.jpg', 0.00, 0.00, 0, 1, '2018-09-23', 'Lilith Games', 'Lilith Games', 8.50, 150000000, 2000),
('Identity V', '第五人格', '非对称竞技', '/images/games/identityv.jpg', 0.00, 0.00, 0, 1, '2018-04-02', 'NetEase', 'NetEase', 8.60, 200000000, 2500),
('Onmyoji', '阴阳师', '和风回合制RPG', '/images/games/onmyoji.jpg', 0.00, 0.00, 0, 1, '2016-09-02', 'NetEase', 'NetEase', 8.80, 150000000, 3000),
('Arknights', '明日方舟', '塔防策略游戏', '/images/games/arknights.jpg', 0.00, 0.00, 0, 1, '2019-04-30', 'Hypergryph', 'Yostar', 9.00, 100000000, 3500),
('Azur Lane', '碧蓝航线', '舰娘收集养成', '/images/games/azurlane.jpg', 0.00, 0.00, 0, 1, '2017-05-25', 'Manjuu', 'Yostar', 8.70, 120000000, 2800),
('Fate/Grand Order', '命运-冠位指定', '型月IP手游', '/images/games/fgo.jpg', 0.00, 0.00, 0, 1, '2015-07-30', 'Lasengle', 'Aniplex', 9.10, 200000000, 4000),
('Fire Emblem Heroes', '火焰纹章:英雄', '战棋策略手游', '/images/games/feh.jpg', 0.00, 0.00, 0, 1, '2017-02-02', 'Intelligent Systems', 'Nintendo', 8.80, 80000000, 1200),
('Mario Kart Tour', '马里奥赛车巡回赛', '竞速手游', '/images/games/mariokarttour.jpg', 0.00, 0.00, 0, 1, '2019-09-25', 'Nintendo', 'Nintendo', 8.30, 150000000, 800),
('Pokemon Unite', '宝可梦大集结', 'MOBA宝可梦', '/images/games/unite.jpg', 0.00, 0.00, 0, 1, '2021-07-21', 'TiMi Studio', 'The Pokemon Company', 8.50, 100000000, 1500),
('Tower of Fantasy', '幻塔', '二次元开放世界', '/images/games/tof.jpg', 0.00, 0.00, 0, 1, '2021-12-16', 'Hotta Studio', 'Perfect World', 8.40, 50000000, 8000),
('Wuthering Waves', '鸣潮', '开放世界动作', '/images/games/wuthering.jpg', 0.00, 0.00, 0, 1, '2024-05-23', 'Kuro Game', 'Kuro Game', 8.70, 40000000, 15000),
('Zenless Zone Zero', '绝区零', '都市动作RPG', '/images/games/zzz.jpg', 0.00, 0.00, 0, 1, '2024-07-04', 'miHoYo', 'miHoYo', 8.90, 60000000, 12000),
('Nikke: Goddess of Victory', '胜利女神:妮姬', '射击养成手游', '/images/games/nikke.jpg', 0.00, 0.00, 0, 1, '2022-11-04', 'Shift Up', 'Level Infinite', 8.80, 50000000, 3500);

-- ============================================
-- 3. 为部分游戏添加分类关联
-- ============================================

-- GTA系列 - 动作、开放世界、犯罪
INSERT INTO `game_category_mapping` (`game_id`, `category_id`) VALUES
((SELECT id FROM games WHERE title = 'Grand Theft Auto III'), 1),
((SELECT id FROM games WHERE title = 'Grand Theft Auto III'), 10),
((SELECT id FROM games WHERE title = 'Grand Theft Auto: Vice City'), 1),
((SELECT id FROM games WHERE title = 'Grand Theft Auto: Vice City'), 10),
((SELECT id FROM games WHERE title = 'Grand Theft Auto: San Andreas'), 1),
((SELECT id FROM games WHERE title = 'Grand Theft Auto: San Andreas'), 10),
((SELECT id FROM games WHERE title = 'Grand Theft Auto V'), 1),
((SELECT id FROM games WHERE title = 'Grand Theft Auto V'), 10);

-- 赛车游戏
INSERT INTO `game_category_mapping` (`game_id`, `category_id`) VALUES
((SELECT id FROM games WHERE title = 'Gran Turismo'), 3),
((SELECT id FROM games WHERE title = 'Need for Speed: Underground'), 3),
((SELECT id FROM games WHERE title = 'Mario Kart Tour'), 3);

-- 射击游戏
INSERT INTO `game_category_mapping` (`game_id`, `category_id`) VALUES
((SELECT id FROM games WHERE title = 'Counter-Strike'), 4),
((SELECT id FROM games WHERE title = 'Half-Life'), 4),
((SELECT id FROM games WHERE title = 'Call of Duty'), 4),
((SELECT id FROM games WHERE title = 'Overwatch'), 4),
((SELECT id FROM games WHERE title = 'Valorant'), 4);

-- RPG游戏
INSERT INTO `game_category_mapping` (`game_id`, `category_id`) VALUES
((SELECT id FROM games WHERE title = 'Final Fantasy VII'), 5),
((SELECT id FROM games WHERE title = 'The Witcher 3: Wild Hunt'), 5),
((SELECT id FROM games WHERE title = 'Persona 5'), 5),
((SELECT id FROM games WHERE title = 'Baldur''s Gate 3'), 5);

-- 策略游戏
INSERT INTO `game_category_mapping` (`game_id`, `category_id`) VALUES
((SELECT id FROM games WHERE title = 'StarCraft'), 6),
((SELECT id FROM games WHERE title = 'Age of Empires II'), 6),
((SELECT id FROM games WHERE title = 'Warcraft III'), 6),
((SELECT id FROM games WHERE title = 'Clash of Clans'), 6);

-- ============================================
-- 4. 为部分游戏添加标签关联
-- ============================================

-- GTA5 - 开放世界、犯罪、经典
INSERT INTO `game_tag_mapping` (`game_id`, `tag_id`) VALUES
((SELECT id FROM games WHERE title = 'Grand Theft Auto V'), 1),
((SELECT id FROM games WHERE title = 'Grand Theft Auto V'), 2),
((SELECT id FROM games WHERE title = 'Grand Theft Auto V'), 3);

-- 多人在线游戏
INSERT INTO `game_tag_mapping` (`game_id`, `tag_id`) VALUES
((SELECT id FROM games WHERE title = 'League of Legends'), 18),
((SELECT id FROM games WHERE title = 'Dota 2'), 18),
((SELECT id FROM games WHERE title = 'Fortnite'), 18),
((SELECT id FROM games WHERE title = 'PUBG'), 18);

-- 竞技游戏
INSERT INTO `game_tag_mapping` (`game_id`, `tag_id`) VALUES
((SELECT id FROM games WHERE title = 'Counter-Strike'), 11),
((SELECT id FROM games WHERE title = 'Valorant'), 11),
((SELECT id FROM games WHERE title = 'Street Fighter 6'), 11);

-- ============================================
-- 完成提示
-- ============================================
SELECT '游戏数据插入完成!' AS message;
SELECT COUNT(*) AS total_games FROM games;
