package com.crypto.mapper;

import com.crypto.entity.SysRole;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class SysRoleMapper {

    private final JdbcTemplate jdbcTemplate;

    public SysRoleMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SysRole> rowMapper = new RowMapper<SysRole>() {
        @Override
        public SysRole mapRow(ResultSet rs, int rowNum) throws SQLException {
            SysRole role = new SysRole();
            role.setId(rs.getLong("id"));
            role.setRoleName(rs.getString("role_name"));
            role.setRoleCode(rs.getString("role_code"));
            role.setDescription(rs.getString("description"));
            role.setStatus(rs.getInt("status"));
            Timestamp ca = rs.getTimestamp("created_at");
            role.setCreatedAt(ca != null ? ca.toLocalDateTime() : null);
            Timestamp ua = rs.getTimestamp("updated_at");
            role.setUpdatedAt(ua != null ? ua.toLocalDateTime() : null);
            return role;
        }
    };

    public List<SysRole> findAll() {
        return jdbcTemplate.query("SELECT * FROM sys_role ORDER BY id", rowMapper);
    }

    public SysRole findById(Long id) {
        List<SysRole> list = jdbcTemplate.query(
            "SELECT * FROM sys_role WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(SysRole role) {
        return jdbcTemplate.update(
            "INSERT INTO sys_role (role_name, role_code, description) VALUES (?, ?, ?)",
            role.getRoleName(), role.getRoleCode(), role.getDescription());
    }

    public int update(SysRole role) {
        return jdbcTemplate.update(
            "UPDATE sys_role SET role_name=?, role_code=?, description=?, status=? WHERE id=?",
            role.getRoleName(), role.getRoleCode(), role.getDescription(),
            role.getStatus(), role.getId());
    }

    public int delete(Long id) {
        // 同时删除关联的菜单
        jdbcTemplate.update("DELETE FROM sys_role_menu WHERE role_id=?", id);
        return jdbcTemplate.update("DELETE FROM sys_role WHERE id=?", id);
    }
}
