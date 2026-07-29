package com.crypto.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class FulleShareholder {
    private Long id;
    private Long companyId;
    private String holderName;
    private String holderType;  // PERSON / COMPANY / EXTERNAL
    private Long linkedCompanyId;
    private BigDecimal shareRatio;  // 百分比值，如 27.53943 代表 27.53943%
    private BigDecimal shareAmount; // 认缴出资额(万元)
    private LocalDate shareDate;
    private String dataSource;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public FulleShareholder() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getHolderName() { return holderName; }
    public void setHolderName(String holderName) { this.holderName = holderName; }
    public String getHolderType() { return holderType; }
    public void setHolderType(String holderType) { this.holderType = holderType; }
    public Long getLinkedCompanyId() { return linkedCompanyId; }
    public void setLinkedCompanyId(Long linkedCompanyId) { this.linkedCompanyId = linkedCompanyId; }
    public BigDecimal getShareRatio() { return shareRatio; }
    public void setShareRatio(BigDecimal shareRatio) { this.shareRatio = shareRatio; }
    public BigDecimal getShareAmount() { return shareAmount; }
    public void setShareAmount(BigDecimal shareAmount) { this.shareAmount = shareAmount; }
    public LocalDate getShareDate() { return shareDate; }
    public void setShareDate(LocalDate shareDate) { this.shareDate = shareDate; }
    public String getDataSource() { return dataSource; }
    public void setDataSource(String dataSource) { this.dataSource = dataSource; }
    public String getRemark() { return remark; }
    public void setRemark(String remark) { this.remark = remark; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
