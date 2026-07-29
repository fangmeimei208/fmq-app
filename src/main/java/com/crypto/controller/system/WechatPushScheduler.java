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
            // next(Temporal) 是"严格下一次"，now=08:00时next返回10:00而不是08:00。
            // 所以用 now-1秒 作为锚点，确保 now 正好在命中区间内时会返回 now
            ZonedDateTime anchor = now.minusSeconds(1).atZone(ZoneId.of("Asia/Shanghai"));

            int successCount = 0;
            int failCount = 0;

            for (WechatPushTask task : tasks) {
                try {
                    String cronExpr = task.getCronExpression();
                    if (cronExpr == null || cronExpr.trim().isEmpty()) {
                        logger.warn("定时任务ID={} 缺少cron表达式，跳过", task.getId());
                        continue;
                    }

                    // 容错：5 字段 Linux cron → 6 字段 Spring cron
                    cronExpr = normalizeCron(cronExpr);
                    CronExpression cronExpression = CronExpression.parse(cronExpr);

                    // next() 返回下一次执行时间 (Temporal 适配 Java 8 编译)
                    java.time.temporal.Temporal nextTemporal = cronExpression.next(anchor);
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

    /**
     * 容错：5 字段 Linux cron (分 时 日 月 周) → 6 字段 Spring cron (秒 分 时 日 月 周)
     * 5 字段时自动在最前补秒，最后把最后一个字段当作星期
     */
    static String normalizeCron(String expr) {
        if (expr == null) return null;
        expr = expr.trim();
        // 去掉可能的多余空格
        String[] parts = expr.split("\\s+");
        if (parts.length == 5) {
            // 5 字段 Linux cron: 分 时 日 月 周 → 补秒 + 问号处理
            String minute = parts[0], hour = parts[1], day = parts[2], month = parts[3], week = parts[4];
            // 日和星期不能同时为具体值，如果有具体星期值，日用 ?
            if (!"*".equals(week) && "?".equals(week)) week = "?";
            if ("*".equals(day) && !"*".equals(week) && !"?".equals(week)) day = "?";
            if ("*".equals(week) && !"*".equals(day) && !"?".equals(day)) week = "?";
            expr = "0 " + minute + " " + hour + " " + day + " " + month + " " + week;
            logger.debug("cron 5→6 字段转换: {} → {}", String.join(" ", parts), expr);
        } else if (parts.length == 6) {
            // 已经是 6 字段，直接返回
        } else {
            logger.warn("cron 表达式字段数异常: {} 字段 (期望 5 或 6)", parts.length);
        }
        return expr;
    }
}
