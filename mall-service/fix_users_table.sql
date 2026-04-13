-- 修复 users 表结构
USE gacha_system;

-- 删除旧的 password 字段
ALTER TABLE users DROP COLUMN password;

-- 确保 password_hash 字段正确
ALTER TABLE users MODIFY COLUMN password_hash VARCHAR(255) NOT NULL COMMENT '密码哈希';

-- 设置默认值
UPDATE users SET account_status = 1 WHERE account_status IS NULL;
UPDATE users SET user_level = 1 WHERE user_level IS NULL;
UPDATE users SET experience_points = 0 WHERE experience_points IS NULL;
UPDATE users SET email_verified = 0 WHERE email_verified IS NULL;
UPDATE users SET phone_verified = 0 WHERE phone_verified IS NULL;

-- 显示最终表结构
DESC users;
