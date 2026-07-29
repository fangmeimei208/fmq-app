package com.crypto.mapper;

import com.crypto.entity.WechatPushConfig;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class WechatPushConfigMapper {

    private final JdbcTemplate jdbcTemplate;

    public WechatPushConfigMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<WechatPushConfig> rowMapper = new RowMapper<WechatPushConfig>() {
        @Override
        public WechatPushConfig mapRow(ResultSet rs, int rowNum) throws SQLException {
            WechatPushConfig c = mapBase(rs);
            return c;
        }
    };

    private WechatPushConfig mapBase(ResultSet rs) throws SQLException {
        WechatPushConfig c = new WechatPushConfig();
        c.setId(rs.getLong("id"));
        c.setGroupName(rs.getString("group_name"));
        c.setWebhookUrl(rs.getString("webhook_url"));
        c.setStatus(rs.getInt("status"));
        c.setRemark(rs.getString("remark"));
        try { long cb = rs.getLong("created_by"); if (!rs.wasNull()) c.setCreatedBy(cb); } catch (Exception e) { }
        try { c.setCreatorName(rs.getString("creator_name")); } catch (Exception e) { }
        Timestamp ca = rs.getTimestamp("created_at");
        c.setCreatedAt(ca != null ? ca.toLocalDateTime() : null);
        Timestamp ua = rs.getTimestamp("updated_at");
        c.setUpdatedAt(ua != null ? ua.toLocalDateTime() : null);
        return c;
    }

    private final RowMapper<WechatPushConfig> rowMapperWithJoin = (rs, rowNum) -> mapBase(rs);

    public List<WechatPushConfig> findAll() {
        return jdbcTemplate.query(
            "SELECT c.*, u.real_name as creator_name FROM wechat_push_config c LEFT JOIN sys_user u ON c.created_by = u.id ORDER BY c.id",
            rowMapperWithJoin);
    }

    public List<WechatPushConfig> findByCreator(Long userId) {
        return jdbcTemplate.query(
            "SELECT c.*, u.real_name as creator_name FROM wechat_push_config c LEFT JOIN sys_user u ON c.created_by = u.id WHERE c.created_by = ? ORDER BY c.id",
            rowMapperWithJoin, userId);
    }

    public List<WechatPushConfig> findActive() {
        return jdbcTemplate.query("SELECT c.*, u.real_name as creator_name FROM wechat_push_config c LEFT JOIN sys_user u ON c.created_by = u.id WHERE c.status = 1 ORDER BY c.id", rowMapperWithJoin);
    }

    public WechatPushConfig findById(Long id) {
        List<WechatPushConfig> list = jdbcTemplate.query(
            "SELECT c.*, u.real_name as creator_name FROM wechat_push_config c LEFT JOIN sys_user u ON c.created_by = u.id WHERE c.id = ?", rowMapperWithJoin, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(WechatPushConfig c) {
        return jdbcTemplate.update(
            "INSERT INTO wechat_push_config (group_name, webhook_url, status, remark, created_by) VALUES (?,?,?,?,?)",
            c.getGroupName(), c.getWebhookUrl(), c.getStatus() != null ? c.getStatus() : 1, c.getRemark(), c.getCreatedBy());
    }

    public int update(WechatPushConfig c) {
        return jdbcTemplate.update(
            "UPDATE wechat_push_config SET group_name=?, webhook_url=?, status=?, remark=? WHERE id=?",
            c.getGroupName(), c.getWebhookUrl(), c.getStatus(), c.getRemark(), c.getId());
    }

    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM wechat_push_config WHERE id=?", id);
    }
}
