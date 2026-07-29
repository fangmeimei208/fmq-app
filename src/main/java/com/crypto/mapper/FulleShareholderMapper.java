package com.crypto.mapper;

import com.crypto.entity.FulleShareholder;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

@Repository
public class FulleShareholderMapper {

    private final JdbcTemplate jdbcTemplate;

    public FulleShareholderMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<FulleShareholder> rowMapper = new RowMapper<FulleShareholder>() {
        @Override
        public FulleShareholder mapRow(ResultSet rs, int rowNum) throws SQLException {
            FulleShareholder s = new FulleShareholder();
            s.setId(rs.getLong("id"));
            s.setCompanyId(rs.getLong("company_id"));
            s.setHolderName(rs.getString("holder_name"));
            s.setHolderType(rs.getString("holder_type"));
            s.setLinkedCompanyId(rs.getObject("linked_company_id", Long.class));
            s.setShareRatio(rs.getBigDecimal("share_ratio"));
            s.setShareAmount(rs.getBigDecimal("share_amount"));
            java.sql.Date sd = rs.getDate("share_date");
            s.setShareDate(sd != null ? sd.toLocalDate() : null);
            s.setDataSource(rs.getString("data_source"));
            s.setRemark(rs.getString("remark"));
            Timestamp ca = rs.getTimestamp("created_at");
            s.setCreatedAt(ca != null ? ca.toLocalDateTime() : null);
            Timestamp ua = rs.getTimestamp("updated_at");
            s.setUpdatedAt(ua != null ? ua.toLocalDateTime() : null);
            return s;
        }
    };

    public List<FulleShareholder> findByCompanyId(Long companyId) {
        return jdbcTemplate.query(
            "SELECT * FROM fulle_shareholder WHERE company_id = ? ORDER BY share_ratio DESC", rowMapper, companyId);
    }

    public List<FulleShareholder> findByHolderNameLike(String name) {
        return jdbcTemplate.query(
            "SELECT * FROM fulle_shareholder WHERE holder_name LIKE ? ORDER BY share_ratio DESC",
            rowMapper, "%" + name + "%");
    }

    public List<FulleShareholder> findAllPersonShares() {
        // 查询所有自然人持股（非COMPANY、非EXTERNAL），用于全局穿透计算
        return jdbcTemplate.query(
            "SELECT * FROM fulle_shareholder WHERE holder_type NOT IN ('COMPANY', 'EXTERNAL') ORDER BY holder_name, share_ratio DESC",
            rowMapper);
    }

    public List<FulleShareholder> findAll() {
        return jdbcTemplate.query("SELECT * FROM fulle_shareholder ORDER BY company_id, share_ratio DESC", rowMapper);
    }

    public FulleShareholder findById(Long id) {
        List<FulleShareholder> list = jdbcTemplate.query(
            "SELECT * FROM fulle_shareholder WHERE id = ?", rowMapper, id);
        return list.isEmpty() ? null : list.get(0);
    }

    public int insert(FulleShareholder s) {
        return jdbcTemplate.update(
            "INSERT INTO fulle_shareholder (company_id, holder_name, holder_type, linked_company_id, share_ratio, share_amount, share_date, data_source, remark) VALUES (?,?,?,?,?,?,?,?,?)",
            s.getCompanyId(), s.getHolderName(), s.getHolderType(), s.getLinkedCompanyId(),
            s.getShareRatio(), s.getShareAmount(), s.getShareDate(), s.getDataSource(), s.getRemark());
    }

    public int update(FulleShareholder s) {
        if (s.getId() == null) return 0;
        return jdbcTemplate.update(
            "UPDATE fulle_shareholder SET company_id=?, holder_name=?, holder_type=?, linked_company_id=?, share_ratio=?, share_amount=?, share_date=?, data_source=?, remark=? WHERE id=?",
            s.getCompanyId(), s.getHolderName(), s.getHolderType(), s.getLinkedCompanyId(),
            s.getShareRatio(), s.getShareAmount(), s.getShareDate(), s.getDataSource(), s.getRemark(), s.getId());
    }

    public int delete(Long id) {
        return jdbcTemplate.update("DELETE FROM fulle_shareholder WHERE id=?", id);
    }

    public int deleteByCompanyId(Long companyId) {
        return jdbcTemplate.update("DELETE FROM fulle_shareholder WHERE company_id=?", companyId);
    }
}
