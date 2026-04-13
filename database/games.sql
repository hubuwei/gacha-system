/*
 Navicat Premium Dump SQL

 Source Server         : 本地
 Source Server Type    : MySQL
 Source Server Version : 80042 (8.0.42)
 Source Host           : localhost:3306
 Source Schema         : gacha_system_prod

 Target Server Type    : MySQL
 Target Server Version : 80042 (8.0.42)
 File Encoding         : 65001

 Date: 11/04/2026 23:18:39
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

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
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '游戏表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of games
-- ----------------------------

SET FOREIGN_KEY_CHECKS = 1;
