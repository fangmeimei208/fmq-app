package com.crypto.mapper;

import com.crypto.entity.WechatPushLog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Repository
public class WechatPushLogMapper {

    private final JdbcTemplate jdbcTemplate;

    public WechatPushLogMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private WechatPushLog mapLog(ResultSet rs) throws SQLException {
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
        try { long cb = rs.getLong("created_by"); if (!rs.wasNull()) log.setCreatedBy(cb); } catch (Exception e) { }
        try { log.setCreatorName(rs.getString("creator_name")); } catch (Exception e) { }
        return log;
    }

    private final RowMapper<WechatPushLog> rowMapperWithJoin = (rs, rowNum) -> mapLog(rs);

    private static final String LOG_JOIN_SQL =
        "SELECT l.*, t.created_by, u.real_name as creator_name FROM wechat_push_log l " +
        "LEFT JOIN wechat_push_task t ON l.task_id = t.id " +
        "LEFT JOIN sys_user u ON t.created_by = u.id ";

    public int insert(WechatPushLog log) {
        return jdbcTemplate.update(
            "INSERT INTO wechat_push_log (task_id, config_id, group_name, push_content, push_mode, errcode, errmsg, status) VALUES (?,?,?,?,?,?,?,?)",
            log.getTaskId(), log.getConfigId(), log.getGroupName(),
            log.getPushContent(), log.getPushMode(),
            log.getErrcode(), log.getErrmsg(), log.getStatus());
    }

    public List<WechatPushLog> findAll(int limit) {
        return jdbcTemplate.query(LOG_JOIN_SQL + "ORDER BY l.id DESC LIMIT ?", rowMapperWithJoin, limit);
    }

    /**
     * 按条件查询日志
     */
    public List<WechatPushLog> query(Long userId, Boolean isAdmin, Long configId, String keyword, int limit) {
        StringBuilder sql = new StringBuilder(LOG_JOIN_SQL + "WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (!isAdmin) {
            sql.append("AND t.created_by = ? ");
            params.add(userId);
        }

        if (configId != null) {
            sql.append("AND l.config_id = ? ");
            params.add(configId);
        }

        if (keyword != null && !keyword.trim().isEmpty()) {
            sql.append("AND l.push_content LIKE ? ");
            params.add("%" + keyword.trim() + "%");
        }

        sql.append("ORDER BY l.id DESC LIMIT ?");
        params.add(limit);

        return jdbcTemplate.query(sql.toString(), rowMapperWithJoin, params.toArray());
    }

    public List<WechatPushLog> findByTaskId(Long taskId) {
        return jdbcTemplate.query(LOG_JOIN_SQL + "WHERE l.task_id = ? ORDER BY l.id DESC", rowMapperWithJoin, taskId);
    }
}
