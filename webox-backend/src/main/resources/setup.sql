use webox_db;
-- ========== 1. 用户表 ==========
-- ========== 1. 用户表 ==========
DROP TABLE IF EXISTS `t_user`;
CREATE TABLE `t_user` (
                          `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                          `email` VARCHAR(255) NOT NULL COMMENT '登录邮箱',
                          `password` VARCHAR(255) NOT NULL COMMENT '密码（加密存储）',
                          `name` VARCHAR(100) COMMENT '用户姓名',
                          `categories` JSON COMMENT '偏好分类（多选, 如 ["chinese","japanese"]）',
                          `allergens` JSON COMMENT '过敏原标签（如 ["peanut","dairy"]）',
                          `flavor_spiciness_level` INT DEFAULT 0 COMMENT '辣度（高90, 中50, 低10）',
                          `flavor_taste_level` INT DEFAULT 0 COMMENT '口味（重口90, 适中50, 清淡10）',
                          `flavor_protein_level` INT DEFAULT 0 COMMENT '蛋白含量（高90, 中50, 低10）',
                          `flavor_fat_level` INT DEFAULT 0 COMMENT '脂肪含量（高90, 中50, 低10）',
                          `is_deleted` INT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                          `created_by` VARCHAR(64) COMMENT '创建人',
                          `updated_by` VARCHAR(64) COMMENT '更新人',
                          `created_at` DATETIME COMMENT '创建时间',
                          `updated_at` DATETIME COMMENT '更新时间',
                          PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- ========== 2. 菜品表 ==========
DROP TABLE IF EXISTS `t_menu_item`;
CREATE TABLE `t_menu_item` (
                               `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                               `name` VARCHAR(200) NOT NULL COMMENT '菜品名称',
                               `description` TEXT COMMENT '菜品描述',
                               `image` VARCHAR(500) COMMENT '图片URL',
                               `category` VARCHAR(50) COMMENT '所属分类（如 chinese、japanese）',
                               `allergens` JSON COMMENT '过敏原标签（如 ["peanut","dairy"]）',
                               `flavor_spiciness_level` INT DEFAULT 0 COMMENT '辣度（高90, 中50, 低10）',
                               `flavor_taste_level` INT DEFAULT 0 COMMENT '口味（重口90, 适中50, 清淡10）',
                               `flavor_protein_level` INT DEFAULT 0 COMMENT '蛋白含量（高90, 中50, 低10）',
                               `flavor_fat_level` INT DEFAULT 0 COMMENT '脂肪含量（高90, 中50, 低10）',
                               `is_deleted` INT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                               `created_by` VARCHAR(64) COMMENT '创建人',
                               `updated_by` VARCHAR(64) COMMENT '更新人',
                               `created_at` DATETIME COMMENT '创建时间',
                               `updated_at` DATETIME COMMENT '更新时间',
                               PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品表';

-- ========== 3. 每日菜单表 ==========
DROP TABLE IF EXISTS `t_daily_menu`;
CREATE TABLE `t_daily_menu` (
                                `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `today` DATE NOT NULL COMMENT '菜单对应的当前日期',
                                `menu_item_id` BIGINT NOT NULL COMMENT '菜品ID',
                                `price` INT COMMENT '单价',
                                `is_deleted` INT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                                `created_by` VARCHAR(64) COMMENT '创建人',
                                `updated_by` VARCHAR(64) COMMENT '更新人',
                                `created_at` DATETIME COMMENT '创建时间',
                                `updated_at` DATETIME COMMENT '更新时间',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='每日菜单表';

-- ========== 4. 订单表 ==========
DROP TABLE IF EXISTS `t_order`;
CREATE TABLE `t_order` (
                           `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                           `user_id` BIGINT NOT NULL COMMENT '下单用户ID',
                           `email` VARCHAR(255) COMMENT '登录邮箱',
                           `name` VARCHAR(100) COMMENT '用户姓名',
                           `total_amount` INT COMMENT '订单总额',
                           `delivery_date` DATE COMMENT '配送日期',
                           `meal_period` VARCHAR(20) COMMENT 'lunch | dinner',
                           `delivery_address` VARCHAR(500) COMMENT '配送地址',
                           `status` VARCHAR(20) DEFAULT 'pending' COMMENT 'pending | confirmed | completed | cancelled',
                           `is_deleted` INT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                           `created_by` VARCHAR(64) COMMENT '创建人',
                           `updated_by` VARCHAR(64) COMMENT '更新人',
                           `created_at` DATETIME COMMENT '创建时间',
                           `updated_at` DATETIME COMMENT '更新时间',
                           PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

-- ========== 5. 订单项表 ==========
DROP TABLE IF EXISTS `t_order_item`;
CREATE TABLE `t_order_item` (
                                `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                `order_id` BIGINT NOT NULL COMMENT '订单ID',
                                `user_id` BIGINT NOT NULL COMMENT '下单用户ID',
                                `menu_item_id` BIGINT NOT NULL COMMENT '菜品ID',
                                `name` VARCHAR(200) COMMENT '菜品名称（冗余快照）',
                                `price` INT COMMENT '下单时单价（快照）',
                                `quantity` INT COMMENT '数量',
                                `is_deleted` INT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                                `created_by` VARCHAR(64) COMMENT '创建人',
                                `updated_by` VARCHAR(64) COMMENT '更新人',
                                `created_at` DATETIME COMMENT '创建时间',
                                `updated_at` DATETIME COMMENT '更新时间',
                                PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单项表';

-- ========== 6. 菜品价格历史表 ==========
DROP TABLE IF EXISTS `t_menu_item_history`;
CREATE TABLE `t_menu_item_history` (
                                       `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                                       `menu_item_id` BIGINT NOT NULL COMMENT '菜品ID',
                                       `price` INT NOT NULL COMMENT '单价',
                                       `valid_from_date` DATE NOT NULL COMMENT '生效起始日期',
                                       `is_deleted` INT DEFAULT 0 COMMENT '逻辑删除 0-未删除 1-已删除',
                                       `created_by` VARCHAR(64) COMMENT '创建人',
                                       `updated_by` VARCHAR(64) COMMENT '更新人',
                                       `created_at` DATETIME COMMENT '创建时间',
                                       `updated_at` DATETIME COMMENT '更新时间',
                                       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='菜品价格历史表';

