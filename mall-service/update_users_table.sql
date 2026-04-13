-- 为 mall-service 的 users 表添加缺失字段
-- 用于支持用户登录、注册等功能

USE gacha_system;

-- 添加密码字段（如果不存在）
ALTER TABLE users MODIFY COLUMN password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希';

-- 添加手机号相关字段
ALTER TABLE users MODIFY COLUMN phone VARCHAR(20) DEFAULT NULL COMMENT '手机号';
ALTER TABLE users ADD COLUMN phone_verified TINYINT(1) DEFAULT 0 COMMENT '手机号是否验证';

-- 添加邮箱字段
ALTER TABLE users MODIFY COLUMN email VARCHAR(100) DEFAULT NULL COMMENT '邮箱';
ALTER TABLE users ADD COLUMN email_verified TINYINT(1) DEFAULT 0 COMMENT '邮箱是否验证';

-- 添加用户信息字段
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(500) DEFAULT NULL COMMENT '头像 URL';
ALTER TABLE users ADD COLUMN nickname VARCHAR(50) DEFAULT NULL COMMENT '昵称';
ALTER TABLE users ADD COLUMN signature VARCHAR(200) DEFAULT NULL COMMENT '个性签名';

-- 添加账号状态字段
ALTER TABLE users ADD COLUMN account_status INT DEFAULT 1 COMMENT '账号状态 (0-禁用，1-正常)';
ALTER TABLE users ADD COLUMN user_level INT DEFAULT 1 COMMENT '用户等级';
ALTER TABLE users ADD COLUMN experience_points INT DEFAULT 0 COMMENT '经验值';

-- 添加登录信息字段
ALTER TABLE users ADD COLUMN last_login_time DATETIME DEFAULT NULL COMMENT '最后登录时间';
ALTER TABLE users ADD COLUMN last_login_ip VARCHAR(50) DEFAULT NULL COMMENT '最后登录 IP';
ALTER TABLE users ADD COLUMN login_type VARCHAR(20) DEFAULT NULL COMMENT '登录类型 (password/sms)';

-- 添加时间戳字段
ALTER TABLE users ADD COLUMN created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间';
ALTER TABLE users ADD COLUMN updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间';

-- 显示表结构
DESC users;
