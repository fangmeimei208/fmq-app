package com.crypto.mapper;

import com.crypto.entity.WechatPushTask;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class WechatPushTaskMapper {

    private final JdbcTemplate jdbcTemplate;

    public WechatPushTaskMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<WechatPushTask> rowMapper = new RowMapper<WechatPushTask>() {
        @Override
        public WechatPushTask mapRow(ResultSet rs, int rowNum) throws SQLException {
            WechatPushTask t = new WechatPushTask();
            t.setId(rs.getLong("id"));
            t.setConfigId(rs.getLong("config_id"));
            t.setPushContent(rs.getString("push_content"));
            t.setPushMode(rs.getString("push_mode"));
            t.setCronExpression(rs.getString("cron_expression"));
            t.setCronDesc(rs.getString("cron_desc"));
            t.setEnabled(rs.getInt("enabled"));
            Timestamp lpt = rs.getTimestamp("last_push_time");
            t.setLastPushTime(lpt != null ? lpt.toLocalDateTime() : null);
            t.setLastPushStatus(rs.getString("last_push_status"));
            t.setLastPushMsg(rs.getString("last_push_msg"));
            Timestamp ca = rs.getTimestamp("created_at");
            t.setCreatedAt(ca != null ? ca.toLocalDateTime() : null);
            Timestamp ua = rs.getTimestamp("updated_at");
            t.setUpdatedAt(ua != null ? ua.toLocalDateTime() : null);
            return t;
        }
    };

    public List<WechatPushTask> findAll() {
        String sql = "SELECT t.*, c.group_name, c.webhook_url FROM wechat_push_task t " +
                     "LEFT JOIN wechat_push_config c ON t.config_id = c.id ORDER BY t.id DESC";
        return jdbcTemplate.query(sql, rowMapperWithJoin);
    }

    public List<WechatPushTask> findEnabledScheduled() {
        String sql = "SELECT t.*, c.group_name, c.webhook_url FROM wechat_push_task t " +
                     "LEFT JOIN wechat_push_config c ON t.config_id = c.id " +
                     "WHERE t.enabled = 1 AND t.push_mode = 'SCHEDULED' AND c.status = 1";
        return jdbcTemplate.query(sql, rowMapperWithJoin);
    }

    public WechatPushTask findById(Long id) {
        String sql = "SELECT t.*, c.group_name, c.webhook_url FROM wechat_push_task t " +
                     "LEFT JOIN wechat_push_config c ON t.config_id = c.id WHERE t.id = ?";
        List<WechatPushTask> list = jdbcTemplate.query(sql, rowMapperWithJoin, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<WechatPushTask> findByConfigId(Long configId) {
        String sql = "SELECT t.*, c.group_name, c.webhook_url FROM wechat_push_task t " +
                     "LEFT JOIN wechat_push_config c ON t.config_id = c.id WHERE t.config_id = ? ORDER BY t.id DESC";
        return jdbcTemplate.query(sql, rowMapperWithJoin, configId);
    }

    public int insert(WechatPushTask t) {
        return jdbcTemplate.update(
            "INSERT INTO wechat_push_task (config_id, push_content, push_mode, cron_expression, cron_desc, enabled) VALUES (?,?,?,?,?,?)",
            t.getConfigId(), t.getPushContent(), t.getPushMode(),
            t.getCronExpression(), t.getCronDesc(), t.getEnabled() != null ? t.getEnabled() : 1);
    }

    public int update(WechatPushTask t) {
        return jdbcTemplate.update(
            "UPDATE wechat_push_task SET config_id=?, push_content=?, push_mode=?, cron_expression=?, cron_desc=? WHERE id=?",
            t.getConfigId(), t.getPushContent(), t.getPushMode(),
            t.getCronExpression(), t.getCronDesc(), t.getId());
    }

    public int updatePushResult(Long id, String status, String msg) {
        return jdbcTemplate.update(
            "UPDATE wechat_push_task SET last_push_time=NOW(), last_push_status=?, last_push_msg=? WHERE id=?",
            status, msg, id);
    }

    public int toggleEnabled(Long id, int enabled) {
        return jdbcTemplate.update("UPDATE wechat_push_task SET enabled=? WHERE id=?", enabled, id);
    }

    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM wechat_push_task WHERE id=?", id);
    }

    public int deleteByConfigId(Long configId) {
        return jdbcTemplate.update("DELETE FROM wechat_push_task WHERE config_id=?", configId);
    }

    // ---- 带 JOIN 的 RowMapper ----

    private final RowMapper<WechatPushTask> rowMapperWithJoin = new RowMapper<WechatPushTask>() {
        @Override
        public WechatPushTask mapRow(ResultSet rs, int rowNum) throws SQLException {
            WechatPushTask t = new WechatPushTask();
            t.setId(rs.getLong("id"));
            t.setConfigId(rs.getLong("config_id"));
            t.setPushContent(rs.getString("push_content"));
            t.setPushMode(rs.getString("push_mode"));
            t.setCronExpression(rs.getString("cron_expression"));
            t.setCronDesc(rs.getString("cron_desc"));
            t.setEnabled(rs.getInt("enabled"));
            Timestamp lpt = rs.getTimestamp("last_push_time");
            t.setLastPushTime(lpt != null ? lpt.toLocalDateTime() : null);
            t.setLastPushStatus(rs.getString("last_push_status"));
            t.setLastPushMsg(rs.getString("last_push_msg"));
            Timestamp ca = rs.getTimestamp("created_at");
            t.setCreatedAt(ca != null ? ca.toLocalDateTime() : null);
            Timestamp ua = rs.getTimestamp("updated_at");
            t.setUpdatedAt(ua != null ? ua.toLocalDateTime() : null);
            // join 字段
            try { t.setGroupName(rs.getString("group_name")); } catch (Exception e) { }
            try { t.setWebhookUrl(rs.getString("webhook_url")); } catch (Exception e) { }
            return t;
        }
    };
}
