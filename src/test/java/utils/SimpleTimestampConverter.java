package utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class SimpleTimestampConverter {
	
    public static void main(String[] args) {
        long timestamp = 1814630407000L;
        
        // 转换为北京时间
        String beijingTime = LocalDateTime.ofInstant(
            Instant.ofEpochMilli(timestamp),
            ZoneId.of("Asia/Shanghai")
        ).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        
        System.out.println("时间戳: " + timestamp);
        System.out.println("北京时间: " + beijingTime);
    }
    
}