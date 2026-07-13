-- ============================================
-- 认证授权系统初始化脚本
-- ============================================

-- 1. 用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `username` VARCHAR(50) NOT NULL UNIQUE,
    `password` VARCHAR(255) NOT NULL,
    `real_name` VARCHAR(50) DEFAULT NULL,
    `role_id` BIGINT DEFAULT NULL,
    `is_admin` TINYINT(1) DEFAULT 0 COMMENT '是否管理员',
    `status` TINYINT(1) DEFAULT 1 COMMENT '1=启用 0=禁用',
    `last_login_time` DATETIME DEFAULT NULL,
    `last_login_ip` VARCHAR(50) DEFAULT NULL,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_username` (`username`),
    INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 2. 角色表
CREATE TABLE IF NOT EXISTS `sys_role` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_name` VARCHAR(50) NOT NULL UNIQUE,
    `role_code` VARCHAR(50) NOT NULL UNIQUE,
    `description` VARCHAR(200) DEFAULT NULL,
    `status` TINYINT(1) DEFAULT 1 COMMENT '1=启用 0=禁用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统角色表';

-- 3. 菜单/权限表（parent_id=0为一级菜单，非0为二级菜单）
CREATE TABLE IF NOT EXISTS `sys_menu` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `menu_name` VARCHAR(50) NOT NULL,
    `menu_code` VARCHAR(50) NOT NULL UNIQUE,
    `url` VARCHAR(200) DEFAULT NULL,
    `icon` VARCHAR(50) DEFAULT NULL,
    `parent_id` BIGINT DEFAULT 0,
    `sort_order` INT DEFAULT 0,
    `status` TINYINT(1) DEFAULT 1 COMMENT '1=启用 0=禁用',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统菜单表';

-- 4. 角色-菜单关联表
CREATE TABLE IF NOT EXISTS `sys_role_menu` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `role_id` BIGINT NOT NULL,
    `menu_id` BIGINT NOT NULL,
    UNIQUE KEY `uk_role_menu` (`role_id`, `menu_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色菜单关联表';

-- ============================================
-- 预设数据
-- ============================================

-- 预设角色（使用 IGNORE 避免重复插入）
INSERT IGNORE INTO `sys_role` (`id`, `role_name`, `role_code`, `description`) VALUES
(1, '超级管理员', 'ROLE_SUPER_ADMIN', '拥有所有权限'),
(2, '普通用户', 'ROLE_USER', '基础权限');

-- 预设菜单（两级结构）
-- 一级菜单（分类）
INSERT IGNORE INTO `sys_menu` (`id`, `menu_name`, `menu_code`, `url`, `icon`, `parent_id`, `sort_order`) VALUES
(1, '系统首页', 'home', NULL, '🏠', 0, 0),
(9, '公共功能', 'public_func', NULL, '📦', 0, 1),
(10, '项目专属', 'project_func', NULL, '📁', 0, 2),
(11, '系统管理', 'system_mgmt', NULL, '⚙️', 0, 3);

-- 二级菜单（功能页面）
INSERT IGNORE INTO `sys_menu` (`id`, `menu_name`, `menu_code`, `url`, `icon`, `parent_id`, `sort_order`) VALUES
(3, '快递token有效期登记查询', 'express_token', '/express/expressTokenRegisterAndList.html', '📌', 9, 1),
(2, '中外运新国脉AES加解密', 'sinotrans_aes', '/sinotrans/AES.html', '📌', 10, 1),
(4, '宝洁分销AS2文件接口压测', 'pg_as2', '/PG/PGFXAS2StressTest.html', '📌', 10, 2),
(5, '玖龙地磅MOXA调试', 'jlzy_socket', '/JLZY/sendSocket.html', '📌', 10, 3),
(6, '用户管理', 'user_mgmt', '/user-mgmt.html', '👥', 11, 1),
(7, '角色权限管理', 'role_mgmt', '/role-mgmt.html', '🔐', 11, 2),
(8, '登录日志', 'login_log', '/login-log.html', '📋', 11, 3);

-- 管理员角色拥有全部菜单
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(1, 1), (1, 9), (1, 10), (1, 11),
(1, 2), (1, 3), (1, 4), (1, 5), (1, 6), (1, 7), (1, 8);

-- 普通用户默认只有首页、公共功能（含快递token）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`) VALUES
(2, 1), (2, 9), (2, 3);

-- 预设管理员账号 (密码: admin123, BCrypt加密)
-- BCrypt hash for "admin123"
INSERT IGNORE INTO `sys_user` (`id`, `username`, `password`, `real_name`, `role_id`, `is_admin`, `status`) VALUES
(1, 'admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '系统管理员', 1, 1, 1);
