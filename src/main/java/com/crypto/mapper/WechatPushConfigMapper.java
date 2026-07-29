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
            WechatPushConfig c = new WechatPushConfig();
            c.setId(rs.getLong("id"));
            c.setGroupName(rs.getString("group_name"));
            c.setWebhookUrl(rs.getString("webhook_url"));
            c.setStatus(rs.getInt("status"));
            c.setRemark(rs.getString("remark"));
            Timestamp ca = rs.getTimestamp("created_at");
            c.setCreatedAt(ca != null ? ca.toLocalDateTime() : null);
            Timestamp ua = rs.getTimestamp("updated_at");
            c.setUpdatedAt(ua != null ? ua.toLocalDateTime() : null);
            return c;
        }
    };

    public List<WechatPushConfig> findAll() {
        return jdbcTemplate.query("SELECT * FROM wechat_push_config ORDER BY id", rowMapper);
    }

    public List<WechatPushConfig> findActive() {
        return jdbcTemplate.query("SELECT * FROM wechat_push_config WHERE status = 1 ORDER BY id", rowMapper);
    }

    public WechatPushConfig findById(Long id) {
        List<WechatPushConfig> list = jdbcTemplate.query(
            "SELECT * FROM wechat_push_config WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(WechatPushConfig c) {
        return jdbcTemplate.update(
            "INSERT INTO wechat_push_config (group_name, webhook_url, status, remark) VALUES (?,?,?,?)",
            c.getGroupName(), c.getWebhookUrl(), c.getStatus() != null ? c.getStatus() : 1, c.getRemark());
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
