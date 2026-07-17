package com.crypto.controller.system;

import com.crypto.entity.WechatPushTask;
import com.crypto.service.WechatPushService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * 企微定时推送调度器
 * 每分钟扫描一次待执行的定时任务
 */
@Component
public class WechatPushScheduler {

    private static final Logger logger = LoggerFactory.getLogger(WechatPushScheduler.class);

    @Autowired
    private WechatPushService wechatPushService;

    /**
     * 每分钟执行一次扫描
     */
    @Scheduled(cron = "0 * * * * ?")
    public void checkAndPush() {
        logger.debug("========== 企微定时推送扫描开始 ==========");

        try {
            List<WechatPushTask> tasks = wechatPushService.getEnabledScheduledTasks();

            if (tasks.isEmpty()) {
                logger.debug("无待执行的定时推送任务");
                return;
            }

            // 当前时间（精确到分钟，秒+纳秒清零）
            LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
            ZonedDateTime zonedNow = now.atZone(ZoneId.of("Asia/Shanghai"));

            int successCount = 0;
            int failCount = 0;

            for (WechatPushTask task : tasks) {
                try {
                    String cronExpr = task.getCronExpression();
                    if (cronExpr == null || cronExpr.trim().isEmpty()) {
                        logger.warn("定时任务ID={} 缺少cron表达式，跳过", task.getId());
                        continue;
                    }

                    CronExpression cronExpression = CronExpression.parse(cronExpr);

                    // next() 返回下一次执行时间 (Temporal 适配 Java 8 编译)
                    java.time.temporal.Temporal nextTemporal = cronExpression.next(zonedNow);
                    LocalDateTime next = nextTemporal != null ? LocalDateTime.from(nextTemporal) : null;

                    if (next != null) {
                        LocalDateTime nextTruncated = next.truncatedTo(ChronoUnit.MINUTES);

                        // 判断当前分钟是否命中
                        if (nextTruncated.equals(now)) {
                            // 去重：如果上次推送时间已经是当前分钟，跳过
                            if (task.getLastPushTime() != null
                                && task.getLastPushTime().truncatedTo(ChronoUnit.MINUTES).equals(now)) {
                                logger.debug("任务ID={} 当前分钟已执行过，跳过", task.getId());
                                continue;
                            }

                            logger.info("触发定时推送 - 任务ID: {}, cron: {}, 描述: {}",
                                    task.getId(), cronExpr, task.getCronDesc());

                            wechatPushService.executePush(task);
                            successCount++;
                        }
                    }
                } catch (Exception e) {
                    failCount++;
                    logger.error("定时推送任务执行失败 - 任务ID: {}", task.getId(), e);
                }

                // 避免并发过高
                if (tasks.size() > 1) {
                    Thread.sleep(100);
                }
            }

            if (successCount > 0 || failCount > 0) {
                logger.info("定时推送扫描完成 - 成功: {}, 失败: {}", successCount, failCount);
            }

        } catch (Exception e) {
            logger.error("定时推送扫描异常: {}", e.getMessage(), e);
        }

        logger.debug("========== 企微定时推送扫描结束 ==========");
    }
}
