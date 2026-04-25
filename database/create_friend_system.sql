-- ============================================
-- 好友社交系统数据库表结构
-- ============================================

USE gacha_system_dev;

-- 1. 用户在线状态表
CREATE TABLE IF NOT EXISTS user_online_status (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    uid BIGINT NOT NULL UNIQUE COMMENT '用户ID',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '在线状态：0离线 1在线 2离开 3游戏中',
    game_id BIGINT DEFAULT NULL COMMENT '当前游玩游戏ID（status=3时有效）',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后心跳时间',
    INDEX idx_uid (uid),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户在线状态表';

-- 2. 好友关系表
CREATE TABLE IF NOT EXISTS user_friend (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    uid BIGINT NOT NULL COMMENT '当前用户ID',
    friend_uid BIGINT NOT NULL COMMENT '好友用户ID',
    status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0待同意 1已同意 2拒绝 3拉黑',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    remark VARCHAR(100) DEFAULT NULL COMMENT '好友备注',
    UNIQUE KEY uk_uid_friend (uid, friend_uid),
    INDEX idx_uid (uid),
    INDEX idx_friend_uid (friend_uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友关系表';

-- 3. 好友申请表
CREATE TABLE IF NOT EXISTS friend_apply (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    apply_uid BIGINT NOT NULL COMMENT '申请人ID',
    receive_uid BIGINT NOT NULL COMMENT '接收人ID',
    message VARCHAR(500) DEFAULT NULL COMMENT '申请留言',
    status TINYINT NOT NULL DEFAULT 0 COMMENT '状态：0待处理 1已同意 2已拒绝',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
    INDEX idx_receive_uid (receive_uid),
    INDEX idx_apply_uid (apply_uid),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='好友申请表';

-- 插入测试数据（可选）
INSERT INTO user_online_status (uid, status, game_id) VALUES 
(1, 1, NULL),
(2, 3, 5),
(3, 0, NULL)
ON DUPLICATE KEY UPDATE status=VALUES(status), game_id=VALUES(game_id);
