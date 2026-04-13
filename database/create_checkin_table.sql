-- 每日签到记录表（如果已创建请忽略）
CREATE TABLE IF NOT EXISTS daily_check_in_records (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '用户 ID',
    check_in_date DATE NOT NULL COMMENT '签到日期',
    reward_points INT DEFAULT 0 COMMENT '奖励积分',
    reward_balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '奖励虚拟货币',
    consecutive_days INT DEFAULT 1 COMMENT '连续签到天数',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_date (user_id, check_in_date),
    INDEX idx_user_id (user_id),
    INDEX idx_check_in_date (check_in_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci 
COMMENT='每日签到记录表';
