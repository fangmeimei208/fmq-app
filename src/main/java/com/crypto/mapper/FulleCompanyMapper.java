package com.crypto.mapper;

import com.crypto.entity.FulleCompany;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class FulleCompanyMapper {

    private final JdbcTemplate jdbcTemplate;

    public FulleCompanyMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<FulleCompany> rowMapper = new RowMapper<FulleCompany>() {
        @Override
        public FulleCompany mapRow(ResultSet rs, int rowNum) throws SQLException {
            FulleCompany c = new FulleCompany();
            c.setId(rs.getLong("id"));
            c.setCompanyName(rs.getString("company_name"));
            c.setShortName(rs.getString("short_name"));
            c.setParentId(rs.getObject("parent_id", Long.class));
            c.setLevel(rs.getInt("level"));
            c.setTotalShares(rs.getBigDecimal("total_shares"));
            c.setDataSource(rs.getString("data_source"));
            c.setRemark(rs.getString("remark"));
            Timestamp ca = rs.getTimestamp("created_at");
            c.setCreatedAt(ca != null ? ca.toLocalDateTime() : null);
            Timestamp ua = rs.getTimestamp("updated_at");
            c.setUpdatedAt(ua != null ? ua.toLocalDateTime() : null);
            return c;
        }
    };

    public List<FulleCompany> findAll() {
        return jdbcTemplate.query("SELECT * FROM fulle_company ORDER BY level, id", rowMapper);
    }

    public FulleCompany findById(Long id) {
        List<FulleCompany> list = jdbcTemplate.query(
            "SELECT * FROM fulle_company WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<FulleCompany> findByParentId(Long parentId) {
        return jdbcTemplate.query(
            "SELECT * FROM fulle_company WHERE parent_id = ? ORDER BY id", rowMapper, parentId);
    }

    public FulleCompany findByRoot() {
        List<FulleCompany> list = jdbcTemplate.query(
            "SELECT * FROM fulle_company WHERE parent_id IS NULL ORDER BY id LIMIT 1", rowMapper);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(FulleCompany c) {
        return jdbcTemplate.update(
            "INSERT INTO fulle_company (company_name, short_name, parent_id, level, total_shares, data_source, remark) VALUES (?,?,?,?,?,?,?)",
            c.getCompanyName(), c.getShortName(), c.getParentId(), c.getLevel(),
            c.getTotalShares(), c.getDataSource(), c.getRemark());
    }

    public int update(FulleCompany c) {
        if (c.getId() == null) return 0;
        return jdbcTemplate.update(
            "UPDATE fulle_company SET company_name=?, short_name=?, parent_id=?, level=?, total_shares=?, data_source=?, remark=? WHERE id=?",
            c.getCompanyName(), c.getShortName(), c.getParentId(), c.getLevel(),
            c.getTotalShares(), c.getDataSource(), c.getRemark(), c.getId());
    }

    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM fulle_company WHERE id=?", id);
    }
}
