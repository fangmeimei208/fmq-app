package com.crypto.controller.PG;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 宝洁分销  AS2 压力测试控制器
 */
//@RestController
//@RequestMapping("/api/pgfxas2")
public class PGFXAS2Controller2 {

    private static final Logger logger = LoggerFactory.getLogger(PGFXAS2Controller2.class);

    @Autowired
    private MongoTemplate mongoTemplate;

    /**
     * 导入文件到MongoDB
     * POST /api/pgfxas2/import
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importFiles(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        long startTime = System.currentTimeMillis();

        try {
            String messageSet = request.get("messageSet");
            String messageCode = request.get("messageCode");
            String filePath = request.get("filePath");

            // 参数验证
            if (messageSet == null || messageSet.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "消息集不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            if (messageCode == null || messageCode.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "消息编码不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            if (filePath == null || filePath.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "文件路径不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            // 检查目录是否存在
            File directory = new File(filePath.trim());
            if (!directory.exists()) {
                response.put("success", false);
                response.put("message", "文件路径不存在: " + filePath);
                return ResponseEntity.badRequest().body(response);
            }
            if (!directory.isDirectory()) {
                response.put("success", false);
                response.put("message", "路径不是目录: " + filePath);
                return ResponseEntity.badRequest().body(response);
            }

            // 获取所有文件
            File[] files = directory.listFiles();
            if (files == null || files.length == 0) {
                response.put("success", false);
                response.put("message", "目录下没有文件: " + filePath);
                return ResponseEntity.badRequest().body(response);
            }

            // 创建MongoDB集合名称
            String collectionName = "pgfx_as2_" + messageSet.trim() + "_" + messageCode.trim();

            int totalFiles = 0;
            int successCount = 0;
            int failCount = 0;
            List<String> failFiles = new ArrayList<>();

            String importTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

            // 遍历文件
            for (File file : files) {
                if (file.isFile()) {
                    totalFiles++;
                    try {
                        // 读取文件内容
                        String content = new String(Files.readAllBytes(Paths.get(file.getAbsolutePath())));
                        
                        // 构建文档
                        Document doc = new Document();
                        doc.put("messageSet", messageSet.trim());
                        doc.put("messageCode", messageCode.trim());
                        doc.put("fileName", file.getName());
                        doc.put("filePath", file.getAbsolutePath());
                        doc.put("fileSize", file.length());
                        doc.put("content", content);
                        doc.put("contentLength", content.length());
                        doc.put("importTime", importTime);
                        doc.put("lastModified", new Date(file.lastModified()));

                        // 检查是否已存在同名文件，存在则删除
                        Document filter = new Document("fileName", file.getName());
                        mongoTemplate.remove(filter, collectionName);
                        
                        // 插入新文档
                        mongoTemplate.insert(doc, collectionName);
                        
                        successCount++;
                        logger.debug("导入文件: {}", file.getName());
                        
                    } catch (IOException e) {
                        failCount++;
                        failFiles.add(file.getName() + ": " + e.getMessage());
                        logger.error("读取文件失败: {}, 错误: {}", file.getName(), e.getMessage());
                    }
                }
            }

            long elapsedTime = System.currentTimeMillis() - startTime;

            // 构建响应
            Map<String, Object> data = new HashMap<>();
            data.put("totalFiles", totalFiles);
            data.put("successCount", successCount);
            data.put("failCount", failCount);
            data.put("collectionName", collectionName);
            data.put("elapsedTime", formatElapsedTime(elapsedTime));
            if (!failFiles.isEmpty()) {
                data.put("failFiles", failFiles);
            }

            response.put("success", true);
            response.put("message", "导入完成");
            response.put("data", data);

            logger.info("导入完成 - 总数: {}, 成功: {}, 失败: {}, 耗时: {}", 
                    totalFiles, successCount, failCount, formatElapsedTime(elapsedTime));

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("导入失败: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "导入失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }


    /**
     * 格式化耗时
     */
    private String formatElapsedTime(long millis) {
        if (millis < 1000) {
            return millis + "ms";
        }
        if (millis < 60000) {
            return String.format("%.1fs", millis / 1000.0);
        }
        long minutes = millis / 60000;
        long seconds = (millis % 60000) / 1000;
        return minutes + "m " + seconds + "s";
    }
}
