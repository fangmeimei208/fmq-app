package com.crypto.mapper;

import com.crypto.entity.SysUser;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class SysUserMapper {

    private final JdbcTemplate jdbcTemplate;

    public SysUserMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<SysUser> rowMapper = new RowMapper<SysUser>() {
        @Override
        public SysUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            SysUser user = new SysUser();
            user.setId(rs.getLong("id"));
            user.setUsername(rs.getString("username"));
            user.setPassword(rs.getString("password"));
            user.setRealName(rs.getString("real_name"));
            user.setRoleId(rs.getObject("role_id", Long.class));
            user.setIsAdmin(rs.getObject("is_admin", Boolean.class));
            user.setStatus(rs.getInt("status"));
            Timestamp llt = rs.getTimestamp("last_login_time");
            user.setLastLoginTime(llt != null ? llt.toLocalDateTime() : null);
            user.setLastLoginIp(rs.getString("last_login_ip"));
            Timestamp ca = rs.getTimestamp("created_at");
            user.setCreatedAt(ca != null ? ca.toLocalDateTime() : null);
            Timestamp ua = rs.getTimestamp("updated_at");
            user.setUpdatedAt(ua != null ? ua.toLocalDateTime() : null);
            return user;
        }
    };

    public SysUser findByUsername(String username) {
        List<SysUser> list = jdbcTemplate.query(
            "SELECT * FROM sys_user WHERE username = ?", rowMapper, username);
        return list.isEmpty() ? null : list.get(0);
    }

    public SysUser findById(Long id) {
        List<SysUser> list = jdbcTemplate.query(
            "SELECT * FROM sys_user WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<SysUser> findAll() {
        return jdbcTemplate.query("SELECT * FROM sys_user ORDER BY id", rowMapper);
    }

    public int insert(SysUser user) {
        return jdbcTemplate.update(
            "INSERT INTO sys_user (username, password, real_name, role_id, is_admin, status) VALUES (?, ?, ?, ?, ?, ?)",
            user.getUsername(), user.getPassword(), user.getRealName(),
            user.getRoleId(), user.getIsAdmin() != null ? user.getIsAdmin() : false,
            user.getStatus() != null ? user.getStatus() : 1);
    }

    public int update(SysUser user) {
        return jdbcTemplate.update(
            "UPDATE sys_user SET username=?, real_name=?, role_id=?, is_admin=?, status=? WHERE id=?",
            user.getUsername(), user.getRealName(), user.getRoleId(),
            user.getIsAdmin(), user.getStatus(), user.getId());
    }

    public int updatePassword(Long id, String password) {
        return jdbcTemplate.update("UPDATE sys_user SET password=? WHERE id=?", password, id);
    }

    public int updateLoginInfo(Long id, String ip) {
        return jdbcTemplate.update(
            "UPDATE sys_user SET last_login_time=NOW(), last_login_ip=? WHERE id=?", ip, id);
    }

    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM sys_user WHERE id=?", id);
    }
}
