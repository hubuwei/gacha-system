-- 创建游戏系统配置要求表
USE gacha_system_prod;

CREATE TABLE IF NOT EXISTS game_system_requirements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键ID',
    game_id BIGINT NOT NULL UNIQUE COMMENT '游戏ID',
    os_min VARCHAR(200) COMMENT '最低配置-操作系统',
    os_recommended VARCHAR(200) COMMENT '推荐配置-操作系统',
    cpu_min VARCHAR(200) COMMENT '最低配置-处理器',
    cpu_recommended VARCHAR(200) COMMENT '推荐配置-处理器',
    ram_min VARCHAR(50) COMMENT '最低配置-内存',
    ram_recommended VARCHAR(50) COMMENT '推荐配置-内存',
    gpu_min VARCHAR(200) COMMENT '最低配置-显卡',
    gpu_recommended VARCHAR(200) COMMENT '推荐配置-显卡',
    directx_min VARCHAR(50) COMMENT '最低配置-DirectX',
    directx_recommended VARCHAR(50) COMMENT '推荐配置-DirectX',
    storage_min VARCHAR(50) NOT NULL COMMENT '最低配置-存储空间',
    storage_recommended VARCHAR(50) COMMENT '推荐配置-存储空间',
    network_min VARCHAR(100) COMMENT '最低配置-网络',
    network_recommended VARCHAR(100) COMMENT '推荐配置-网络',
    sound_card_min VARCHAR(100) COMMENT '最低配置-声卡',
    sound_card_recommended VARCHAR(100) COMMENT '推荐配置-声卡',
    additional_notes TEXT COMMENT '附加说明',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    FOREIGN KEY (game_id) REFERENCES games(id) ON DELETE CASCADE,
    INDEX idx_game_id (game_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='游戏系统配置要求表';

-- 为黑神话：悟空插入配置要求数据
INSERT INTO game_system_requirements (
    game_id,
    os_min, os_recommended,
    cpu_min, cpu_recommended,
    ram_min, ram_recommended,
    gpu_min, gpu_recommended,
    storage_min, storage_recommended,
    additional_notes
) VALUES (
    1,
    'Windows 10 64-bit', 'Windows 10/11 64-bit',
    'Intel Core i5-8400 / AMD Ryzen 5 1600', 'Intel Core i7-9700 / AMD Ryzen 5 5500',
    '16 GB', '16 GB',
    'NVIDIA GeForce GTX 1060 6GB / AMD Radeon RX 580 8GB', 'NVIDIA GeForce RTX 2060 / AMD Radeon RX 5700 XT',
    '130 GB', '130 GB SSD',
    '需要SSD以获得最佳体验'
) ON DUPLICATE KEY UPDATE
    os_min = VALUES(os_min),
    os_recommended = VALUES(os_recommended),
    cpu_min = VALUES(cpu_min),
    cpu_recommended = VALUES(cpu_recommended),
    ram_min = VALUES(ram_min),
    ram_recommended = VALUES(ram_recommended),
    gpu_min = VALUES(gpu_min),
    gpu_recommended = VALUES(gpu_recommended),
    storage_min = VALUES(storage_min),
    storage_recommended = VALUES(storage_recommended),
    additional_notes = VALUES(additional_notes);

-- 验证数据
SELECT * FROM game_system_requirements WHERE game_id = 1\G
