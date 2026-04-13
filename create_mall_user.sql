-- 创建 mall_user 用户并授权
CREATE USER IF NOT EXISTS 'mall_user'@'localhost' IDENTIFIED BY 'Xc037417!';
GRANT ALL PRIVILEGES ON gacha_system_prod.* TO 'mall_user'@'localhost';
FLUSH PRIVILEGES;

-- 验证用户创建
SELECT user, host FROM mysql.user WHERE user='mall_user';
SHOW GRANTS FOR 'mall_user'@'localhost';
