-- ============================================
-- 企微群消息推送系统 - 初始化脚本
-- ============================================

-- 1. 企微推送群配置表
CREATE TABLE IF NOT EXISTS `wechat_push_config` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `group_name` VARCHAR(200) NOT NULL COMMENT '群名称',
    `webhook_url` VARCHAR(500) NOT NULL COMMENT '企微机器人Webhook地址',
    `status` TINYINT DEFAULT 1 COMMENT '状态: 1=启用, 0=禁用',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY `uk_webhook_url` (`webhook_url`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企微推送-群配置表';

-- 2. 推送任务表
CREATE TABLE IF NOT EXISTS `wechat_push_task` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `config_id` BIGINT NOT NULL COMMENT '关联群配置ID',
    `push_content` TEXT NOT NULL COMMENT '推送消息内容',
    `push_mode` VARCHAR(20) NOT NULL DEFAULT 'IMMEDIATE' COMMENT '推送模式: IMMEDIATE=立即, SCHEDULED=定时',
    `cron_expression` VARCHAR(100) DEFAULT NULL COMMENT 'Cron表达式(定时必填)',
    `cron_desc` VARCHAR(200) DEFAULT NULL COMMENT '定时描述(如"每天10:00")',
    `enabled` TINYINT DEFAULT 1 COMMENT '是否启用: 1=启用, 0=停用',
    `last_push_time` DATETIME DEFAULT NULL COMMENT '上次推送时间',
    `last_push_status` VARCHAR(20) DEFAULT NULL COMMENT '上次推送结果: SUCCESS/FAIL',
    `last_push_msg` VARCHAR(500) DEFAULT NULL COMMENT '上次推送结果详情',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX `idx_config_id` (`config_id`),
    INDEX `idx_push_mode` (`push_mode`),
    INDEX `idx_enabled` (`enabled`),
    CONSTRAINT `fk_task_config` FOREIGN KEY (`config_id`) REFERENCES `wechat_push_config`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企微推送-任务表';

-- 3. 推送日志表（审计用）
CREATE TABLE IF NOT EXISTS `wechat_push_log` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
    `task_id` BIGINT DEFAULT NULL COMMENT '关联任务ID(NULL=手动立即发送)',
    `config_id` BIGINT DEFAULT NULL COMMENT '关联群配置ID',
    `group_name` VARCHAR(200) DEFAULT NULL COMMENT '群名称快照',
    `push_content` TEXT COMMENT '推送内容快照',
    `push_mode` VARCHAR(20) COMMENT '推送模式',
    `errcode` INT DEFAULT NULL COMMENT '企微返回errcode',
    `errmsg` VARCHAR(500) DEFAULT NULL COMMENT '企微返回errmsg/异常信息',
    `status` VARCHAR(20) NOT NULL COMMENT 'SUCCESS/FAIL',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企微推送-日志表';

-- 4. 注册菜单到 sys_menu（如果表存在）
INSERT IGNORE INTO `sys_menu` (`id`, `menu_name`, `menu_code`, `url`, `parent_id`, `sort_order`, `icon`)
VALUES (50, '企微群推送', 'wechat_push', '/wechat-push.html', 9, 2, '📨');

-- 5. 给 admin 角色分配菜单权限（sys_role_menu）
INSERT IGNORE INTO `sys_role_menu` (`role_id`, `menu_id`)
SELECT 1, 50 FROM DUAL
WHERE EXISTS (SELECT 1 FROM `sys_menu` WHERE `id` = 50)
AND EXISTS (SELECT 1 FROM `sys_role` WHERE `id` = 1);
