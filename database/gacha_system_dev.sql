/*
 Navicat Premium Dump SQL

 Source Server         : 本地
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : gacha_system_dev

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 15/04/2026 22:23:42
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin_operation_logs
-- ----------------------------
DROP TABLE IF EXISTS `admin_operation_logs`;
CREATE TABLE `admin_operation_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `admin_id` bigint NOT NULL COMMENT '管理员 ID',
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型',
  `target_table` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标表',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标 ID',
  `operation_desc` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作描述',
  `old_value` json NULL COMMENT '旧值',
  `new_value` json NULL COMMENT '新值',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP 地址',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_admin`(`admin_id` ASC) USING BTREE,
  INDEX `idx_type`(`operation_type` ASC) USING BTREE,
  INDEX `idx_created`(`created_at` ASC) USING BTREE,
  CONSTRAINT `admin_operation_logs_ibfk_1` FOREIGN KEY (`admin_id`) REFERENCES `admins` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员操作日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin_operation_logs
-- ----------------------------

-- ----------------------------
-- Table structure for admins
-- ----------------------------
DROP TABLE IF EXISTS `admins`;
CREATE TABLE `admins`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '管理员 ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码哈希',
  `real_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '真实姓名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'admin' COMMENT '角色：super_admin-超级管理员 admin-管理员 operator-操作员',
  `permissions` json NULL COMMENT '权限列表',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后登录 IP',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_role`(`role` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admins
-- ----------------------------
INSERT INTO `admins` VALUES (1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8QeN3jYpCvnFzJHkH9gPZQwLxK5iG', '系统管理员', NULL, NULL, NULL, 'super_admin', NULL, 1, NULL, NULL, '2026-04-09 21:59:17', '2026-04-09 21:59:17');

-- ----------------------------
-- Table structure for banners
-- ----------------------------
DROP TABLE IF EXISTS `banners`;
CREATE TABLE `banners`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标题',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片 URL',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标类型：game-游戏 link-链接',
  `target_id` bigint NULL DEFAULT NULL COMMENT '目标 ID',
  `target_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '目标 URL',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `start_time` datetime NULL DEFAULT NULL COMMENT '开始时间',
  `end_time` datetime NULL DEFAULT NULL COMMENT '结束时间',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_active`(`is_active` ASC) USING BTREE,
  INDEX `idx_sort`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '轮播图表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of banners
-- ----------------------------

-- ----------------------------
-- Table structure for daily_check_in_records
-- ----------------------------
DROP TABLE IF EXISTS `daily_check_in_records`;
CREATE TABLE `daily_check_in_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `check_in_date` date NOT NULL,
  `consecutive_days` int NOT NULL,
  `created_at` datetime NOT NULL,
  `reward_balance` double NOT NULL,
  `reward_points` int NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of daily_check_in_records
-- ----------------------------

-- ----------------------------
-- Table structure for delivery_address
-- ----------------------------
DROP TABLE IF EXISTS `delivery_address`;
CREATE TABLE `delivery_address`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `created_at` datetime NOT NULL,
  `detail_address` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `exchange_record_id` bigint NOT NULL,
  `phone_number` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `recipient_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `updated_at` datetime NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_1fal1lmisl5onaqa3u5njk117`(`exchange_record_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of delivery_address
-- ----------------------------

-- ----------------------------
-- Table structure for exchange_item
-- ----------------------------
DROP TABLE IF EXISTS `exchange_item`;
CREATE TABLE `exchange_item`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL,
  `current_stock` int NOT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `enabled` bit(1) NOT NULL,
  `icon_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `required_points` int NOT NULL,
  `sort_weight` int NOT NULL,
  `total_stock` int NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of exchange_item
-- ----------------------------

-- ----------------------------
-- Table structure for exchange_record
-- ----------------------------
DROP TABLE IF EXISTS `exchange_record`;
CREATE TABLE `exchange_record`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL,
  `exchange_date` date NOT NULL,
  `item_id` bigint NOT NULL,
  `status` int NOT NULL,
  `updated_at` datetime NOT NULL,
  `used_points` int NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of exchange_record
-- ----------------------------

-- ----------------------------
-- Table structure for gacha_config
-- ----------------------------
DROP TABLE IF EXISTS `gacha_config`;
CREATE TABLE `gacha_config`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `base_prob` double NOT NULL,
  `is_pity_guaranteed` bit(1) NOT NULL,
  `item_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `pity_threshold` int NULL DEFAULT NULL,
  `rarity` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gacha_config
-- ----------------------------

-- ----------------------------
-- Table structure for gacha_records
-- ----------------------------
DROP TABLE IF EXISTS `gacha_records`;
CREATE TABLE `gacha_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `cost` double NOT NULL,
  `created_at` datetime NOT NULL,
  `is_pity` bit(1) NOT NULL,
  `item_id` int NOT NULL,
  `region_id` int NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of gacha_records
-- ----------------------------

-- ----------------------------
-- Table structure for game_categories
-- ----------------------------
DROP TABLE IF EXISTS `game_categories`;
CREATE TABLE `game_categories`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '分类 ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `parent_id` int NULL DEFAULT NULL COMMENT '父分类 ID',
  `icon` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '图标',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `is_active` tinyint(1) NULL DEFAULT 1 COMMENT '是否启用',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_parent`(`parent_id` ASC) USING BTREE,
  CONSTRAINT `game_categories_ibfk_1` FOREIGN KEY (`parent_id`) REFERENCES `game_categories` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 27 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '游戏分类表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of game_categories
-- ----------------------------
INSERT INTO `game_categories` VALUES (1, '动作', NULL, '⚔️', 1, 1, '2026-04-09 21:59:17');
INSERT INTO `game_categories` VALUES (2, '冒险', NULL, '🗺️', 2, 1, '2026-04-09 21:59:17');
INSERT INTO `game_categories` VALUES (3, '赛车', NULL, '🏎️', 3, 1, '2026-04-09 21:59:17');
INSERT INTO `game_categories` VALUES (4, '射击', NULL, '🔫', 4, 1, '2026-04-09 21:59:17');
INSERT INTO `game_categories` VALUES (5, '角色扮演', NULL, '🎭', 5, 1, '2026-04-09 21:59:17');
INSERT INTO `game_categories` VALUES (6, '策略', NULL, '♟️', 6, 1, '2026-04-09 21:59:17');
INSERT INTO `game_categories` VALUES (7, '模拟', NULL, '🎮', 7, 1, '2026-04-09 21:59:17');
INSERT INTO `game_categories` VALUES (8, '体育', NULL, '⚽', 8, 1, '2026-04-09 21:59:17');
INSERT INTO `game_categories` VALUES (9, '休闲', NULL, '🎯', 9, 1, '2026-04-09 21:59:17');
INSERT INTO `game_categories` VALUES (10, '开放世界', 1, '🌍', 1, 1, '2026-04-09 21:59:17');
INSERT INTO `game_categories` VALUES (11, '竞速', 3, '🏁', 1, 1, '2026-04-09 21:59:17');
INSERT INTO `game_categories` VALUES (12, 'FPS', 4, '🎯', 1, 1, '2026-04-09 21:59:17');
INSERT INTO `game_categories` VALUES (13, '动作', NULL, '⚔️', 1, 1, '2026-04-09 22:04:48');
INSERT INTO `game_categories` VALUES (14, '冒险', NULL, '🗺️', 2, 1, '2026-04-09 22:04:48');
INSERT INTO `game_categories` VALUES (15, '赛车', NULL, '🏎️', 3, 1, '2026-04-09 22:04:48');
INSERT INTO `game_categories` VALUES (16, '射击', NULL, '🔫', 4, 1, '2026-04-09 22:04:48');
INSERT INTO `game_categories` VALUES (17, '角色扮演', NULL, '🎭', 5, 1, '2026-04-09 22:04:48');
INSERT INTO `game_categories` VALUES (18, '策略', NULL, '♟️', 6, 1, '2026-04-09 22:04:48');
INSERT INTO `game_categories` VALUES (19, '模拟', NULL, '🎮', 7, 1, '2026-04-09 22:04:48');
INSERT INTO `game_categories` VALUES (20, '体育', NULL, '⚽', 8, 1, '2026-04-09 22:04:48');
INSERT INTO `game_categories` VALUES (21, '休闲', NULL, '🎯', 9, 1, '2026-04-09 22:04:48');
INSERT INTO `game_categories` VALUES (22, '动作', NULL, '?', 1, 1, '2026-04-09 22:31:53');
INSERT INTO `game_categories` VALUES (23, '冒险', NULL, '??', 2, 1, '2026-04-09 22:31:53');
INSERT INTO `game_categories` VALUES (24, '射击', NULL, '?', 3, 1, '2026-04-09 22:31:53');
INSERT INTO `game_categories` VALUES (25, 'RPG', NULL, '??', 4, 1, '2026-04-09 22:31:53');
INSERT INTO `game_categories` VALUES (26, 'MOBA', NULL, '?', 5, 1, '2026-04-09 22:31:53');

-- ----------------------------
-- Table structure for game_category_mapping
-- ----------------------------
DROP TABLE IF EXISTS `game_category_mapping`;
CREATE TABLE `game_category_mapping`  (
  `game_id` bigint NOT NULL COMMENT '游戏 ID',
  `category_id` int NOT NULL COMMENT '分类 ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`game_id`, `category_id`) USING BTREE,
  INDEX `idx_game`(`game_id` ASC) USING BTREE,
  INDEX `idx_category`(`category_id` ASC) USING BTREE,
  CONSTRAINT `game_category_mapping_ibfk_1` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `game_category_mapping_ibfk_2` FOREIGN KEY (`category_id`) REFERENCES `game_categories` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '游戏分类关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of game_category_mapping
-- ----------------------------

-- ----------------------------
-- Table structure for game_reviews
-- ----------------------------
DROP TABLE IF EXISTS `game_reviews`;
CREATE TABLE `game_reviews`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '评论 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名 (快照)',
  `user_avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像 (快照)',
  `game_id` bigint NOT NULL COMMENT '游戏 ID',
  `rating` int NOT NULL COMMENT '评分 1-10',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评论标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '评论内容',
  `pros` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '优点',
  `cons` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '缺点',
  `play_hours` decimal(10, 2) NULL DEFAULT NULL COMMENT '游戏时长 (小时)',
  `is_verified_purchase` tinyint(1) NULL DEFAULT 0 COMMENT '是否已验证购买',
  `helpful_count` int NULL DEFAULT 0 COMMENT '有用数量',
  `not_helpful_count` int NULL DEFAULT 0 COMMENT '无用数量',
  `parent_id` bigint NULL DEFAULT NULL COMMENT '父评论 ID (回复)',
  `is_official` tinyint(1) NULL DEFAULT 0 COMMENT '是否官方回复',
  `status` tinyint NULL DEFAULT 1 COMMENT '状态：0-待审核 1-显示 2-隐藏',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `parent_id`(`parent_id` ASC) USING BTREE,
  INDEX `idx_game`(`game_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_rating`(`rating` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created`(`created_at` ASC) USING BTREE,
  CONSTRAINT `game_reviews_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `game_reviews_ibfk_2` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `game_reviews_ibfk_3` FOREIGN KEY (`parent_id`) REFERENCES `game_reviews` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '游戏评论表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of game_reviews
-- ----------------------------

-- ----------------------------
-- Table structure for game_servers
-- ----------------------------
DROP TABLE IF EXISTS `game_servers`;
CREATE TABLE `game_servers`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime NULL DEFAULT NULL,
  `server_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `server_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `status` int NOT NULL,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `UK_4hum8y6l3yrxjp9ice1e5pk8u`(`server_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of game_servers
-- ----------------------------

-- ----------------------------
-- Table structure for game_system_requirements
-- ----------------------------
DROP TABLE IF EXISTS `game_system_requirements`;
CREATE TABLE `game_system_requirements`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `game_id` bigint NOT NULL COMMENT '游戏 ID',
  `os_min` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最低操作系统',
  `os_recommended` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推荐操作系统',
  `cpu_min` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最低 CPU',
  `cpu_recommended` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推荐 CPU',
  `ram_min` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最低内存',
  `ram_recommended` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推荐内存',
  `gpu_min` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最低显卡',
  `gpu_recommended` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推荐显卡',
  `directx_min` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最低 DirectX',
  `directx_recommended` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推荐 DirectX',
  `storage_min` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '最低存储空间',
  `storage_recommended` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推荐存储空间',
  `network_min` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最低网络要求',
  `network_recommended` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推荐网络要求',
  `sound_card_min` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最低声卡',
  `sound_card_recommended` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推荐声卡',
  `additional_notes` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '额外说明',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `game_id`(`game_id` ASC) USING BTREE,
  INDEX `idx_game`(`game_id` ASC) USING BTREE,
  CONSTRAINT `game_system_requirements_ibfk_1` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '游戏配置要求表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of game_system_requirements
-- ----------------------------

-- ----------------------------
-- Table structure for game_tag_mapping
-- ----------------------------
DROP TABLE IF EXISTS `game_tag_mapping`;
CREATE TABLE `game_tag_mapping`  (
  `game_id` bigint NOT NULL COMMENT '游戏 ID',
  `tag_id` int NOT NULL COMMENT '标签 ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`game_id`, `tag_id`) USING BTREE,
  INDEX `idx_game`(`game_id` ASC) USING BTREE,
  INDEX `idx_tag`(`tag_id` ASC) USING BTREE,
  CONSTRAINT `game_tag_mapping_ibfk_1` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `game_tag_mapping_ibfk_2` FOREIGN KEY (`tag_id`) REFERENCES `game_tags` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '游戏标签关联表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of game_tag_mapping
-- ----------------------------

-- ----------------------------
-- Table structure for game_tags
-- ----------------------------
DROP TABLE IF EXISTS `game_tags`;
CREATE TABLE `game_tags`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '标签 ID',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标签名称',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '标签颜色',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 34 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '游戏标签表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of game_tags
-- ----------------------------
INSERT INTO `game_tags` VALUES (1, '开放世界', '#ff6b35', 1, '2026-04-09 21:59:17');
INSERT INTO `game_tags` VALUES (2, '犯罪', '#ff00ff', 2, '2026-04-09 21:59:17');
INSERT INTO `game_tags` VALUES (3, '经典', '#00ffff', 3, '2026-04-09 21:59:17');
INSERT INTO `game_tags` VALUES (4, '竞速', '#4ecdc4', 4, '2026-04-09 21:59:17');
INSERT INTO `game_tags` VALUES (5, '多人', '#f4a261', 5, '2026-04-09 21:59:17');
INSERT INTO `game_tags` VALUES (6, '体育', '#2d6a4f', 6, '2026-04-09 21:59:17');
INSERT INTO `game_tags` VALUES (7, '生存', '#e63946', 7, '2026-04-09 21:59:17');
INSERT INTO `game_tags` VALUES (8, '探索', '#457b9d', 8, '2026-04-09 21:59:17');
INSERT INTO `game_tags` VALUES (9, '剧情', '#1d3557', 9, '2026-04-09 21:59:17');
INSERT INTO `game_tags` VALUES (10, 'FPS', '#ff4757', 10, '2026-04-09 21:59:17');
INSERT INTO `game_tags` VALUES (11, '竞技', '#2ed573', 11, '2026-04-09 21:59:17');
INSERT INTO `game_tags` VALUES (12, '经营', '#ffa502', 12, '2026-04-09 21:59:17');
INSERT INTO `game_tags` VALUES (13, '休闲', '#7bed9f', 13, '2026-04-09 21:59:17');
INSERT INTO `game_tags` VALUES (14, '策略', '#70a1ff', 14, '2026-04-09 21:59:17');
INSERT INTO `game_tags` VALUES (15, '赛博朋克', '#a55eea', 15, '2026-04-09 21:59:17');
INSERT INTO `game_tags` VALUES (16, '黑帮', '#fd79a8', 16, '2026-04-09 21:59:17');
INSERT INTO `game_tags` VALUES (17, '特技', '#fdcb6e', 17, '2026-04-09 21:59:17');
INSERT INTO `game_tags` VALUES (18, '多人在线', '#6c5ce7', 18, '2026-04-09 21:59:17');
INSERT INTO `game_tags` VALUES (29, '热门', '#ff4757', 1, '2026-04-09 22:31:53');
INSERT INTO `game_tags` VALUES (30, '新游', '#2ed573', 2, '2026-04-09 22:31:53');
INSERT INTO `game_tags` VALUES (31, '免费', '#1e90ff', 3, '2026-04-09 22:31:53');
INSERT INTO `game_tags` VALUES (32, '折扣', '#ffa502', 4, '2026-04-09 22:31:53');
INSERT INTO `game_tags` VALUES (33, '国产', '#3742fa', 5, '2026-04-09 22:31:53');

-- ----------------------------
-- Table structure for games
-- ----------------------------
DROP TABLE IF EXISTS `games`;
CREATE TABLE `games`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '游戏 ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '游戏标题',
  `short_description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '简短描述',
  `full_description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '完整描述',
  `cover_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '封面图片 URL',
  `banner_image` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '横幅图片 URL',
  `screenshots` json NULL COMMENT '截图 JSON 数组',
  `trailer_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '预告片 URL',
  `base_price` decimal(10, 2) NOT NULL COMMENT '基础价格',
  `current_price` decimal(10, 2) NOT NULL COMMENT '当前价格',
  `discount_rate` int NULL DEFAULT 0 COMMENT '折扣率 (0-100)',
  `discount_start` datetime NULL DEFAULT NULL COMMENT '折扣开始时间',
  `discount_end` datetime NULL DEFAULT NULL COMMENT '折扣结束时间',
  `is_featured` tinyint(1) NULL DEFAULT 0 COMMENT '是否精选',
  `is_on_sale` tinyint(1) NULL DEFAULT 0 COMMENT '是否在售',
  `release_date` date NULL DEFAULT NULL COMMENT '发布日期',
  `developer` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '开发商',
  `publisher` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发行商',
  `rating` decimal(3, 2) NULL DEFAULT 0.00 COMMENT '评分 (0-10)',
  `rating_count` int NULL DEFAULT 0 COMMENT '评分人数',
  `total_sales` int NULL DEFAULT 0 COMMENT '总销量',
  `total_reviews` int NULL DEFAULT 0 COMMENT '总评论数',
  `download_count` int NULL DEFAULT 0 COMMENT '下载量',
  `file_size` bigint NULL DEFAULT NULL COMMENT '文件大小 (MB)',
  `version` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '当前版本',
  `last_update` datetime NULL DEFAULT NULL COMMENT '最后更新时间',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_title`(`title` ASC) USING BTREE,
  INDEX `idx_price`(`current_price` ASC) USING BTREE,
  INDEX `idx_featured`(`is_featured` ASC) USING BTREE,
  INDEX `idx_on_sale`(`is_on_sale` ASC) USING BTREE,
  INDEX `idx_rating`(`rating` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '游戏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of games
-- ----------------------------
INSERT INTO `games` VALUES (1, '黑神话：悟空', '中国首款3A大作，西游题材动作RPG', NULL, '/GamePapers/黑神话悟空.jpg', NULL, NULL, NULL, 298.00, 149.00, 50, NULL, NULL, 1, 1, NULL, '游戏科学', '游戏科学', 9.50, 0, 0, 0, 0, NULL, NULL, NULL, '2026-04-09 22:31:15', '2026-04-11 15:00:56');
INSERT INTO `games` VALUES (2, '原神', '开放世界冒险游戏', NULL, '/GamePapers/原神.jpg', NULL, NULL, NULL, 0.00, 0.00, 0, NULL, NULL, 1, 1, NULL, '米哈游', '米哈游', 8.80, 0, 0, 0, 0, NULL, NULL, NULL, '2026-04-09 22:31:15', '2026-04-09 22:31:15');
INSERT INTO `games` VALUES (3, '王者荣耀', 'MOBA竞技手游', NULL, '/GamePapers/王者荣耀.jpg', NULL, NULL, NULL, 0.00, 0.00, 0, NULL, NULL, 1, 1, NULL, '腾讯天美', '腾讯游戏', 8.50, 0, 0, 0, 0, NULL, NULL, NULL, '2026-04-09 22:31:15', '2026-04-09 22:31:15');
INSERT INTO `games` VALUES (4, '英雄联盟', '经典MOBA游戏', NULL, '/GamePapers/英雄联盟.jpg', NULL, NULL, NULL, 0.00, 0.00, 0, NULL, NULL, 1, 1, NULL, 'Riot Games', '腾讯游戏', 9.00, 0, 0, 0, 0, NULL, NULL, NULL, '2026-04-09 22:31:15', '2026-04-09 22:31:15');
INSERT INTO `games` VALUES (5, '绝地求生', '战术竞技射击游戏', NULL, '/GamePapers/绝地求生.jpg', NULL, NULL, NULL, 98.00, 49.00, 50, NULL, NULL, 0, 1, NULL, 'PUBG Corporation', 'Krafton', 8.20, 0, 0, 0, 0, NULL, NULL, NULL, '2026-04-09 22:31:15', '2026-04-09 22:31:15');

-- ----------------------------
-- Table structure for login_logs
-- ----------------------------
DROP TABLE IF EXISTS `login_logs`;
CREATE TABLE `login_logs`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `login_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录方式：password sms qr',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'IP 地址',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户代理',
  `login_status` tinyint NULL DEFAULT 1 COMMENT '状态：0-失败 1-成功',
  `fail_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '失败原因',
  `device_info` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '设备信息',
  `location` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录地点',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_created`(`created_at` ASC) USING BTREE,
  INDEX `idx_ip`(`ip_address` ASC) USING BTREE,
  CONSTRAINT `login_logs_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '登录日志表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of login_logs
-- ----------------------------

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `is_read` bit(1) NULL DEFAULT NULL,
  `related_game_id` bigint NULL DEFAULT NULL,
  `related_order_id` bigint NULL DEFAULT NULL,
  `related_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  `message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 20 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notifications
-- ----------------------------
INSERT INTO `notifications` VALUES (1, '全场游戏低至3折，限时7天！快来选购心仪的游戏吧~', '2026-04-11 16:43:48.649600', b'0', NULL, NULL, 'game', '🎉 春季大促开启！', 'promotion', 1, NULL);
INSERT INTO `notifications` VALUES (2, '全场游戏低至3折，限时7天！快来选购心仪的游戏吧~', '2026-04-11 16:43:48.666733', b'0', NULL, NULL, 'game', '🎉 春季大促开启！', 'promotion', 2, NULL);
INSERT INTO `notifications` VALUES (3, '全场游戏低至3折，限时7天！快来选购心仪的游戏吧~', '2026-04-11 16:43:48.671099', b'1', NULL, NULL, 'game', '🎉 春季大促开启！', 'promotion', 5, NULL);
INSERT INTO `notifications` VALUES (4, '1', '2026-04-11 17:01:57.722048', b'0', NULL, NULL, 'game', '1', 'promotion', 1, NULL);
INSERT INTO `notifications` VALUES (5, '1', '2026-04-11 17:01:57.729031', b'0', NULL, NULL, 'game', '1', 'promotion', 2, NULL);
INSERT INTO `notifications` VALUES (6, '1', '2026-04-11 17:01:57.734962', b'1', NULL, NULL, 'game', '1', 'promotion', 5, NULL);
INSERT INTO `notifications` VALUES (7, '1', '2026-04-11 17:01:57.740716', b'0', NULL, NULL, 'game', '1', 'promotion', 6, NULL);
INSERT INTO `notifications` VALUES (8, NULL, '2026-04-11 18:07:36.287874', b'0', 1, NULL, 'game', '??????!', 'promotion', 1, '??????3?,??7?!??????????~ ????????,????????????????????');
INSERT INTO `notifications` VALUES (9, NULL, '2026-04-11 18:07:36.297952', b'0', 1, NULL, 'game', '??????!', 'promotion', 2, '??????3?,??7?!??????????~ ????????,????????????????????');
INSERT INTO `notifications` VALUES (10, NULL, '2026-04-11 18:07:36.301046', b'1', 1, NULL, 'game', '??????!', 'promotion', 5, '??????3?,??7?!??????????~ ????????,????????????????????');
INSERT INTO `notifications` VALUES (11, NULL, '2026-04-11 18:07:36.303514', b'0', 1, NULL, 'game', '??????!', 'promotion', 6, '??????3?,??7?!??????????~ ????????,????????????????????');
INSERT INTO `notifications` VALUES (12, NULL, '2026-04-11 18:09:06.093595', b'0', NULL, NULL, 'game', '?? ????', 'promotion', 1, '????3??,?????!');
INSERT INTO `notifications` VALUES (13, NULL, '2026-04-11 18:09:06.097594', b'0', NULL, NULL, 'game', '?? ????', 'promotion', 2, '????3??,?????!');
INSERT INTO `notifications` VALUES (15, NULL, '2026-04-11 18:09:06.104729', b'0', NULL, NULL, 'game', '?? ????', 'promotion', 6, '????3??,?????!');
INSERT INTO `notifications` VALUES (16, NULL, '2026-04-11 18:12:22.805922', b'0', NULL, NULL, 'game', '🎉 春季大促开启！', 'promotion', 1, '全场游戏低至3折，限时7天！快来选购心仪的游戏吧~ 这是一个测试消息，用于验证通知内容是否正确保存到数据库中。');
INSERT INTO `notifications` VALUES (17, NULL, '2026-04-11 18:12:22.821269', b'0', NULL, NULL, 'game', '🎉 春季大促开启！', 'promotion', 2, '全场游戏低至3折，限时7天！快来选购心仪的游戏吧~ 这是一个测试消息，用于验证通知内容是否正确保存到数据库中。');
INSERT INTO `notifications` VALUES (18, NULL, '2026-04-11 18:12:22.825478', b'1', NULL, NULL, 'game', '🎉 春季大促开启！', 'promotion', 5, '全场游戏低至3折，限时7天！快来选购心仪的游戏吧~ 这是一个测试消息，用于验证通知内容是否正确保存到数据库中。');
INSERT INTO `notifications` VALUES (19, NULL, '2026-04-11 18:12:22.829967', b'0', NULL, NULL, 'game', '🎉 春季大促开启！', 'promotion', 6, '全场游戏低至3折，限时7天！快来选购心仪的游戏吧~ 这是一个测试消息，用于验证通知内容是否正确保存到数据库中。');

-- ----------------------------
-- Table structure for order_items
-- ----------------------------
DROP TABLE IF EXISTS `order_items`;
CREATE TABLE `order_items`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `order_id` bigint NOT NULL COMMENT '订单 ID',
  `game_id` bigint NOT NULL COMMENT '游戏 ID',
  `game_title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '游戏名称 (快照)',
  `game_cover` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '游戏封面 (快照)',
  `quantity` int NOT NULL DEFAULT 1 COMMENT '数量',
  `original_price` decimal(10, 2) NOT NULL COMMENT '原价',
  `actual_price` decimal(10, 2) NOT NULL COMMENT '实际价格',
  `discount_rate` int NULL DEFAULT 0 COMMENT '折扣率',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_order`(`order_id` ASC) USING BTREE,
  INDEX `idx_game`(`game_id` ASC) USING BTREE,
  CONSTRAINT `order_items_ibfk_1` FOREIGN KEY (`order_id`) REFERENCES `orders` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `order_items_ibfk_2` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单详情表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_items
-- ----------------------------
INSERT INTO `order_items` VALUES (1, 1, 1, '黑神话：悟空', '/GamePapers/黑神话悟空.jpg', 1, 298.00, 268.20, 10, '2026-04-10 21:16:22');
INSERT INTO `order_items` VALUES (2, 2, 1, '黑神话：悟空', '/GamePapers/黑神话悟空.jpg', 1, 298.00, 268.20, 10, '2026-04-10 21:16:30');
INSERT INTO `order_items` VALUES (3, 3, 5, '绝地求生', '/GamePapers/绝地求生.jpg', 1, 98.00, 49.00, 50, '2026-04-10 21:24:47');
INSERT INTO `order_items` VALUES (4, 4, 5, '绝地求生', '/GamePapers/绝地求生.jpg', 1, 98.00, 49.00, 50, '2026-04-10 21:30:34');
INSERT INTO `order_items` VALUES (5, 5, 5, '绝地求生', '/GamePapers/绝地求生.jpg', 1, 98.00, 49.00, 50, '2026-04-10 21:36:21');
INSERT INTO `order_items` VALUES (6, 6, 1, '黑神话：悟空', '/GamePapers/黑神话悟空.jpg', 1, 298.00, 268.20, 10, '2026-04-10 21:53:57');

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '订单 ID',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `discount_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '优惠金额',
  `actual_amount` decimal(10, 2) NOT NULL COMMENT '实际支付金额',
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付方式：balance-余额 alipay-支付宝 wechat-微信',
  `payment_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending' COMMENT '支付状态：pending-待支付 paid-已支付 failed-失败 refunded-已退款',
  `payment_time` datetime NULL DEFAULT NULL COMMENT '支付时间',
  `order_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'pending' COMMENT '订单状态：pending-待支付 completed-已完成 cancelled-已取消',
  `refund_time` datetime NULL DEFAULT NULL COMMENT '退款时间',
  `refund_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '退款原因',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_status`(`payment_status` ASC) USING BTREE,
  INDEX `idx_created`(`created_at` ASC) USING BTREE,
  CONSTRAINT `orders_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (1, 'ORD202604102116227104', 5, 268.20, 29.80, 268.20, 'alipay', 'pending', NULL, 'cancelled', NULL, NULL, NULL, '2026-04-10 21:16:22', '2026-04-11 10:02:23');
INSERT INTO `orders` VALUES (2, 'ORD202604102116306808', 5, 268.20, 29.80, 268.20, 'wechat', 'pending', NULL, 'cancelled', NULL, NULL, NULL, '2026-04-10 21:16:30', '2026-04-11 10:02:23');
INSERT INTO `orders` VALUES (3, 'ORD202604102124476717', 5, 49.00, 49.00, 49.00, 'alipay', 'pending', NULL, 'cancelled', NULL, NULL, NULL, '2026-04-10 21:24:47', '2026-04-11 10:02:23');
INSERT INTO `orders` VALUES (4, 'ORD202604102130341329', 5, 49.00, 49.00, 49.00, 'wechat', 'pending', NULL, 'cancelled', NULL, NULL, NULL, '2026-04-10 21:30:34', '2026-04-11 10:02:23');
INSERT INTO `orders` VALUES (5, 'ORD202604102136215636', 5, 49.00, 49.00, 49.00, 'alipay', 'pending', NULL, 'cancelled', NULL, NULL, NULL, '2026-04-10 21:36:21', '2026-04-10 21:36:27');
INSERT INTO `orders` VALUES (6, 'ORD202604102153565969', 5, 268.20, 29.80, 268.20, 'balance', 'paid', '2026-04-10 21:53:57', 'completed', NULL, NULL, NULL, '2026-04-10 21:53:57', '2026-04-10 21:53:57');

-- ----------------------------
-- Table structure for recharge_records
-- ----------------------------
DROP TABLE IF EXISTS `recharge_records`;
CREATE TABLE `recharge_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` double NOT NULL,
  `create_time` datetime NOT NULL,
  `points` int NOT NULL,
  `remark` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recharge_records
-- ----------------------------

-- ----------------------------
-- Table structure for regions
-- ----------------------------
DROP TABLE IF EXISTS `regions`;
CREATE TABLE `regions`  (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `server_id` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of regions
-- ----------------------------

-- ----------------------------
-- Table structure for review_images
-- ----------------------------
DROP TABLE IF EXISTS `review_images`;
CREATE TABLE `review_images`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `review_id` bigint NOT NULL COMMENT '评论 ID',
  `image_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '图片 URL',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_review`(`review_id` ASC) USING BTREE,
  CONSTRAINT `review_images_ibfk_1` FOREIGN KEY (`review_id`) REFERENCES `game_reviews` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '评论图片表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of review_images
-- ----------------------------

-- ----------------------------
-- Table structure for seckill_products
-- ----------------------------
DROP TABLE IF EXISTS `seckill_products`;
CREATE TABLE `seckill_products`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `end_time` datetime NOT NULL,
  `interval_hours` int NOT NULL,
  `is_active` bit(1) NOT NULL,
  `max_per_user` int NOT NULL,
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `original_price` double NOT NULL,
  `remaining_stock` int NOT NULL,
  `seckill_points` int NOT NULL,
  `start_time` datetime NOT NULL,
  `total_stock` int NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of seckill_products
-- ----------------------------

-- ----------------------------
-- Table structure for seckill_records
-- ----------------------------
DROP TABLE IF EXISTS `seckill_records`;
CREATE TABLE `seckill_records`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime NOT NULL,
  `points` int NOT NULL,
  `product_id` bigint NOT NULL,
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `seckill_time` datetime NOT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of seckill_records
-- ----------------------------

-- ----------------------------
-- Table structure for shopping_cart
-- ----------------------------
DROP TABLE IF EXISTS `shopping_cart`;
CREATE TABLE `shopping_cart`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `game_id` bigint NOT NULL COMMENT '游戏 ID',
  `quantity` int NULL DEFAULT 1 COMMENT '数量',
  `added_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  `checked` tinyint(1) NULL DEFAULT 1 COMMENT '是否选中',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_game`(`user_id` ASC, `game_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_game`(`game_id` ASC) USING BTREE,
  CONSTRAINT `shopping_cart_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `shopping_cart_ibfk_2` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shopping_cart
-- ----------------------------

-- ----------------------------
-- Table structure for system_notifications
-- ----------------------------
DROP TABLE IF EXISTS `system_notifications`;
CREATE TABLE `system_notifications`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '通知 ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知内容',
  `type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '类型：discount-折扣 release-发售 system-系统 order-订单',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读',
  `read_at` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `related_game_id` bigint NULL DEFAULT NULL COMMENT '关联游戏 ID',
  `related_order_id` bigint NULL DEFAULT NULL COMMENT '关联订单 ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_type`(`type` ASC) USING BTREE,
  INDEX `idx_read`(`is_read` ASC) USING BTREE,
  INDEX `idx_created`(`created_at` ASC) USING BTREE,
  CONSTRAINT `system_notifications_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统通知表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_notifications
-- ----------------------------

-- ----------------------------
-- Table structure for transactions
-- ----------------------------
DROP TABLE IF EXISTS `transactions`;
CREATE TABLE `transactions`  (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `amount` decimal(10, 2) NOT NULL,
  `balance_after` decimal(10, 2) NULL DEFAULT NULL,
  `balance_before` decimal(10, 2) NULL DEFAULT NULL,
  `created_at` datetime(6) NULL DEFAULT NULL,
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `payment_method` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `related_order_id` bigint NULL DEFAULT NULL,
  `related_order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `transaction_status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL,
  `transaction_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of transactions
-- ----------------------------
INSERT INTO `transactions` VALUES (1, 100.00, 100.00, 0.00, '2026-04-10 20:34:30.423960', '账户充值', 'balance', NULL, NULL, 'completed', 'recharge', 5);
INSERT INTO `transactions` VALUES (2, 50.00, 150.00, 100.00, '2026-04-10 20:34:48.157593', '账户充值', 'alipay', NULL, NULL, 'completed', 'recharge', 5);
INSERT INTO `transactions` VALUES (3, 100.00, 250.00, 150.00, '2026-04-10 20:35:53.454042', '账户充值', 'alipay', NULL, NULL, 'completed', 'recharge', 5);
INSERT INTO `transactions` VALUES (4, 100.00, 350.00, 250.00, '2026-04-10 20:38:02.829212', '账户充值', 'alipay', NULL, NULL, 'completed', 'recharge', 5);
INSERT INTO `transactions` VALUES (5, 100.00, 450.00, 350.00, '2026-04-10 20:38:10.700589', '账户充值', 'balance', NULL, NULL, 'completed', 'recharge', 5);
INSERT INTO `transactions` VALUES (6, 100.00, 550.00, 450.00, '2026-04-10 20:38:42.139195', '账户充值', 'balance', NULL, NULL, 'completed', 'recharge', 5);
INSERT INTO `transactions` VALUES (7, 100.00, 650.00, 550.00, '2026-04-10 20:42:25.874279', '账户充值', 'balance', NULL, NULL, 'completed', 'recharge', 5);
INSERT INTO `transactions` VALUES (8, 50.00, 700.00, 650.00, '2026-04-10 20:51:27.885847', '账户充值', 'balance', NULL, NULL, 'completed', 'recharge', 5);
INSERT INTO `transactions` VALUES (9, 268.20, 431.80, 700.00, '2026-04-10 21:53:56.722043', '购买游戏 - 订单号: ORD202604102153565969', NULL, 6, 'ORD202604102153565969', 'completed', 'purchase', 5);
INSERT INTO `transactions` VALUES (10, 30.00, 461.80, 431.80, '2026-04-11 18:13:18.626939', '账户充值 - 单号: RCH17759023986135439', 'balance', NULL, 'RCH17759023986135439', 'completed', 'recharge', 5);

-- ----------------------------
-- Table structure for user_wallets
-- ----------------------------
DROP TABLE IF EXISTS `user_wallets`;
CREATE TABLE `user_wallets`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `balance` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '可用余额',
  `frozen_balance` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '冻结余额',
  `total_recharge` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '累计充值',
  `total_consumed` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '累计消费',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  CONSTRAINT `user_wallets_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户钱包表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user_wallets
-- ----------------------------
INSERT INTO `user_wallets` VALUES (1, 1, 1000.00, 0.00, 1000.00, 0.00, '2026-04-09 21:59:17', '2026-04-09 21:59:17');
INSERT INTO `user_wallets` VALUES (2, 2, 500.00, 0.00, 500.00, 0.00, '2026-04-09 21:59:17', '2026-04-09 21:59:17');
INSERT INTO `user_wallets` VALUES (3, 5, 461.80, 0.00, 730.00, 268.20, '2026-04-09 22:20:23', '2026-04-11 18:13:19');
INSERT INTO `user_wallets` VALUES (4, 6, 0.00, 0.00, 0.00, 0.00, '2026-04-11 17:01:38', '2026-04-11 17:01:38');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户 ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码哈希',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `phone_verified` tinyint(1) NULL DEFAULT 0 COMMENT '手机号是否验证',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱',
  `email_verified` tinyint(1) NULL DEFAULT 0 COMMENT '邮箱是否验证',
  `avatar_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像 URL',
  `nickname` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `signature` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '个性签名',
  `account_status` tinyint NULL DEFAULT 1 COMMENT '账号状态：0-封禁 1-正常 2-冻结',
  `user_level` int NULL DEFAULT 1 COMMENT '用户等级',
  `experience_points` int NULL DEFAULT 0 COMMENT '经验值',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后登录 IP',
  `login_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '登录方式：password-密码 sms-短信 qr-二维码',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `username`(`username` ASC) USING BTREE,
  UNIQUE INDEX `phone`(`phone` ASC) USING BTREE,
  UNIQUE INDEX `email`(`email` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE,
  INDEX `idx_status`(`account_status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 7 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'testuser1', 'e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446', NULL, 0, 'test1@example.com', 0, NULL, '测试用户1', NULL, 1, 5, 0, NULL, NULL, NULL, '2026-04-09 21:59:17', '2026-04-09 22:23:11');
INSERT INTO `users` VALUES (2, 'testuser2', 'e606e38b0d8c19b24cf0ee3808183162ea7cd63ff7912dbb22b5e803286b4446', NULL, 0, 'test2@example.com', 0, NULL, '测试用户2', NULL, 1, 3, 0, NULL, NULL, NULL, '2026-04-09 21:59:17', '2026-04-09 22:23:11');
INSERT INTO `users` VALUES (5, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', '14758967428', 1, '1712133303@QQ.COM', 1, 'http://localhost:8084/uploads/avatars/6d10ab3b-a47e-41a4-ba1b-16c998882fbc.jpg', '管理员', NULL, 1, 10, 0, NULL, NULL, NULL, '2026-04-09 22:19:52', '2026-04-11 08:21:15');
INSERT INTO `users` VALUES (6, 'test001', 'ecd71870d1963316a97e3ac3408c9835ad8cf0f3c1bc703527c30265534f75ae', NULL, 0, '2519814855@QQ.COM', 0, NULL, NULL, NULL, 1, 1, 0, NULL, NULL, NULL, '2026-04-11 09:01:02', '2026-04-11 09:01:02');

-- ----------------------------
-- Table structure for wallet_transactions
-- ----------------------------
DROP TABLE IF EXISTS `wallet_transactions`;
CREATE TABLE `wallet_transactions`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `transaction_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '交易类型：recharge-充值 purchase-购买 refund-退款 withdraw-提现',
  `amount` decimal(10, 2) NOT NULL COMMENT '金额',
  `balance_before` decimal(10, 2) NOT NULL COMMENT '交易前余额',
  `balance_after` decimal(10, 2) NOT NULL COMMENT '交易后余额',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '交易描述',
  `related_order_id` bigint NULL DEFAULT NULL COMMENT '关联订单 ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_type`(`transaction_type` ASC) USING BTREE,
  INDEX `idx_order_id`(`related_order_id` ASC) USING BTREE,
  CONSTRAINT `wallet_transactions_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户钱包流水表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wallet_transactions
-- ----------------------------

-- ----------------------------
-- Table structure for wallets
-- ----------------------------
DROP TABLE IF EXISTS `wallets`;
CREATE TABLE `wallets`  (
  `user_id` bigint NOT NULL,
  `balance` double NOT NULL,
  `points` int NOT NULL,
  `updated_at` datetime NOT NULL,
  PRIMARY KEY (`user_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wallets
-- ----------------------------
INSERT INTO `wallets` VALUES (6, 0, 1000, '2026-04-11 09:01:02');

-- ----------------------------
-- Table structure for wishlists
-- ----------------------------
DROP TABLE IF EXISTS `wishlists`;
CREATE TABLE `wishlists`  (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户 ID',
  `game_id` bigint NOT NULL COMMENT '游戏 ID',
  `priority` int NULL DEFAULT 1 COMMENT '优先级：1-普通 2-重要 3-非常想要',
  `notify_discount` tinyint(1) NULL DEFAULT 1 COMMENT '是否通知折扣',
  `notify_release` tinyint(1) NULL DEFAULT 1 COMMENT '是否通知发售',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '添加时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_game`(`user_id` ASC, `game_id` ASC) USING BTREE,
  INDEX `idx_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_game`(`game_id` ASC) USING BTREE,
  CONSTRAINT `wishlists_ibfk_1` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `wishlists_ibfk_2` FOREIGN KEY (`game_id`) REFERENCES `games` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '愿望单表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of wishlists
-- ----------------------------
INSERT INTO `wishlists` VALUES (1, 5, 1, 1, 1, 1, '2026-04-10 21:16:11');

-- ----------------------------
-- View structure for v_game_detail
-- ----------------------------
DROP VIEW IF EXISTS `v_game_detail`;
CREATE ALGORITHM = UNDEFINED SQL SECURITY DEFINER VIEW `v_game_detail` AS select `g`.`id` AS `id`,`g`.`title` AS `title`,`g`.`short_description` AS `short_description`,`g`.`full_description` AS `full_description`,`g`.`cover_image` AS `cover_image`,`g`.`banner_image` AS `banner_image`,`g`.`screenshots` AS `screenshots`,`g`.`trailer_url` AS `trailer_url`,`g`.`base_price` AS `base_price`,`g`.`current_price` AS `current_price`,`g`.`discount_rate` AS `discount_rate`,`g`.`discount_start` AS `discount_start`,`g`.`discount_end` AS `discount_end`,`g`.`is_featured` AS `is_featured`,`g`.`is_on_sale` AS `is_on_sale`,`g`.`release_date` AS `release_date`,`g`.`developer` AS `developer`,`g`.`publisher` AS `publisher`,`g`.`rating` AS `rating`,`g`.`rating_count` AS `rating_count`,`g`.`total_sales` AS `total_sales`,`g`.`total_reviews` AS `total_reviews`,`g`.`download_count` AS `download_count`,`g`.`file_size` AS `file_size`,`g`.`version` AS `version`,`g`.`last_update` AS `last_update`,`g`.`created_at` AS `created_at`,`g`.`updated_at` AS `updated_at`,group_concat(distinct `gc`.`name` separator ',') AS `categories`,group_concat(distinct `gt`.`name` separator ',') AS `tags`,`gsr`.`cpu_min` AS `cpu_min`,`gsr`.`cpu_recommended` AS `cpu_recommended`,`gsr`.`ram_min` AS `ram_min`,`gsr`.`ram_recommended` AS `ram_recommended`,`gsr`.`gpu_min` AS `gpu_min`,`gsr`.`gpu_recommended` AS `gpu_recommended`,`gsr`.`storage_min` AS `storage_min` from (((((`games` `g` left join `game_category_mapping` `gcm` on((`g`.`id` = `gcm`.`game_id`))) left join `game_categories` `gc` on((`gcm`.`category_id` = `gc`.`id`))) left join `game_tag_mapping` `gtm` on((`g`.`id` = `gtm`.`game_id`))) left join `game_tags` `gt` on((`gtm`.`tag_id` = `gt`.`id`))) left join `game_system_requirements` `gsr` on((`g`.`id` = `gsr`.`game_id`))) group by `g`.`id`;

SET FOREIGN_KEY_CHECKS = 1;
