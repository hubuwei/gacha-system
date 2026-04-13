-- 为 games 表添加系统配置要求字段
USE gacha_system_prod;

-- 添加最低配置字段
ALTER TABLE games 
ADD COLUMN os_min VARCHAR(200) COMMENT '最低配置-操作系统' AFTER version,
ADD COLUMN cpu_min VARCHAR(200) COMMENT '最低配置-处理器' AFTER os_min,
ADD COLUMN ram_min VARCHAR(100) COMMENT '最低配置-内存' AFTER cpu_min,
ADD COLUMN gpu_min VARCHAR(200) COMMENT '最低配置-显卡' AFTER ram_min,
ADD COLUMN storage_min VARCHAR(100) COMMENT '最低配置-存储空间' AFTER gpu_min;

-- 添加推荐配置字段
ALTER TABLE games 
ADD COLUMN os_recommended VARCHAR(200) COMMENT '推荐配置-操作系统' AFTER storage_min,
ADD COLUMN cpu_recommended VARCHAR(200) COMMENT '推荐配置-处理器' AFTER os_recommended,
ADD COLUMN ram_recommended VARCHAR(100) COMMENT '推荐配置-内存' AFTER cpu_recommended,
ADD COLUMN gpu_recommended VARCHAR(200) COMMENT '推荐配置-显卡' AFTER ram_recommended,
ADD COLUMN storage_recommended VARCHAR(100) COMMENT '推荐配置-存储空间' AFTER gpu_recommended;

-- 为黑神话：悟空添加配置要求示例数据
UPDATE games SET
  os_min = 'Windows 10 64-bit',
  cpu_min = 'Intel Core i5-8400 / AMD Ryzen 5 1600',
  ram_min = '16 GB',
  gpu_min = 'NVIDIA GeForce GTX 1060 6GB / AMD Radeon RX 580 8GB',
  storage_min = '130 GB',
  os_recommended = 'Windows 10/11 64-bit',
  cpu_recommended = 'Intel Core i7-9700 / AMD Ryzen 5 5500',
  ram_recommended = '16 GB',
  gpu_recommended = 'NVIDIA GeForce RTX 2060 / AMD Radeon RX 5700 XT',
  storage_recommended = '130 GB SSD'
WHERE id = 1;

-- 验证字段添加
DESCRIBE games;

-- 查看黑神话的配置要求
SELECT id, title, os_min, cpu_min, ram_min, gpu_min, storage_min, os_recommended, cpu_recommended, ram_recommended, gpu_recommended, storage_recommended 
FROM games 
WHERE id = 1\G
