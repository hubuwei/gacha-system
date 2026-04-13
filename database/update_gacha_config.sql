-- 清空现有抽奖配置数据
-- 由于存在外键约束，临时禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 清空配置表（如果有抽奖记录也一并清空）
DELETE FROM gacha_records;
DELETE FROM gacha_config;

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- 插入原神角色配置数据（总概率 = 1）
-- SSR 级角色（5 星）：总概率 2%，90 抽保底
INSERT INTO gacha_config (item_name, rarity, base_prob, pity_threshold, is_pity_guaranteed) VALUES
('胡桃', 'SSR', 0.007, 90, TRUE),
('雷电将军', 'SSR', 0.007, 90, TRUE),
('纳西妲', 'SSR', 0.006, 90, TRUE);

-- SR 级角色（4 星）：总概率 18%
INSERT INTO gacha_config (item_name, rarity, base_prob, pity_threshold, is_pity_guaranteed) VALUES
('班尼特', 'SR', 0.045, NULL, FALSE),
('行秋', 'SR', 0.045, NULL, FALSE),
('香菱', 'SR', 0.045, NULL, FALSE),
('北斗', 'SR', 0.045, NULL, FALSE);

-- R 级角色（3 星）：总概率 80%
INSERT INTO gacha_config (item_name, rarity, base_prob, pity_threshold, is_pity_guaranteed) VALUES
('神射手之誓', 'R', 0.200, NULL, FALSE),
('弹弓', 'R', 0.200, NULL, FALSE),
('黑缨枪', 'R', 0.200, NULL, FALSE),
('以理服人', 'R', 0.200, NULL, FALSE);

-- 验证概率总和
SELECT 
    rarity,
    COUNT(*) as count,
    SUM(base_prob) as total_prob
FROM gacha_config 
GROUP BY rarity 
ORDER BY 
    CASE rarity 
        WHEN 'SSR' THEN 1 
        WHEN 'SR' THEN 2 
        WHEN 'R' THEN 3 
        WHEN 'N' THEN 4 
    END;

-- 显示所有配置
SELECT * FROM gacha_config ORDER BY rarity, item_name;
