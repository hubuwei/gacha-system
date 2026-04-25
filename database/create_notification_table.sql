-- 创建用户通知表
CREATE TABLE IF NOT EXISTS user_notification (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    uid BIGINT NOT NULL COMMENT '接收人ID',
    type VARCHAR(50) NOT NULL COMMENT '通知类型',
    title VARCHAR(200) COMMENT '标题',
    content TEXT COMMENT '内容',
    is_read TINYINT DEFAULT 0 COMMENT '是否已读: 0未读 1已读',
    related_id BIGINT COMMENT '关联ID（如申请ID、游戏ID等）',
    from_uid BIGINT COMMENT '发送人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_uid (uid),
    INDEX idx_is_read (is_read)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户通知表';
