package com.crypto.entity;

import java.time.LocalDateTime;

public class FulleCompany {
    private Long id;
    private String companyName;
    private String shortName;
    private Long parentId;
    private Integer level;
    private java.math.BigDecimal totalShares;
    private String dataSource;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FulleCompany() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCompanyName() { return companyName; }
    public void setCompanyName(String companyName) { this.companyName = companyName; }
    public String getShortName() { return shortName; }
    public void setShortName(String shortName) { this.shortName = shortName; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public java.math.BigDecimal getTotalShares() { return totalShares; }
    public void setTotalShares(java.math.BigDecimal totalShares) { this.totalShares = totalShares; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
