package com.crypto.mapper;

import com.crypto.entity.WechatPushLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class WechatPushLogMapper {

    private final JdbcTemplate jdbcTemplate;

    public WechatPushLogMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<WechatPushLog> rowMapper = new RowMapper<WechatPushLog>() {
        @Override
        public WechatPushLog mapRow(ResultSet rs, int rowNum) throws SQLException {
            WechatPushLog log = new WechatPushLog();
            log.setId(rs.getLong("id"));
            log.setTaskId(rs.getObject("task_id", Long.class));
            log.setConfigId(rs.getObject("config_id", Long.class));
            log.setGroupName(rs.getString("group_name"));
            log.setPushContent(rs.getString("push_content"));
            log.setPushMode(rs.getString("push_mode"));
            log.setErrcode(rs.getObject("errcode", Integer.class));
            log.setErrmsg(rs.getString("errmsg"));
            log.setStatus(rs.getString("status"));
            Timestamp ca = rs.getTimestamp("created_at");
            log.setCreatedAt(ca != null ? ca.toLocalDateTime() : null);
            return log;
        }
    };

    public int insert(WechatPushLog log) {
        return jdbcTemplate.update(
            "INSERT INTO wechat_push_log (task_id, config_id, group_name, push_content, push_mode, errcode, errmsg, status) VALUES (?,?,?,?,?,?,?,?)",
            log.getTaskId(), log.getConfigId(), log.getGroupName(),
            log.getPushContent(), log.getPushMode(),
            log.getErrcode(), log.getErrmsg(), log.getStatus());
    }

    public List<WechatPushLog> findAll(int limit) {
        return jdbcTemplate.query(
            "SELECT * FROM wechat_push_log ORDER BY id DESC LIMIT ?", rowMapper, limit);
    }

    public List<WechatPushLog> findByTaskId(Long taskId) {
        return jdbcTemplate.query(
            "SELECT * FROM wechat_push_log WHERE task_id = ? ORDER BY id DESC", rowMapper, taskId);
    }
}
