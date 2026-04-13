-- 创建数据库
CREATE DATABASE IF NOT EXISTS gacha_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE gacha_system;

-- 用户表
DROP TABLE IF EXISTS users;
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    points INT DEFAULT 0 NOT NULL,
    balance DECIMAL(10, 2) DEFAULT 0.0 NOT NULL,
    current_server VARCHAR(50),
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 大区服务器表
DROP TABLE IF EXISTS game_servers;
CREATE TABLE game_servers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    server_code VARCHAR(50) UNIQUE NOT NULL,
    server_name VARCHAR(100) NOT NULL,
    status INT DEFAULT 1 NOT NULL COMMENT '1:正常，0:维护中',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='大区服务器表';

-- 充值记录表
DROP TABLE IF EXISTS recharge_records;
CREATE TABLE recharge_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    points INT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remark VARCHAR(50),
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='充值记录表';

-- 抽奖记录表
DROP TABLE IF EXISTS gacha_records;
CREATE TABLE gacha_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    pool_code VARCHAR(50) NOT NULL,
    prize_name VARCHAR(100) NOT NULL,
    prize_level INT NOT NULL COMMENT '1=SSR, 2=SR, 3=R',
    consume_points INT NOT NULL,
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time),
    INDEX idx_prize_level (prize_level)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='抽奖记录表';

-- 抽奖配置表（支持动态调整概率）
DROP TABLE IF EXISTS gacha_config;
CREATE TABLE gacha_config (
    id INT NOT NULL AUTO_INCREMENT COMMENT '配置 ID',
    item_name VARCHAR(50) NOT NULL COMMENT '道具名称',
    rarity ENUM('N','R','SR','SSR') NOT NULL COMMENT '稀有度',
    base_prob DECIMAL(5,4) NOT NULL CHECK (base_prob BETWEEN 0 AND 1) COMMENT '基础概率（如 0.01=1%）',
    pity_threshold INT DEFAULT NULL COMMENT '保底抽数（如 SSR 为 90）',
    is_pity_guaranteed BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否保底必出',
    PRIMARY KEY (id),
    INDEX idx_rarity (rarity)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='抽奖配置表';

-- 插入测试数据 - 大区服务器
INSERT INTO game_servers (server_code, server_name, status) VALUES
('east-china-1', '华东一区', 1),
('east-china-2', '华东二区', 1),
('north-china-1', '华北一区', 1),
('south-china-1', '华南一区', 1),
('us-west-1', '美西服', 1),
('us-east-1', '美东服', 1),
('eu-west-1', '欧西服', 1),
('asia-1', '亚洲服', 1);

-- 插入测试数据 - 测试用户（密码是 123456 的 SHA256 加密）
INSERT INTO users (username, password, points, balance) VALUES
('test001', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 1000, 0.00),
('test002', '8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92', 5000, 100.00);

-- 插入抽奖配置数据（根据实际表数据）
-- N 级道具
INSERT INTO gacha_config (item_name, rarity, base_prob, pity_threshold, is_pity_guaranteed) VALUES
('普通角色 A', 'N', 0.0070, NULL, TRUE);

-- R 级道具
INSERT INTO gacha_config (item_name, rarity, base_prob, pity_threshold, is_pity_guaranteed) VALUES
('稀有武器 B', 'R', 0.0030, NULL, TRUE);

-- SR 级道具
INSERT INTO gacha_config (item_name, rarity, base_prob, pity_threshold, is_pity_guaranteed) VALUES
('史诗坐骑 C', 'SR', 0.1000, 10, TRUE);

-- SSR 级道具
INSERT INTO gacha_config (item_name, rarity, base_prob, pity_threshold, is_pity_guaranteed) VALUES
('传说英雄 D', 'SSR', 0.8000, 90, TRUE);
