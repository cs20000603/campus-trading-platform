/*
 Navicat Premium Data Transfer

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80026 (8.0.26)
 Source Host           : localhost:3306
 Source Schema         : code2026

 Target Server Type    : MySQL
 Target Server Version : 80026 (8.0.26)
 File Encoding         : 65001

 Date: 02/07/2026 22:10:29
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for admin
-- ----------------------------
DROP TABLE IF EXISTS `admin`;
CREATE TABLE `admin`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '姓名',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'ADMIN' COMMENT '角色',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_admin_username`(`username` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '管理员' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of admin
-- ----------------------------
INSERT INTO `admin` VALUES (1, 'admin', 'admin', '系统管理员', NULL, 'ADMIN');

-- ----------------------------
-- Table structure for carousel
-- ----------------------------
DROP TABLE IF EXISTS `carousel`;
CREATE TABLE `carousel`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `goods_id` int NULL DEFAULT NULL COMMENT '关联商品ID',
  `img` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '轮播图片URL',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_carousel_goods`(`goods_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '轮播图' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of carousel
-- ----------------------------
INSERT INTO `carousel` VALUES (1, 22, 'http://127.0.0.1:9000/code2026/1083c70d-e30b-429e-8460-e050b1930587.webp');
INSERT INTO `carousel` VALUES (2, 21, 'http://127.0.0.1:9000/code2026/2585498e-3cec-4349-8208-5b004ebbd784.webp');
INSERT INTO `carousel` VALUES (3, 20, 'http://127.0.0.1:9000/code2026/165b87ee-b6d3-4fc8-bb4c-190f4461b9f8.png');
INSERT INTO `carousel` VALUES (4, 19, 'http://127.0.0.1:9000/code2026/9befdd48-3290-47a9-98f2-c0e1f1148faf.png');

-- ----------------------------
-- Table structure for cart
-- ----------------------------
DROP TABLE IF EXISTS `cart`;
CREATE TABLE `cart`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `goods_id` int NOT NULL COMMENT '商品ID',
  `num` int NOT NULL DEFAULT 1 COMMENT '数量',
  `user_id` int NOT NULL COMMENT '用户ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_cart_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_cart_goods`(`goods_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '购物车' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of cart
-- ----------------------------
INSERT INTO `cart` VALUES (1, 18, 1, 14);

-- ----------------------------
-- Table structure for category
-- ----------------------------
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '分类名称',
  `shop_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '店铺类型关联',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 13 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品分类' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of category
-- ----------------------------
INSERT INTO `category` VALUES (1, '咖啡', '饮品');
INSERT INTO `category` VALUES (2, '果茶', '饮品');
INSERT INTO `category` VALUES (3, '奶茶', '饮品');
INSERT INTO `category` VALUES (4, '面包', '烘焙');
INSERT INTO `category` VALUES (5, '甜品', '烘焙');
INSERT INTO `category` VALUES (6, '生日蛋糕', '烘焙');
INSERT INTO `category` VALUES (7, '配饰', '服饰');
INSERT INTO `category` VALUES (8, '女装', '服饰');
INSERT INTO `category` VALUES (9, '男装', '服饰');
INSERT INTO `category` VALUES (10, '数码', '数码');
INSERT INTO `category` VALUES (11, '书籍', '文具');
INSERT INTO `category` VALUES (12, '生活用品', '日用');

-- ----------------------------
-- Table structure for chat_message
-- ----------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int NOT NULL COMMENT '用户ID',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色: user/assistant',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_chat_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = 'AI聊天记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of chat_message
-- ----------------------------

-- ----------------------------
-- Table structure for collect
-- ----------------------------
DROP TABLE IF EXISTS `collect`;
CREATE TABLE `collect`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `goods_id` int NOT NULL COMMENT '商品ID',
  `user_id` int NOT NULL COMMENT '用户ID',
  `time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收藏时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_collect_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_collect_goods`(`goods_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '收藏' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of collect
-- ----------------------------

-- ----------------------------
-- Table structure for comment
-- ----------------------------
DROP TABLE IF EXISTS `comment`;
CREATE TABLE `comment`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `score` double NULL DEFAULT 5 COMMENT '评分',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '评价内容',
  `user_id` int NOT NULL COMMENT '用户ID',
  `time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '评价时间',
  `order_id` int NULL DEFAULT NULL COMMENT '订单ID',
  `goods_id` int NOT NULL COMMENT '商品ID',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_comment_goods`(`goods_id` ASC) USING BTREE,
  INDEX `idx_comment_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_comment_order`(`order_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品评价' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of comment
-- ----------------------------

-- ----------------------------
-- Table structure for goods
-- ----------------------------
DROP TABLE IF EXISTS `goods`;
CREATE TABLE `goods`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '商品名称',
  `img` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品图片URL',
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '价格',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '简要描述',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '详情(HTML)',
  `store` int NULL DEFAULT 0 COMMENT '库存数量',
  `category_id` int NULL DEFAULT NULL COMMENT '分类ID',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '上架' COMMENT '状态: 上架/下架',
  `views` int NULL DEFAULT 0 COMMENT '浏览量',
  `sale_count` int NULL DEFAULT 0 COMMENT '销量',
  `time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上架时间',
  `recommend` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推荐标记',
  `shop_id` int NULL DEFAULT NULL COMMENT '店铺ID',
  `discount_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '折扣价',
  `discount_end` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '折扣截止时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_goods_category`(`category_id` ASC) USING BTREE,
  INDEX `idx_goods_shop`(`shop_id` ASC) USING BTREE,
  INDEX `idx_goods_status`(`status` ASC) USING BTREE,
  INDEX `idx_goods_name`(`name` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 24 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '商品' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of goods
-- ----------------------------
INSERT INTO `goods` VALUES (1, '美式咖啡', 'http://127.0.0.1:9000/code2026/70d60b11-569a-4222-b114-06d6258e60c8.jpg', 12.00, '经典美式，现磨咖啡豆', NULL, 200, 1, '上架', 0, 0, '2026-06-26 15:24:40', NULL, 1, NULL, NULL);
INSERT INTO `goods` VALUES (2, '拿铁咖啡', 'http://127.0.0.1:9000/code2026/c952b780-a102-4ce2-abf5-80785acff908.jpg', 15.00, '浓郁奶泡搭配意式浓缩', NULL, 180, 1, '上架', 0, 0, '2026-06-26 15:24:40', NULL, 1, NULL, NULL);
INSERT INTO `goods` VALUES (3, '卡布奇诺', 'http://127.0.0.1:9000/code2026/070b056d-c23b-4be6-b224-48f3b4c45dd6.jpg', 16.00, '奶泡绵密，口感醇厚', NULL, 150, 1, '上架', 0, 0, '2026-06-26 15:24:40', NULL, 1, NULL, NULL);
INSERT INTO `goods` VALUES (4, '柠檬绿茶', 'http://127.0.0.1:9000/code2026/7eec23c3-ecd1-4a13-9500-560b82e52b47.jpg', 10.00, '清爽柠檬搭配绿茶', NULL, 300, 2, '上架', 0, 0, '2026-06-26 15:24:40', NULL, 1, NULL, NULL);
INSERT INTO `goods` VALUES (5, '满杯百香果', 'http://127.0.0.1:9000/code2026/e24730ce-4855-4ef6-b004-407bffc5e98e.jpg', 14.00, '新鲜百香果，酸甜可口', NULL, 250, 2, '上架', 0, 0, '2026-06-26 15:24:40', NULL, 1, NULL, NULL);
INSERT INTO `goods` VALUES (6, '蜜桃乌龙茶', 'http://127.0.0.1:9000/code2026/2e2a6673-df22-4733-adce-9ef4a441eca2.jpg', 13.00, '蜜桃果肉+乌龙茶底', NULL, 200, 2, '上架', 0, 0, '2026-06-26 15:24:40', NULL, 1, NULL, NULL);
INSERT INTO `goods` VALUES (7, '珍珠奶茶', 'http://127.0.0.1:9000/code2026/673a1cf8-7e64-4575-a1f6-7016f3f75045.jpg', 12.00, 'Q弹珍珠，经典味道', NULL, 350, 3, '上架', 0, 0, '2026-06-26 15:24:40', NULL, 1, NULL, NULL);
INSERT INTO `goods` VALUES (8, '椰果奶茶', 'http://127.0.0.1:9000/code2026/2f7cb2a9-c926-4c62-aad4-ffa3be2a24d5.jpg', 12.00, '椰果粒粒分明，清爽香甜', NULL, 280, 3, '上架', 0, 0, '2026-06-26 15:24:40', NULL, 1, NULL, NULL);
INSERT INTO `goods` VALUES (9, '全麦吐司', 'http://127.0.0.1:9000/code2026/bfeb7521-0b83-45a1-afbd-cd6a7323c941.jpg', 8.00, '健康全麦，早餐首选', NULL, 150, 4, '上架', 0, 0, '2026-06-26 15:24:40', NULL, 2, NULL, NULL);
INSERT INTO `goods` VALUES (10, '提拉米苏', 'http://127.0.0.1:9000/code2026/5f862b43-6e7a-4ba4-84eb-35dace6231a3.jpg', 18.00, '经典意式甜品，入口即化', NULL, 80, 5, '上架', 0, 0, '2026-06-26 15:24:40', NULL, 2, NULL, NULL);
INSERT INTO `goods` VALUES (11, '芒果慕斯', 'http://127.0.0.1:9000/code2026/4ea973f5-3290-4e33-b255-ccd1cf05feaf.jpg', 16.00, '新鲜芒果，轻盈慕斯', NULL, 90, 5, '上架', 0, 0, '2026-06-26 15:24:40', NULL, 2, NULL, NULL);
INSERT INTO `goods` VALUES (12, '草莓奶油蛋糕', 'http://127.0.0.1:9000/code2026/8b9ddf44-116c-4d20-99e2-be1b0b5e2ecb.jpg', 88.00, '6寸草莓奶油蛋糕，新鲜现做', NULL, 30, 6, '上架', 0, 0, '2026-06-26 15:24:40', NULL, 2, NULL, NULL);
INSERT INTO `goods` VALUES (13, '简约帆布包', 'http://127.0.0.1:9000/code2026/cf2f038a-1b61-46f3-acd6-64da4c7a5dd5.jpg', 29.00, '文艺简约帆布单肩包', NULL, 100, 7, '上架', 0, 0, '2026-06-26 15:24:40', NULL, NULL, NULL, NULL);
INSERT INTO `goods` VALUES (14, '学院风百褶裙', 'http://127.0.0.1:9000/code2026/616b2133-4035-41ce-8e84-c7d27dd61913.jpg', 59.00, '韩版高腰A字百褶裙', NULL, 60, 8, '上架', 0, 0, '2026-06-26 15:24:40', NULL, NULL, NULL, NULL);
INSERT INTO `goods` VALUES (15, '连帽卫衣', 'http://127.0.0.1:9000/code2026/4bfcb338-143f-457d-9e3e-51d7aad01cd7.jpg', 69.00, '宽松纯色加绒卫衣', NULL, 80, 8, '上架', 0, 0, '2026-06-26 15:24:40', NULL, NULL, NULL, NULL);
INSERT INTO `goods` VALUES (16, 'Type-C数据线', 'http://127.0.0.1:9000/code2026/ce1b57db-673e-4d0f-99b3-3f5bebac44e3.jpg', 12.00, '1米快充数据线，编织材质', NULL, 300, 10, '上架', 0, 0, '2026-06-26 15:24:40', NULL, NULL, NULL, NULL);
INSERT INTO `goods` VALUES (17, '无线蓝牙耳机', 'http://127.0.0.1:9000/code2026/5d8a80cb-befe-4952-a1c5-01f4fa65ae07.jpg', 79.00, '蓝牙5.3，续航8小时', NULL, 100, 10, '上架', 0, 0, '2026-06-26 15:24:40', NULL, NULL, NULL, NULL);
INSERT INTO `goods` VALUES (18, '手机支架', 'http://127.0.0.1:9000/code2026/6b6ecaf7-39d3-424a-8228-3b721910a3b3.webp', 9.90, '可折叠桌面手机支架', '<p><img src=\"http://127.0.0.1:9000/code2026/ba044c8a-4ed8-4f03-87a8-83b4c3b4d5ff.webp\" alt=\"\" data-href=\"\" style=\"\"/></p>', 198, 10, '上架', 0, 2, '2026-06-26 15:24:40', NULL, NULL, NULL, NULL);
INSERT INTO `goods` VALUES (19, '高等数学第七版', 'http://127.0.0.1:9000/code2026/9ca1b356-a07b-43ba-8eea-348487ebcfc0.png', 25.00, '同济大学数学系，九成新', '<p><img src=\"http://127.0.0.1:9000/code2026/d59fb8cc-da8c-450a-b727-3b5a6994d82d.png\" alt=\"\" data-href=\"\" style=\"\"/></p>', 50, 11, '上架', 0, 0, '2026-06-26 15:24:40', NULL, NULL, NULL, NULL);
INSERT INTO `goods` VALUES (20, '英语四级词汇', 'http://127.0.0.1:9000/code2026/08abd713-ff4b-4229-962d-aebbe0e29a8b.webp', 15.00, '星火英语四级词汇书', '<p><img src=\"http://127.0.0.1:9000/code2026/c2b77325-5cb9-4dc5-b80d-24d36c8bb20f.webp\" alt=\"\" data-href=\"\" style=\"\"/></p>', 120, 11, '上架', 0, 0, '2026-06-26 15:24:40', '是', NULL, NULL, NULL);
INSERT INTO `goods` VALUES (21, '保温杯', 'http://127.0.0.1:9000/code2026/145dbdaa-68bc-4207-8dbf-b64d35af0a5e.png', 35.00, '316不锈钢，500ml', '<p><img src=\"http://127.0.0.1:9000/code2026/6ff49804-f434-452b-8713-1e06e0e69333.webp\" alt=\"\" data-href=\"\" style=\"\"/></p>', 150, 12, '上架', 0, 0, '2026-06-26 15:24:40', '是', NULL, NULL, NULL);
INSERT INTO `goods` VALUES (22, '桌面收纳盒', 'http://127.0.0.1:9000/code2026/79045954-dec0-46b3-b5aa-a413785a5c76.webp', 19.90, '三层抽屉式桌面收纳', '<p><img src=\"http://127.0.0.1:9000/code2026/f5da96b8-f178-46e7-bfa8-5d8900a9acdc.webp\" alt=\"\" data-href=\"\" style=\"\"/></p>', 199, 12, '上架', 0, 1, '2026-06-26 15:24:40', '是', NULL, NULL, NULL);
INSERT INTO `goods` VALUES (23, '坚果', 'http://127.0.0.1:9000/code2026/ee8f9521-4d78-4b9f-bb49-4305b6402ee3.webp', 7.00, '【坚果甜品】一口酥脆，满口香浓！精选优质坚果，搭配细腻糖霜，甜而不腻，脆而不硬。无论是下午茶时光，还是追剧小零嘴，都是完美伴侣。健康、美味、解馋，一口就爱上！', NULL, 99, 5, '上架', 0, 1, '2026-06-30 11:44:41', NULL, 3, 5.00, '2026-07-02 00:00:00');

-- ----------------------------
-- Table structure for idle_goods
-- ----------------------------
DROP TABLE IF EXISTS `idle_goods`;
CREATE TABLE `idle_goods`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `images` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '图片URL(逗号分隔)',
  `price` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '售价',
  `original_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '原价',
  `condition` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '成色',
  `delivery_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配送方式',
  `campus_area` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '校区区域',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '闲置分类',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '在售' COMMENT '状态: 在售/已售出/已下架',
  `seller_id` int NOT NULL COMMENT '卖家ID',
  `seller_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '卖家昵称',
  `seller_avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '卖家头像',
  `shop_id` int NULL DEFAULT NULL COMMENT '关联店铺ID',
  `shop_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联店铺名称',
  `views` int NULL DEFAULT 0 COMMENT '浏览量',
  `create_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发布时间',
  `sold_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '售出时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_idle_goods_seller`(`seller_id` ASC) USING BTREE,
  INDEX `idx_idle_goods_status`(`status` ASC) USING BTREE,
  INDEX `idx_idle_goods_category`(`category` ASC) USING BTREE,
  INDEX `idx_idle_goods_title`(`title` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '闲置商品' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of idle_goods
-- ----------------------------

-- ----------------------------
-- Table structure for idle_message
-- ----------------------------
DROP TABLE IF EXISTS `idle_message`;
CREATE TABLE `idle_message`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `idle_id` int NOT NULL COMMENT '闲置商品ID',
  `sender_id` int NOT NULL COMMENT '发送者ID',
  `sender_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发送者昵称',
  `sender_avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发送者头像',
  `receiver_id` int NOT NULL COMMENT '接收者ID',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '消息内容',
  `is_read` tinyint(1) NULL DEFAULT 0 COMMENT '是否已读',
  `create_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '消息时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_im_idle`(`idle_id` ASC) USING BTREE,
  INDEX `idx_im_sender`(`sender_id` ASC) USING BTREE,
  INDEX `idx_im_receiver`(`receiver_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '闲置聊天消息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of idle_message
-- ----------------------------

-- ----------------------------
-- Table structure for idle_wanted
-- ----------------------------
DROP TABLE IF EXISTS `idle_wanted`;
CREATE TABLE `idle_wanted`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '求购标题',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '描述',
  `budget` decimal(10, 2) NULL DEFAULT NULL COMMENT '预算',
  `category` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类',
  `campus_area` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '校区区域',
  `user_id` int NOT NULL COMMENT '用户ID',
  `user_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户名',
  `user_avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '进行中' COMMENT '状态',
  `create_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '发布时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_iw_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_iw_category`(`category` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '求购信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of idle_wanted
-- ----------------------------

-- ----------------------------
-- Table structure for order_detail
-- ----------------------------
DROP TABLE IF EXISTS `order_detail`;
CREATE TABLE `order_detail`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `goods_id` int NOT NULL COMMENT '商品ID',
  `num` int NOT NULL DEFAULT 1 COMMENT '数量',
  `order_id` int NOT NULL COMMENT '订单ID',
  `goods_img` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品图片快照',
  `goods_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '商品名称快照',
  `goods_price` decimal(10, 2) NULL DEFAULT NULL COMMENT '商品价格快照',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_od_order`(`order_id` ASC) USING BTREE,
  INDEX `idx_od_goods`(`goods_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单明细' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of order_detail
-- ----------------------------
INSERT INTO `order_detail` VALUES (3, 22, 1, 5, 'http://127.0.0.1:9000/code2026/79045954-dec0-46b3-b5aa-a413785a5c76.webp', '桌面收纳盒', 19.90);
INSERT INTO `order_detail` VALUES (4, 18, 1, 6, 'http://127.0.0.1:9000/code2026/6b6ecaf7-39d3-424a-8228-3b721910a3b3.webp', '手机支架', 9.90);
INSERT INTO `order_detail` VALUES (5, 23, 1, 7, 'http://127.0.0.1:9000/code2026/ee8f9521-4d78-4b9f-bb49-4305b6402ee3.webp', '坚果', 7.00);
INSERT INTO `order_detail` VALUES (7, 18, 1, 9, 'http://127.0.0.1:9000/code2026/6b6ecaf7-39d3-424a-8228-3b721910a3b3.webp', '手机支架', 9.90);

-- ----------------------------
-- Table structure for orders
-- ----------------------------
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `order_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
  `total` decimal(10, 2) NOT NULL DEFAULT 0.00 COMMENT '总金额',
  `user_id` int NOT NULL COMMENT '用户ID',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '待付款' COMMENT '状态: 待付款/待发货/已出货/已配送/待接单/已完成/已取消',
  `time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '下单时间',
  `deliver_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配送方式',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '收货地址',
  `deliver` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配送信息',
  `shop_id` int NULL DEFAULT NULL COMMENT '店铺ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_order_no`(`order_no` ASC) USING BTREE,
  INDEX `idx_orders_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_orders_shop`(`shop_id` ASC) USING BTREE,
  INDEX `idx_orders_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 10 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '订单' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of orders
-- ----------------------------
INSERT INTO `orders` VALUES (5, '2026062617824635930110413', 19.90, 12, '待接单', '2026-06-26 16:46:33', '自提', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (6, '2026063017827912179693895', 9.90, 12, '待接单', '2026-06-30 11:46:57', '自提', NULL, NULL, NULL);
INSERT INTO `orders` VALUES (7, '2026063017827913577518581', 7.00, 12, '已完成', '2026-06-30 11:49:17', '自提', NULL, NULL, 3);
INSERT INTO `orders` VALUES (9, '2026063017827916729590364', 9.90, 14, '已出货', '2026-06-30 11:54:32', '外送', '民大112栋', '', NULL);

-- ----------------------------
-- Table structure for recharge
-- ----------------------------
DROP TABLE IF EXISTS `recharge`;
CREATE TABLE `recharge`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `money` decimal(10, 2) NOT NULL COMMENT '充值金额',
  `user_id` int NOT NULL COMMENT '用户ID',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '支付方式',
  `time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '充值时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_recharge_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '充值记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recharge
-- ----------------------------
INSERT INTO `recharge` VALUES (1, 1000.00, 12, '微信支付', '2026-06-26 16:39:38');
INSERT INTO `recharge` VALUES (2, 500.00, 14, '微信支付', '2026-06-30 11:54:13');

-- ----------------------------
-- Table structure for recommendation
-- ----------------------------
DROP TABLE IF EXISTS `recommendation`;
CREATE TABLE `recommendation`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int NOT NULL COMMENT '用户ID',
  `goods_id` int NOT NULL COMMENT '商品ID',
  `reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推荐理由',
  `score` double NULL DEFAULT 0 COMMENT '推荐分数',
  `time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '推荐时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_rec_user`(`user_id` ASC) USING BTREE,
  INDEX `idx_rec_goods`(`goods_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '推荐记录' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of recommendation
-- ----------------------------

-- ----------------------------
-- Table structure for search_log
-- ----------------------------
DROP TABLE IF EXISTS `search_log`;
CREATE TABLE `search_log`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `user_id` int NULL DEFAULT NULL COMMENT '用户ID',
  `keyword` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '搜索关键词',
  `time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '搜索时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_search_user`(`user_id` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '搜索日志' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of search_log
-- ----------------------------

-- ----------------------------
-- Table structure for shop
-- ----------------------------
DROP TABLE IF EXISTS `shop`;
CREATE TABLE `shop`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '店铺名称',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '店铺描述',
  `logo` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '店铺Logo URL',
  `owner_id` int NOT NULL COMMENT '店主用户ID',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '审核中' COMMENT '状态: 审核中/审核通过/审核拒绝',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '联系电话',
  `address` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '地址',
  `create_time` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '创建时间',
  `type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '店铺类型',
  `license` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '营业执照URL',
  `reject_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '驳回原因',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_shop_owner`(`owner_id` ASC) USING BTREE,
  INDEX `idx_shop_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '店铺' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of shop
-- ----------------------------
INSERT INTO `shop` VALUES (1, '校园咖啡屋', '现磨咖啡，新鲜直达', NULL, 4, '审核通过', '13800000003', '一食堂旁', '2025-09-01', '饮品', NULL, NULL);
INSERT INTO `shop` VALUES (2, '甜蜜烘焙坊', '手工面包、蛋糕、甜品', NULL, 5, '审核通过', '13800000004', '学生活动中心', '2025-09-05', '烘焙', NULL, NULL);
INSERT INTO `shop` VALUES (3, '甜心甜品', '🍰这家蛋糕店，每一口都是幸福的味道！从经典奶油蛋糕到创意慕斯，甄选优质原料，纯手工制作。无论是生日庆祝还是甜蜜下午茶，这里都有你的专属甜蜜~', NULL, 12, '营业中', '1234567890', '民大二食堂一楼', '2026-06-26 16:39:07', '烘焙', NULL, NULL);
INSERT INTO `shop` VALUES (5, '水果很忙', '【水果很忙，新鲜直达！】清晨采摘，当天到店，果香四溢的甜蜜盛宴！从脆甜苹果到爆汁橙子，颗颗饱满，口口新鲜。每天为您挑选时令鲜果，让忙碌的生活多一份自然的甜。', 'http://127.0.0.1:9000/code2026/95311a90-45c8-47d5-ad4b-fd2aa2892033.jpg', 15, '线上审核中', '15286333459', '一食堂一楼', '2026-06-30 12:37:59', '水果店', 'http://127.0.0.1:9000/code2026/abc84603-bf25-414c-aa28-96b6d62788f1.jpg', NULL);

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '昵称',
  `avatar` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '头像URL',
  `role` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT '普通用户' COMMENT '角色: 普通用户/merchant',
  `account` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '账户余额',
  `phone` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号',
  `openid` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '微信openid',
  `token` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT 'JWT登录令牌',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_username`(`username` ASC) USING BTREE,
  INDEX `idx_user_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_user_openid`(`openid` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of user
-- ----------------------------
INSERT INTO `user` VALUES (1, 'test', '123456', '测试用户', NULL, '普通用户', 1000.00, '13800138000', NULL, NULL);
INSERT INTO `user` VALUES (2, 'buyer1', '123456', '小明', NULL, '普通用户', 0.00, '13800000001', NULL, NULL);
INSERT INTO `user` VALUES (3, 'buyer2', '123456', '小红', NULL, '普通用户', 0.00, '13800000002', NULL, NULL);
INSERT INTO `user` VALUES (4, 'seller1', '123456', '咖啡店主', NULL, '普通用户', 0.00, '13800000003', NULL, NULL);
INSERT INTO `user` VALUES (5, 'seller2', '123456', '烘焙达人', NULL, '普通用户', 0.00, '13800000004', NULL, NULL);
INSERT INTO `user` VALUES (6, '13999990001', '123456', '用户0001', '', '普通用户', 0.00, '13999990001', NULL, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjYsImlhdCI6MTc4MjQ2MTQxNCwiZXhwIjoxNzgzMDY2MjE0fQ.-mG8lK7AhWfmXETiWbNRU2BhhKSDHAruHhTlzSiYJxA');
INSERT INTO `user` VALUES (7, 'testuser666', '123456', 'testuser666', NULL, '普通用户', 0.00, NULL, NULL, NULL);
INSERT INTO `user` VALUES (8, 'testuser555', '123456', 'testuser555', NULL, '普通用户', 0.00, NULL, NULL, NULL);
INSERT INTO `user` VALUES (9, 'testuser333', '123456', 'testuser333', NULL, '普通用户', 0.00, NULL, NULL, NULL);
INSERT INTO `user` VALUES (10, 'testuser999', '123456', 'testuser999', NULL, '普通用户', 0.00, NULL, NULL, NULL);
INSERT INTO `user` VALUES (12, '123456', '123456', '123456', 'http://127.0.0.1:9000/code2026/d4d7c769-d955-46f5-92a7-e6338b28a55f.png', '商家', 963.20, NULL, NULL, NULL);
INSERT INTO `user` VALUES (13, '15284616494', '111111', '15284616494', NULL, '普通用户', 0.00, NULL, NULL, NULL);
INSERT INTO `user` VALUES (14, '15355212256', '123456', '用户2256', 'http://127.0.0.1:9000/code2026/8f514658-124e-4bdf-a5ad-53d3fb791533.webp', '普通用户', 490.10, '15355212256', NULL, 'eyJhbGciOiJIUzI1NiJ9.eyJ1c2VySWQiOjE0LCJpYXQiOjE3ODI3OTE1NjksImV4cCI6MTc4MzM5NjM2OX0.L02BuyTSiwQ5BSBIQwpGieYOKcGxKmSbPWIvMVa2LzQ');
INSERT INTO `user` VALUES (15, '654321', '654321', '654321', NULL, '商家', 0.00, NULL, NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
