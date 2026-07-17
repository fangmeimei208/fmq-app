package com.crypto.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * 一次性数据库迁移：菜单分级改造
 * 执行完成后可删除此文件或注释掉 @Component
 */
// @Component  // 迁移已完成，注释掉避免重复执行
public class DbMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    public DbMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        try {
            jdbcTemplate.update("INSERT IGNORE INTO sys_menu (id, menu_name, menu_code, url, icon, parent_id, sort_order) VALUES (9, '公共功能', 'public_func', NULL, '📦', 0, 1)");
            jdbcTemplate.update("INSERT IGNORE INTO sys_menu (id, menu_name, menu_code, url, icon, parent_id, sort_order) VALUES (10, '项目专属', 'project_func', NULL, '📁', 0, 2)");
            jdbcTemplate.update("INSERT IGNORE INTO sys_menu (id, menu_name, menu_code, url, icon, parent_id, sort_order) VALUES (11, '系统管理', 'system_mgmt', NULL, '⚙️', 0, 3)");
            jdbcTemplate.update("UPDATE sys_menu SET parent_id=0, sort_order=0 WHERE menu_code='home'");
            jdbcTemplate.update("UPDATE sys_menu SET parent_id=9, sort_order=1 WHERE menu_code='express_token'");
            jdbcTemplate.update("UPDATE sys_menu SET parent_id=10, sort_order=1 WHERE menu_code='sinotrans_aes'");
            jdbcTemplate.update("UPDATE sys_menu SET parent_id=10, sort_order=2 WHERE menu_code='pg_as2'");
            jdbcTemplate.update("UPDATE sys_menu SET parent_id=10, sort_order=3 WHERE menu_code='jlzy_socket'");
            jdbcTemplate.update("UPDATE sys_menu SET parent_id=11, sort_order=1 WHERE menu_code='user_mgmt'");
            jdbcTemplate.update("UPDATE sys_menu SET parent_id=11, sort_order=2 WHERE menu_code='role_mgmt'");
            jdbcTemplate.update("UPDATE sys_menu SET parent_id=11, sort_order=3 WHERE menu_code='login_log'");
            jdbcTemplate.update("INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 9)");
            jdbcTemplate.update("INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 10)");
            jdbcTemplate.update("INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (1, 11)");
            jdbcTemplate.update("INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 1)");
            jdbcTemplate.update("INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (2, 9)");
            System.out.println("[DbMigration] ALL DONE");
        } catch (Exception e) {
            System.err.println("[DbMigration] Error: " + e.getMessage());
        }
    }
}
