-- ============================================
-- CMS 运营后台核心表结构
-- 数据库：gacha_system_dev
-- 创建时间：2026-04-25
-- 说明：完全适配现有服务，复用已有表结构
-- ============================================

USE gacha_system_dev;

-- 1. System Configuration Table
CREATE TABLE IF NOT EXISTS `system_configs` (
  `id` INT PRIMARY KEY AUTO_INCREMENT COMMENT 'Config ID',
  `config_key` VARCHAR(100) UNIQUE NOT NULL COMMENT 'Configuration key',
  `config_value` TEXT NOT NULL COMMENT 'Configuration value',
  `config_type` VARCHAR(20) DEFAULT 'string' COMMENT 'Type: string/number/boolean/json',
  `description` VARCHAR(500) COMMENT 'Description',
  `is_public` TINYINT(1) DEFAULT 0 COMMENT 'Public access: 0-backend only, 1-frontend accessible',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
  INDEX `idx_config_key` (`config_key`),
  INDEX `idx_is_public` (`is_public`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='System configuration table';

-- 2. Announcements Table
CREATE TABLE IF NOT EXISTS `announcements` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Announcement ID',
  `title` VARCHAR(200) NOT NULL COMMENT 'Title',
  `content` TEXT NOT NULL COMMENT 'Content',
  `type` VARCHAR(20) DEFAULT 'info' COMMENT 'Type: info/activity/maintenance/update',
  `priority` INT DEFAULT 0 COMMENT 'Priority: higher number = higher priority',
  `image_url` VARCHAR(500) COMMENT 'Image URL',
  `target_type` VARCHAR(20) DEFAULT 'all' COMMENT 'Target: all/vip/new',
  `is_active` TINYINT(1) DEFAULT 1 COMMENT 'Is active',
  `start_time` DATETIME COMMENT 'Start display time',
  `end_time` DATETIME COMMENT 'End display time',
  `click_count` INT DEFAULT 0 COMMENT 'Click count',
  `created_by` BIGINT COMMENT 'Created by admin ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Updated time',
  FOREIGN KEY (`created_by`) REFERENCES `admins`(`id`) ON DELETE SET NULL,
  INDEX `idx_type` (`type`),
  INDEX `idx_is_active` (`is_active`),
  INDEX `idx_priority` (`priority`),
  INDEX `idx_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Announcements table';

-- 3. Game Images Library Table
CREATE TABLE IF NOT EXISTS `game_images` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'Image ID',
  `game_id` BIGINT NOT NULL COMMENT 'Game ID',
  `image_url` VARCHAR(500) NOT NULL COMMENT 'Image URL',
  `image_type` VARCHAR(20) DEFAULT 'screenshot' COMMENT 'Type: cover/banner/screenshot/detail',
  `sort_order` INT DEFAULT 0 COMMENT 'Sort order',
  `file_size` BIGINT COMMENT 'File size in bytes',
  `width` INT COMMENT 'Width',
  `height` INT COMMENT 'Height',
  `upload_admin_id` BIGINT COMMENT 'Uploaded by admin ID',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Upload time',
  FOREIGN KEY (`game_id`) REFERENCES `games`(`id`) ON DELETE CASCADE,
  FOREIGN KEY (`upload_admin_id`) REFERENCES `admins`(`id`) ON DELETE SET NULL,
  INDEX `idx_game_id` (`game_id`),
  INDEX `idx_image_type` (`image_type`),
  INDEX `idx_sort` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Game images library table';

-- 4. Daily Statistics Snapshot Table
CREATE TABLE IF NOT EXISTS `daily_statistics` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `stat_date` DATE NOT NULL COMMENT 'Statistics date',
  `total_users` INT DEFAULT 0 COMMENT 'Total users',
  `new_users` INT DEFAULT 0 COMMENT 'New users',
  `active_users` INT DEFAULT 0 COMMENT 'Active users',
  `total_orders` INT DEFAULT 0 COMMENT 'Total orders',
  `completed_orders` INT DEFAULT 0 COMMENT 'Completed orders',
  `total_revenue` DECIMAL(12,2) DEFAULT 0.00 COMMENT 'Total revenue',
  `total_games` INT DEFAULT 0 COMMENT 'Total games',
  `on_sale_games` INT DEFAULT 0 COMMENT 'Games on sale',
  `avg_order_amount` DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Average order amount',
  `top_game_id` BIGINT COMMENT 'Top selling game ID',
  `top_game_sales` INT DEFAULT 0 COMMENT 'Top selling game sales count',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Created time',
  UNIQUE KEY `uk_stat_date` (`stat_date`),
  INDEX `idx_date` (`stat_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Daily statistics table';

-- 5. Admin Login Logs Table
CREATE TABLE IF NOT EXISTS `admin_login_logs` (
  `id` BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
  `admin_id` BIGINT NOT NULL COMMENT 'Admin ID',
  `login_ip` VARCHAR(50) COMMENT 'Login IP',
  `user_agent` VARCHAR(500) COMMENT 'User agent',
  `login_status` TINYINT DEFAULT 1 COMMENT 'Status: 0-failed, 1-success',
  `fail_reason` VARCHAR(200) COMMENT 'Failure reason',
  `device_info` VARCHAR(200) COMMENT 'Device info',
  `location` VARCHAR(100) COMMENT 'Location',
  `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Login time',
  FOREIGN KEY (`admin_id`) REFERENCES `admins`(`id`) ON DELETE CASCADE,
  INDEX `idx_admin` (`admin_id`),
  INDEX `idx_created` (`created_at`),
  INDEX `idx_ip` (`login_ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Admin login logs table';

-- ============================================
-- Initialize Data
-- ============================================

-- Insert default system configurations
INSERT INTO `system_configs` (`config_key`, `config_value`, `config_type`, `description`, `is_public`) VALUES
('site_name', 'Game Mall CMS', 'string', 'Website name', 0),
('site_logo', '/logo.png', 'string', 'Website logo URL', 0),
('max_upload_size', '10485760', 'number', 'Max upload file size in bytes', 0),
('allowed_image_types', '["jpg","jpeg","png","gif","webp"]', 'json', 'Allowed image types', 0),
('maintenance_mode', 'false', 'boolean', 'Maintenance mode', 1),
('announcement_enabled', 'true', 'boolean', 'Enable announcements', 1);

-- Insert sample announcements
INSERT INTO `announcements` (`title`, `content`, `type`, `priority`, `is_active`, `created_by`) VALUES
('Welcome to Game Mall CMS', 'This is a powerful CMS system. You can manage games, orders, users and more.', 'info', 100, 1, 1),
('May Day Sale Preview', 'Big promotion during May Day! Many games up to 70% off!', 'activity', 90, 1, 1),
('System Maintenance Notice', 'System maintenance scheduled for Saturday 2:00-4:00 AM.', 'maintenance', 80, 1, 1);

SELECT 'CMS core tables created successfully!' AS message;
