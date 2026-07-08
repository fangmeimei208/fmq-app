package com.crypto.common;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

@Component
public class AppUtils {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    
    /**
     * 格式化日期时间
     */
    public String formatDateTime(Object dateTimeObj) {
        if (dateTimeObj == null) {
            return null;
        }
        if (dateTimeObj instanceof LocalDateTime) {
            return ((LocalDateTime) dateTimeObj).format(FORMATTER);
        }
        if (dateTimeObj instanceof Timestamp) {
            return ((Timestamp) dateTimeObj).toLocalDateTime().format(FORMATTER);
        }
        // 处理 13位时间戳（Long 类型）
        if (dateTimeObj instanceof Long) {      	
            long timestamp = (Long) dateTimeObj;
            // 使用东八区时区
            ZoneId beijingZone = ZoneId.of("Asia/Shanghai");
            
            // 转换为LocalDateTime
            LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), beijingZone);
            
            // 格式化输出
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return dateTime.format(formatter);            
        }
        return dateTimeObj.toString();
    }
    
}
