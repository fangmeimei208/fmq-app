package com.crypto.controller.system;

import com.crypto.entity.WechatPushConfig;
import com.crypto.entity.WechatPushLog;
import com.crypto.entity.WechatPushTask;
import com.crypto.service.WechatPushService;
import com.crypto.service.WechatPushService.SendResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/wechat-push")
public class WechatPushController {

    private static final Logger logger = LoggerFactory.getLogger(WechatPushController.class);

    private final WechatPushService service;

    public WechatPushController(WechatPushService service) {
        this.service = service;
    }

    // ==================== 群配置管理 ====================

    @GetMapping("/configs")
    public Map<String, Object> listConfigs() {
        return Map.of("success", true, "data", service.getAllConfigs());
    }

    @GetMapping("/configs/active")
    public Map<String, Object> listActiveConfigs() {
        return Map.of("success", true, "data", service.getActiveConfigs());
    }

    @GetMapping("/configs/{id}")
    public Map<String, Object> getConfig(@PathVariable Long id) {
        WechatPushConfig config = service.getConfigById(id);
        if (config == null) {
            return Map.of("success", false, "msg", "群配置不存在");
        }
        return Map.of("success", true, "data", config);
    }

    @PostMapping("/configs")
    public Map<String, Object> addConfig(@RequestBody WechatPushConfig config) {
        try {
            service.addConfig(config);
            logger.info("新增群配置 - 群名称: {}", config.getGroupName());
            return Map.of("success", true, "msg", "添加成功", "data", config);
        } catch (Exception e) {
            logger.error("新增群配置失败: {}", e.getMessage(), e);
            return Map.of("success", false, "msg", "添加失败: " + e.getMessage());
        }
    }

    @PutMapping("/configs/{id}")
    public Map<String, Object> updateConfig(@PathVariable Long id, @RequestBody WechatPushConfig config) {
        try {
            config.setId(id);
            service.updateConfig(config);
            logger.info("更新群配置 - ID: {}, 群名称: {}", id, config.getGroupName());
            return Map.of("success", true, "msg", "更新成功");
        } catch (Exception e) {
            logger.error("更新群配置失败: {}", e.getMessage(), e);
            return Map.of("success", false, "msg", "更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/configs/{id}")
    public Map<String, Object> deleteConfig(@PathVariable Long id) {
        try {
            int taskCount = service.countTasksByConfigId(id);
            boolean ok = service.deleteConfig(id);
            logger.info("删除群配置 - ID: {}, 关联任务数: {}", id, taskCount);
            return Map.of("success", ok, "msg", ok ? "删除成功，已同步删除 " + taskCount + " 个关联任务" : "删除失败");
        } catch (Exception e) {
            logger.error("删除群配置失败: {}", e.getMessage(), e);
            return Map.of("success", false, "msg", "删除失败: " + e.getMessage());
        }
    }

    // ==================== 推送任务管理 ====================

    @GetMapping("/tasks")
    public Map<String, Object> listTasks() {
        return Map.of("success", true, "data", service.getAllTasks());
    }

    @GetMapping("/tasks/{id}")
    public Map<String, Object> getTask(@PathVariable Long id) {
        WechatPushTask task = service.getTaskById(id);
        if (task == null) {
            return Map.of("success", false, "msg", "任务不存在");
        }
        return Map.of("success", true, "data", task);
    }

    @PostMapping("/tasks")
    public Map<String, Object> addTask(@RequestBody WechatPushTask task) {
        try {
            if (task.getPushMode() == null) {
                task.setPushMode("IMMEDIATE");
            }
            service.addTask(task);
            logger.info("新增推送任务 - ID: {}, 群ID: {}, 模式: {}", task.getId(), task.getConfigId(), task.getPushMode());
            return Map.of("success", true, "msg", "添加成功", "data", task);
        } catch (Exception e) {
            logger.error("新增推送任务失败: {}", e.getMessage(), e);
            return Map.of("success", false, "msg", "添加失败: " + e.getMessage());
        }
    }

    @PutMapping("/tasks/{id}")
    public Map<String, Object> updateTask(@PathVariable Long id, @RequestBody WechatPushTask task) {
        try {
            task.setId(id);
            service.updateTask(task);
            logger.info("更新推送任务 - ID: {}", id);
            return Map.of("success", true, "msg", "更新成功");
        } catch (Exception e) {
            logger.error("更新推送任务失败: {}", e.getMessage(), e);
            return Map.of("success", false, "msg", "更新失败: " + e.getMessage());
        }
    }

    @DeleteMapping("/tasks/{id}")
    public Map<String, Object> deleteTask(@PathVariable Long id) {
        try {
            boolean ok = service.deleteTask(id);
            return Map.of("success", ok, "msg", ok ? "删除成功" : "删除失败");
        } catch (Exception e) {
            logger.error("删除推送任务失败: {}", e.getMessage(), e);
            return Map.of("success", false, "msg", "删除失败: " + e.getMessage());
        }
    }

    @PutMapping("/tasks/{id}/toggle")
    public Map<String, Object> toggleTask(@PathVariable Long id, @RequestParam int enabled) {
        try {
            boolean ok = service.toggleTask(id, enabled);
            return Map.of("success", ok, "msg", ok ? (enabled == 1 ? "已启用" : "已停用") : "操作失败");
        } catch (Exception e) {
            logger.error("切换任务状态失败: {}", e.getMessage(), e);
            return Map.of("success", false, "msg", "操作失败: " + e.getMessage());
        }
    }

    // ==================== 发送消息 ====================

    /**
     * 立即发送（不保存任务）
     */
    @PostMapping("/send")
    public Map<String, Object> sendImmediate(@RequestBody Map<String, Object> body) {
        try {
            Long configId = Long.valueOf(body.get("configId").toString());
            String content = body.get("content").toString();

            SendResult result = service.sendImmediate(configId, content);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", result.isSuccess());
            resp.put("errcode", result.getErrcode());
            resp.put("errmsg", result.getErrmsg());
            resp.put("msg", result.isSuccess() ? "发送成功" : "发送失败: " + result.getErrmsg());
            return resp;
        } catch (Exception e) {
            logger.error("立即发送失败: {}", e.getMessage(), e);
            return Map.of("success", false, "msg", "发送异常: " + e.getMessage());
        }
    }

    /**
     * 执行已有任务的推送
     */
    @PostMapping("/tasks/{id}/send")
    public Map<String, Object> executeTask(@PathVariable Long id) {
        try {
            WechatPushTask task = service.getTaskById(id);
            if (task == null) {
                return Map.of("success", false, "msg", "任务不存在");
            }

            SendResult result = service.executePush(task);

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("success", result.isSuccess());
            resp.put("errcode", result.getErrcode());
            resp.put("errmsg", result.getErrmsg());
            resp.put("msg", result.isSuccess() ? "发送成功" : "发送失败: " + result.getErrmsg());
            return resp;
        } catch (Exception e) {
            logger.error("执行推送任务失败 - ID: {}: {}", id, e.getMessage(), e);
            return Map.of("success", false, "msg", "发送异常: " + e.getMessage());
        }
    }

    // ==================== 推送日志 ====================

    @GetMapping("/logs")
    public Map<String, Object> listLogs(@RequestParam(defaultValue = "50") int limit) {
        List<WechatPushLog> logs = service.getRecentLogs(Math.min(limit, 200));
        return Map.of("success", true, "data", logs);
    }
}
