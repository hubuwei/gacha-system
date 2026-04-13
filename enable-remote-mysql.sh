#!/bin/bash
echo "=== 检查 mall_user 用户 ==="
mysql -u root -pXc037417! <<'SQL'
SELECT user, host FROM mysql.user WHERE user='mall_user';
SQL

echo ""
echo "=== 创建允许远程访问的用户 ==="
mysql -u root -pXc037417! <<'SQL'
-- 创建允许从任何主机连接的用户
CREATE USER IF NOT EXISTS 'mall_user'@'%' IDENTIFIED BY 'Xc037417!';
GRANT ALL PRIVILEGES ON gacha_system_prod.* TO 'mall_user'@'%';
FLUSH PRIVILEGES;
SELECT 'User created for remote access' AS status;
SQL

echo ""
echo "=== 检查 MySQL 绑定地址 ==="
grep -i "bind-address" /etc/mysql/mysql.conf.d/mysqld.cnf 2>/dev/null || grep -i "bind-address" /etc/my.cnf 2>/dev/null || echo "未找到 bind-address 配置"

echo ""
echo "=== 检查防火墙状态 ==="
ufw status 2>/dev/null || firewall-cmd --list-all 2>/dev/null || echo "防火墙命令不可用"

echo ""
echo "=== 检查 3306 端口监听 ==="
netstat -tlnp | grep 3306 || ss -tlnp | grep 3306
