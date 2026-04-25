-- Friend System Phase 5: Game Invitation

-- Create game invitation table
CREATE TABLE IF NOT EXISTS friend_invitation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    inviter_uid BIGINT NOT NULL COMMENT 'Inviter user ID',
    invitee_uid BIGINT NOT NULL COMMENT 'Invitee user ID',
    game_id BIGINT NOT NULL COMMENT 'Game ID to play together',
    status TINYINT NOT NULL DEFAULT 0 COMMENT 'Status: 0-pending, 1-accepted, 2-rejected, 3-expired',
    message VARCHAR(500) COMMENT 'Invitation message',
    room_code VARCHAR(50) COMMENT 'Virtual room code for simulation',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Invitation time',
    responded_at DATETIME COMMENT 'Response time',
    expires_at DATETIME COMMENT 'Expiration time',
    INDEX idx_invitee (invitee_uid),
    INDEX idx_inviter (inviter_uid),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Friend game invitation table';
