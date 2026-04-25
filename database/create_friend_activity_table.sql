-- Friend System Phase 4: Activity Feed

-- Create friend activity table
CREATE TABLE IF NOT EXISTS friend_activity (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    uid BIGINT NOT NULL COMMENT 'User ID who performed the action',
    type VARCHAR(50) NOT NULL COMMENT 'Activity type: GAME_PURCHASED, REVIEW_PUBLISHED, STATUS_CHANGED, ACHIEVEMENT_UNLOCKED',
    content TEXT COMMENT 'Activity content description',
    game_id BIGINT COMMENT 'Related game ID',
    metadata JSON COMMENT 'Additional metadata (optional)',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Activity time',
    INDEX idx_uid (uid),
    INDEX idx_created (created_at),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Friend activity feed table';
