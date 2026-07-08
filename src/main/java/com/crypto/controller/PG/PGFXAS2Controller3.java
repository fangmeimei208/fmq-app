package com.crypto.controller.PG;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

//@RestController
//@RequestMapping("/api/pgfxas2")
public class PGFXAS2Controller3 {

    private static final Logger logger = LoggerFactory.getLogger(PGFXAS2Controller3.class);

    @Autowired
    private GridFsTemplate gridFsTemplate;  // 使用 Spring Data 的 GridFsTemplate

    /**
     * 目录导入方法
     */
    @PostMapping("/import")
    public ResponseEntity<Map<String, Object>> importFiles(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            String datahubCustomerId = request.get("messageSet");	// 消息集
            String messageId = request.get("messageCode");			// 消息编码
            String filePath = request.get("filePath");				// 文件夹路径
            String organizationId = "PG";

            if (filePath == null || filePath.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "目录路径不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            if (organizationId == null || organizationId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "组织ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            File directory = new File(filePath.trim());
            if (!directory.exists() || !directory.isDirectory()) {
                response.put("success", false);
                response.put("message", "目录不存在或不是有效目录: " + filePath);
                return ResponseEntity.badRequest().body(response);
            }

            File[] files = directory.listFiles();
            if (files == null || files.length == 0) {
                response.put("success", false);
                response.put("message", "目录下没有文件: " + filePath);
                return ResponseEntity.badRequest().body(response);
            }

            String addTime = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
            String addTimeIndex = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
            String timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

            int totalFiles = 0;
            int successCount = 0;
            int failCount = 0;
            List<Map<String, Object>> importedFiles = new ArrayList<>();

            for (File file : files) {
                if (!file.isFile()) continue;
                
                totalFiles++;
                String fileName = file.getName();
                
                try {
                    
                    Document metadata = new Document();
                    metadata.put("aliases", fileName);
                    metadata.put("addTime", addTime);
                    metadata.put("fileDel", "");
                    metadata.put("filePath", filePath);	
                    metadata.put("fileErrorInfo", "");
                    metadata.put("timeStamp", timeStamp);
                    metadata.put("organizationId", organizationId);
                    metadata.put("datahubCustomerId", datahubCustomerId);
                    metadata.put("messageId", messageId);
                    metadata.put("filename", fileName);
                    metadata.put("jndi", "RECEIVE");	// 收件箱
                    metadata.put("messageGroupSysId", "");
                    metadata.put("addTimeIndex", addTimeIndex);
                    metadata.put("contentType", fileName.substring(fileName.lastIndexOf(".")+1)); // 文件后缀

                    // 使用 GridFsTemplate 上传
                    try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                        ObjectId fileId = gridFsTemplate.store(inputStream, fileName, metadata);
                        
                        Map<String, Object> fileInfo = new HashMap<>();
                        fileInfo.put("fileName", fileName);
                        fileInfo.put("fileSize", formatFileSize(file.length()));
                        fileInfo.put("fileId", fileId.toString());
                        fileInfo.put("alias", fileName);
                        importedFiles.add(fileInfo);
                        
                        successCount++;
                        logger.debug("导入文件成功: {}, fileId: {}", fileName, fileId);
                    }
                    
                } catch (IOException e) {
                    failCount++;
                    logger.error("导入文件失败: {}, 错误: {}", fileName, e.getMessage());
                }
            }

            long elapsedTime = System.currentTimeMillis() - startTime;

            Map<String, Object> data = new HashMap<>();
            data.put("totalFiles", totalFiles);
            data.put("successCount", successCount);
            data.put("failCount", failCount);
            data.put("bucketName", "PG.DATAHUB_EDI_FILE");
            data.put("directoryPath", filePath);
            data.put("importedFiles", importedFiles);
            data.put("elapsedTime", formatElapsedTime(elapsedTime));

            response.put("success", true);
            response.put("message", "导入完成");
            response.put("data", data);

            logger.info("导入完成 - 总数: {}, 成功: {}, 失败: {}, 耗时: {}", 
                    totalFiles, successCount, failCount, formatElapsedTime(elapsedTime));

        } catch (Exception e) {
            logger.error("导入失败: {}", e.getMessage(), e);
            response.put("success", false);
            response.put("message", "导入失败: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }

        return ResponseEntity.ok(response);
    }


    // ================== 辅助方法 ==================

    private String formatFileSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024));
        return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024));
    }

    private String formatElapsedTime(long millis) {
        if (millis < 1000) return millis + "ms";
        if (millis < 60000) return String.format("%.1fs", millis / 1000.0);
        long minutes = millis / 60000;
        long seconds = (millis % 60000) / 1000;
        return minutes + "m " + seconds + "s";
    }
}