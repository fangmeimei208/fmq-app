package com.crypto.controller.JLZY.sendZH;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;

/**
 * 文件下载工具类
 * 功能：下载URL对应的文件，输出文件流和Base64字符串
 */
public class FileDownloadUtil {

    /**
     * 下载文件并返回文件流和Base64字符串
     * @param fileUrl 文件URL
     * @return 包含文件流和Base64的结果对象
     */
    public static DownloadResult downloadFile(String fileUrl) {
        DownloadResult result = new DownloadResult();
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;

        try {
            // 1. 创建URL对象
            URL url = new URL(fileUrl);
            
            // 2. 打开连接
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);  // 连接超时30秒
            connection.setReadTimeout(60000);     // 读取超时60秒
            connection.setRequestProperty("User-Agent", 
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            
            // 3. 获取响应码
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                result.setSuccess(false);
                result.setMessage("HTTP请求失败，响应码: " + responseCode);
                return result;
            }

            // 4. 获取文件信息
            String contentType = connection.getContentType();
            int contentLength = connection.getContentLength();
            String fileName = getFileNameFromUrl(fileUrl, connection);

            // 5. 读取文件流到字节数组
            inputStream = connection.getInputStream();
            byteArrayOutputStream = new ByteArrayOutputStream();
            
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            
            byte[] fileBytes = byteArrayOutputStream.toByteArray();

            // 6. 转换为Base64字符串
            String base64String = Base64.getEncoder().encodeToString(fileBytes);

            // 7. 创建新的文件流（从字节数组创建）
            ByteArrayInputStream fileStream = new ByteArrayInputStream(fileBytes);

            // 8. 设置结果
            result.setSuccess(true);
            result.setMessage("下载成功");
            result.setFileName(fileName);
            result.setContentType(contentType);
            result.setFileSize(contentLength);
            result.setFileStream(fileStream);
            result.setFileBytes(fileBytes);
            result.setBase64String(base64String);

        } catch (Exception e) {
            result.setSuccess(false);
            result.setMessage("下载失败: " + e.getMessage());
            e.printStackTrace();
        } finally {
            // 关闭连接和流
            try {
                if (byteArrayOutputStream != null) byteArrayOutputStream.close();
                if (inputStream != null) inputStream.close();
                if (connection != null) connection.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    /**
     * 从URL或响应头中获取文件名
     */
    private static String getFileNameFromUrl(String fileUrl, HttpURLConnection connection) {
        // 先从响应头获取
        String disposition = connection.getHeaderField("Content-Disposition");
        if (disposition != null && disposition.contains("filename=")) {
            int index = disposition.indexOf("filename=");
            String fileName = disposition.substring(index + 9);
            return fileName.replaceAll("\"", "").trim();
        }
        
        // 从URL中提取
        String urlPath = fileUrl;
        if (urlPath.contains("?")) {
            urlPath = urlPath.substring(0, urlPath.indexOf("?"));
        }
        if (urlPath.contains("/")) {
            urlPath = urlPath.substring(urlPath.lastIndexOf("/") + 1);
        }
        
        return urlPath.isEmpty() ? "downloaded_file" : urlPath;
    }

    /**
     * 下载结果类
     */
    public static class DownloadResult {
        private boolean success;
        private String message;
        private String fileName;
        private String contentType;
        private int fileSize;
        private InputStream fileStream;      // 文件输入流
        private byte[] fileBytes;            // 文件字节数组
        private String base64String;         // Base64字符串

        // Getter和Setter
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public String getContentType() { return contentType; }
        public void setContentType(String contentType) { this.contentType = contentType; }

        public int getFileSize() { return fileSize; }
        public void setFileSize(int fileSize) { this.fileSize = fileSize; }

        public InputStream getFileStream() { return fileStream; }
        public void setFileStream(InputStream fileStream) { this.fileStream = fileStream; }

        public byte[] getFileBytes() { return fileBytes; }
        public void setFileBytes(byte[] fileBytes) { this.fileBytes = fileBytes; }

        public String getBase64String() { return base64String; }
        public void setBase64String(String base64String) { this.base64String = base64String; }

        @Override
        public String toString() {
            return "DownloadResult{" +
                    "success=" + success +
                    ", message='" + message + '\'' +
                    ", fileName='" + fileName + '\'' +
                    ", contentType='" + contentType + '\'' +
                    ", fileSize=" + fileSize +
                    ", base64Length=" + (base64String != null ? base64String.length() : 0) +
                    '}';
        }
    }

    // ==================== 测试方法 ====================

    public static void main(String[] args) {
        String fileUrl = "https://doc.erp321.com/download/wto4agaM/2_2_815093f4636541a7aea5d6be315e1185?OrderId=11712625_32271285_CNG00824249048068.pdf";

        System.out.println("开始下载文件...");
        System.out.println("URL: " + fileUrl);
        System.out.println("========================================");

        DownloadResult result = downloadFile(fileUrl);

        if (result.isSuccess()) {
            System.out.println("✅ 下载成功！");
            System.out.println("文件名: " + result.getFileName());
            System.out.println("文件类型: " + result.getContentType());
            System.out.println("文件大小: " + result.getFileSize() + " bytes");
            System.out.println("Base64长度: " + result.getBase64String().length() + " 字符");
            System.out.println("========================================");
            
            // 输出文件流信息
            System.out.println("文件流可用: " + (result.getFileStream() != null));
            System.out.println("字节数组长度: " + result.getFileBytes().length + " bytes");
            System.out.println("文件流: " + (result.getFileStream().toString()));
            
            // 输出Base64字符串（前200个字符）
            System.out.println("========================================");
            System.out.println("Base64字符串（前200字符）:");
            String base64 = result.getBase64String();
            System.out.println(base64);
            
        } else {
            System.out.println("❌ 下载失败: " + result.getMessage());
        }
    }
}
