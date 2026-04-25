-- Friend System Phase 2: Group and Blacklist

-- 1. Add group_name column to user_friend table
ALTER TABLE user_friend 
ADD COLUMN group_name VARCHAR(50) DEFAULT 'My Friends' COMMENT 'Friend group name';

-- 2. Create blacklist table
CREATE TABLE IF NOT EXISTS friend_blacklist (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    uid BIGINT NOT NULL COMMENT 'User ID',
    blocked_uid BIGINT NOT NULL COMMENT 'Blocked user ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Block time',
    UNIQUE KEY uk_uid_blocked (uid, blocked_uid),
    INDEX idx_uid (uid),
    INDEX idx_blocked_uid (blocked_uid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Friend blacklist table';
