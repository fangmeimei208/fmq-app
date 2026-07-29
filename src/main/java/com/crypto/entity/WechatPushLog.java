package com.crypto.entity;

import java.time.LocalDateTime;

/**
 * 企微推送日志实体
 */
public class WechatPushLog {

    private Long id;
    private Long taskId;
    private Long configId;
    private String groupName;
    private String pushContent;
    private String pushMode;
    private Integer errcode;
    private String errmsg;
    private String status;
    private Long createdBy;
    private String creatorName;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTaskId() { return taskId; }
    public void setTaskId(Long taskId) { this.taskId = taskId; }

    public Long getConfigId() { return configId; }
    public void setConfigId(Long configId) { this.configId = configId; }

    public String getGroupName() { return groupName; }
    public void setGroupName(String groupName) { this.groupName = groupName; }

    public String getPushContent() { return pushContent; }
    public void setPushContent(String pushContent) { this.pushContent = pushContent; }

    public String getPushMode() { return pushMode; }
    public void setPushMode(String pushMode) { this.pushMode = pushMode; }

    public Integer getErrcode() { return errcode; }
    public void setErrcode(Integer errcode) { this.errcode = errcode; }

    public String getErrmsg() { return errmsg; }
    public void setErrmsg(String errmsg) { this.errmsg = errmsg; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }

    public String getCreatorName() { return creatorName; }
    public void setCreatorName(String creatorName) { this.creatorName = creatorName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
