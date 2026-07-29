package com.crypto.entity;

import java.time.LocalDateTime;

/**
 * 企微推送任务实体
 */
public class WechatPushTask {

    private Long id;
    private Long configId;
    private String pushContent;
    private String pushMode;
    private String cronExpression;
    private String cronDesc;
    private Long createdBy;
    private String creatorName;
    private Integer enabled;
    private LocalDateTime lastPushTime;
    private String lastPushStatus;
    private String lastPushMsg;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 关联字段（查询时 join 用）
    private String groupName;
    private String webhookUrl;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }

    public String getPushContent() { return pushContent; }
    public void setPushContent(String pushContent) { this.pushContent = pushContent; }

    public String getPushMode() { return pushMode; }
    public void setPushMode(String pushMode) { this.pushMode = pushMode; }

    public String getCronExpression() { return cronExpression; }
    public void setCronExpression(String cronExpression) { this.cronExpression = cronExpression; }

    public String getCronDesc() { return cronDesc; }
    public void setCronDesc(String cronDesc) { this.cronDesc = cronDesc; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer enabled) { this.enabled = enabled; }

    public LocalDateTime getLastPushTime() { return lastPushTime; }
    public void setLastPushTime(LocalDateTime lastPushTime) { this.lastPushTime = lastPushTime; }

    public String getLastPushStatus() { return lastPushStatus; }
    public void setLastPushStatus(String lastPushStatus) { this.lastPushStatus = lastPushStatus; }

    public String getLastPushMsg() { return lastPushMsg; }
    public void setLastPushMsg(String lastPushMsg) { this.lastPushMsg = lastPushMsg; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // 关联字段
    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getWebhookUrl() { return webhookUrl; }
    public void setWebhookUrl(String webhookUrl) { this.webhookUrl = webhookUrl; }
}
