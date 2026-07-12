package com.crypto.mapper;

import com.crypto.entity.SysMenu;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class SysMenuMapper {

    private final JdbcTemplate jdbcTemplate;

    public SysMenuMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SysMenu> rowMapper = new RowMapper<SysMenu>() {
        @Override
        public SysMenu mapRow(ResultSet rs, int rowNum) throws SQLException {
            SysMenu menu = new SysMenu();
            menu.setId(rs.getLong("id"));
            menu.setMenuName(rs.getString("menu_name"));
            menu.setMenuCode(rs.getString("menu_code"));
            menu.setUrl(rs.getString("url"));
            menu.setIcon(rs.getString("icon"));
            menu.setParentId(rs.getLong("parent_id"));
            menu.setSortOrder(rs.getInt("sort_order"));
            menu.setStatus(rs.getInt("status"));
            Timestamp ca = rs.getTimestamp("created_at");
            menu.setCreatedAt(ca != null ? ca.toLocalDateTime() : null);
            return menu;
        }
    };

    public List<SysMenu> findAll() {
        return jdbcTemplate.query(
            "SELECT * FROM sys_menu WHERE status=1 ORDER BY sort_order", rowMapper);
    }

    public SysMenu findById(Long id) {
        List<SysMenu> list = jdbcTemplate.query(
            "SELECT * FROM sys_menu WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<SysMenu> findByRoleId(Long roleId) {
        return jdbcTemplate.query(
            "SELECT m.* FROM sys_menu m INNER JOIN sys_role_menu rm ON m.id = rm.menu_id " +
            "WHERE rm.role_id = ? AND m.status = 1 ORDER BY m.sort_order", rowMapper, roleId);
    }

    public int insert(SysMenu menu) {
        return jdbcTemplate.update(
            "INSERT INTO sys_menu (menu_name, menu_code, url, icon, parent_id, sort_order) VALUES (?, ?, ?, ?, ?, ?)",
            menu.getMenuName(), menu.getMenuCode(), menu.getUrl(), menu.getIcon(),
            menu.getParentId(), menu.getSortOrder());
    }

    public int update(SysMenu menu) {
        return jdbcTemplate.update(
            "UPDATE sys_menu SET menu_name=?, menu_code=?, url=?, icon=?, parent_id=?, sort_order=?, status=? WHERE id=?",
            menu.getMenuName(), menu.getMenuCode(), menu.getUrl(), menu.getIcon(),
            menu.getParentId(), menu.getSortOrder(), menu.getStatus(), menu.getId());
    }

    public int delete(Long id) {
        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE menu_id=?", id);
        return jdbcTemplate.update("DELETE FROM sys_menu WHERE id=?", id);
    }

    // Role-Menu 关联操作
    public void deleteRoleMenus(Long roleId) {
        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE role_id=?", roleId);
    }

    public void insertRoleMenu(Long roleId, Long menuId) {
        jdbcTemplate.update(
            "INSERT IGNORE INTO sys_role_menu (role_id, menu_id) VALUES (?, ?)", roleId, menuId);
    }

    public List<Long> findMenuIdsByRoleId(Long roleId) {
        return jdbcTemplate.queryForList(
            "SELECT menu_id FROM sys_role_menu WHERE role_id=?", Long.class, roleId);
    }
}
