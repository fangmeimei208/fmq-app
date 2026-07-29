package com.crypto.service;

import com.crypto.entity.WechatPushConfig;
import com.crypto.entity.WechatPushLog;
import com.crypto.entity.WechatPushTask;
import com.crypto.mapper.WechatPushConfigMapper;
import com.crypto.mapper.WechatPushLogMapper;
import com.crypto.mapper.WechatPushTaskMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class WechatPushService {

    private static final Logger logger = LoggerFactory.getLogger(WechatPushService.class);

    private final WechatPushConfigMapper configMapper;
    private final WechatPushTaskMapper taskMapper;
    private final WechatPushLogMapper logMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public WechatPushService(WechatPushConfigMapper configMapper,
                             WechatPushTaskMapper taskMapper,
                             WechatPushLogMapper logMapper) {
        this.configMapper = configMapper;
        this.taskMapper = taskMapper;
        this.logMapper = logMapper;
    }

    // ==================== 群配置管理 ====================

    public List<WechatPushConfig> getAllConfigs() {
        return configMapper.findAll();
    }

    public List<WechatPushConfig> getActiveConfigs() {
        return configMapper.findActive();
    }

    public WechatPushConfig getConfigById(Long id) {
        return configMapper.findById(id);
    }

    public WechatPushConfig addConfig(WechatPushConfig config) {
        configMapper.insert(config);
        return config;
    }

    public WechatPushConfig updateConfig(WechatPushConfig config) {
        configMapper.update(config);
        return config;
    }

    public boolean deleteConfig(Long id) {
        // 级联删除关联任务
        taskMapper.deleteByConfigId(id);
        return configMapper.delete(id) > 0;
    }

    public int countTasksByConfigId(Long configId) {
        List<WechatPushTask> tasks = taskMapper.findByConfigId(configId);
        return tasks.size();
    }

    // ==================== 推送任务管理 ====================

    public List<WechatPushTask> getAllTasks() {
        return taskMapper.findAll();
    }

    public WechatPushTask getTaskById(Long id) {
        return taskMapper.findById(id);
    }

    public WechatPushTask addTask(WechatPushTask task) {
        taskMapper.insert(task);
        return task;
    }

    public WechatPushTask updateTask(WechatPushTask task) {
        taskMapper.update(task);
        return task;
    }

    public boolean deleteTask(Long id) {
        return taskMapper.delete(id) > 0;
    }

    public boolean toggleTask(Long id, int enabled) {
        return taskMapper.toggleEnabled(id, enabled) > 0;
    }

    public List<WechatPushTask> getEnabledScheduledTasks() {
        return taskMapper.findEnabledScheduled();
    }

    // ==================== 消息发送核心逻辑 ====================

    /**
     * 构建企微文本消息体
     * {
     *     "msgtype": "text",
     *     "text": {
     *         "content": "消息内容",
     *         "mentioned_mobile_list": ["@all"]
     *     }
     * }
     */
    public Map<String, Object> buildMessage(String content) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("msgtype", "text");

        Map<String, Object> text = new LinkedHashMap<>();
        text.put("content", content);
        text.put("mentioned_mobile_list", List.of("@all"));

        message.put("text", text);
        return message;
    }

    /**
     * 发送消息到指定 Webhook
     */
    public SendResult sendToWebhook(String webhookUrl, String content) {
        SendResult result = new SendResult();

        try {
            Map<String, Object> message = buildMessage(content);
            String requestJson = objectMapper.writeValueAsString(message);
            logger.debug("发送企微消息 - url: {}, body: {}", webhookUrl, requestJson);

            String response = restTemplate.postForObject(webhookUrl, message, String.class);
            logger.debug("企微响应: {}", response);

            if (response == null || response.trim().isEmpty()) {
                result.setSuccess(false);
                result.setErrcode(-1);
                result.setErrmsg("企微响应为空");
                return result;
            }

            JsonNode rootNode = objectMapper.readTree(response);
            int errcode = rootNode.has("errcode") ? rootNode.get("errcode").asInt() : -1;
            String errmsg = rootNode.has("errmsg") ? rootNode.get("errmsg").asText() : "未知错误";

            result.setErrcode(errcode);
            result.setErrmsg(errmsg);
            result.setSuccess(errcode == 0);

            if (errcode != 0) {
                logger.warn("企微返回错误 - errcode: {}, errmsg: {}, 说明: {}", errcode, errmsg, getWechatErrorMsg(errcode));
            }

        } catch (Exception e) {
            logger.error("发送企微消息异常: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setErrcode(-1);
            result.setErrmsg("发送异常: " + e.getMessage());
        }

        return result;
    }

    /**
     * 执行一条推送任务（发送 + 更新任务状态 + 记录日志）
     */
    public SendResult executePush(WechatPushTask task) {
        WechatPushConfig config = configMapper.findById(task.getConfigId());
        if (config == null) {
            SendResult fail = new SendResult();
            fail.setSuccess(false);
            fail.setErrcode(-1);
            fail.setErrmsg("群配置不存在");
            logger.error("任务ID={} 关联的群配置不存在", task.getId());
            return fail;
        }

        SendResult result = sendToWebhook(config.getWebhookUrl(), task.getPushContent());

        // 更新任务状态
        String status = result.isSuccess() ? "SUCCESS" : "FAIL";
        String msg = result.getErrmsg();
        taskMapper.updatePushResult(task.getId(), status, msg);

        // 记录日志
        WechatPushLog log = new WechatPushLog();
        log.setTaskId(task.getId());
        log.setConfigId(config.getId());
        log.setGroupName(config.getGroupName());
        log.setPushContent(task.getPushContent());
        log.setPushMode(task.getPushMode());
        log.setErrcode(result.getErrcode());
        log.setErrmsg(result.getErrmsg());
        log.setStatus(status);
        logMapper.insert(log);

        logger.info("推送完成 - 任务ID: {}, 群: {}, 结果: {}", task.getId(), config.getGroupName(), status);
        return result;
    }

    /**
     * 立即发送（不创建任务，直接发一条消息，并记录日志）
     */
    public SendResult sendImmediate(Long configId, String content) {
        WechatPushConfig config = configMapper.findById(configId);
        if (config == null) {
            SendResult fail = new SendResult();
            fail.setSuccess(false);
            fail.setErrcode(-1);
            fail.setErrmsg("群配置不存在");
            return fail;
        }

        SendResult result = sendToWebhook(config.getWebhookUrl(), content);

        // 记录日志（taskId 为 null）
        WechatPushLog log = new WechatPushLog();
        log.setTaskId(null);
        log.setConfigId(config.getId());
        log.setGroupName(config.getGroupName());
        log.setPushContent(content);
        log.setPushMode("IMMEDIATE");
        log.setErrcode(result.getErrcode());
        log.setErrmsg(result.getErrmsg());
        log.setStatus(result.isSuccess() ? "SUCCESS" : "FAIL");
        logMapper.insert(log);

        return result;
    }

    // ==================== 推送日志 ====================

    public List<WechatPushLog> getRecentLogs(int limit) {
        return logMapper.findAll(limit);
    }

    // ==================== 工具方法 ====================

    private String getWechatErrorMsg(int errcode) {
        switch (errcode) {
            case 0: return "请求成功";
            case 93000: return "机器人webhook地址不合法";
            case 45009: return "接口调用超过限制";
            case 45033: return "被限流，请降低调用频率";
            case 48001: return "API功能未授权";
            default: return "未知错误码: " + errcode;
        }
    }

    /**
     * 发送结果
     */
    public static class SendResult {
        private boolean success;
        private int errcode;
        private String errmsg;

        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public int getErrcode() { return errcode; }
        public void setErrcode(int errcode) { this.errcode = errcode; }

        public String getErrmsg() { return errmsg; }
        public void setErrmsg(String errmsg) { this.errmsg = errmsg; }
    }
}
