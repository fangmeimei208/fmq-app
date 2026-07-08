package com.crypto.controller.JLZY;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.*;

/**
 * 【玖龙纸业TMS】【DATAHUB V900 P07】TMS对接地磅MOXA卡 sendSocket调试控制器 20260608142237000071
 */
@RestController
@RequestMapping("/api/socket")
public class SocketController {

    private static final Logger logger = LoggerFactory.getLogger(SocketController.class);

    private static final int BUFFER_SIZE = 1024;

    // 存储当前Socket连接
    private Socket currentSocket = null;
    private InputStream currentInputStream = null;
    private OutputStream currentOutputStream = null;

    /**
     * 建立Socket连接
     * POST /api/socket/connect
     */
    @PostMapping("/connect")
    public synchronized ResponseEntity<Map<String, Object>> connect(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String ip = (String) request.get("ip");
            Integer port = (Integer) request.get("port") ;

            // 先断开旧连接
            disconnectInternal();

            // 建立新连接
            Socket socket = new Socket(ip, port);
            socket.setSoTimeout(5000); // 读取超时5秒

            currentSocket = socket;
            currentInputStream = socket.getInputStream();
            currentOutputStream = socket.getOutputStream();

            logger.info("Socket连接成功: {}:{}", ip, port);

            response.put("success", true);
            response.put("message", "连接成功");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("连接失败: {}", e.getMessage());
            disconnectInternal();
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 读取Socket数据
     * POST /api/socket/read
     */
    @PostMapping("/read")
    public synchronized ResponseEntity<Map<String, Object>> read() {
        Map<String, Object> response = new HashMap<>();

        try {
            if (currentSocket == null || currentSocket.isClosed() || !currentSocket.isConnected()) {
                response.put("success", false);
                response.put("message", "连接已断开");
                return ResponseEntity.ok(response);
            }

            InputStream in = currentInputStream;
            byte[] buf = new byte[BUFFER_SIZE];
            int len;

            try {
                len = in.read(buf);
            } catch (java.net.SocketTimeoutException e) {
                // 超时，无数据可读
                response.put("success", true);
                response.put("message", "无数据");
                return ResponseEntity.ok(response);
            }

            if (len <= 0) {
                disconnectInternal();
                response.put("success", false);
                response.put("message", "通道断开");
                return ResponseEntity.ok(response);
            }

            // 构建HEX字符串
            StringBuilder hexSb = new StringBuilder();
            for (int i = 0; i < len; i++) {
                String s = Integer.toHexString(0xff & buf[i]);
                if (s.length() == 1) hexSb.append("0");
                hexSb.append(s).append(" ");
            }

            // 构建原始字符串
            String raw = new String(buf, 0, len, "UTF-8");

            Map<String, String> data = new LinkedHashMap<>();
            data.put("hex", hexSb.toString().trim());
            data.put("raw", raw);

            response.put("success", true);
            response.put("data", data);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("读取异常: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 发送Socket数据
     * POST /api/socket/send
     */
    @PostMapping("/send")
    public synchronized ResponseEntity<Map<String, Object>> send(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            if (currentSocket == null || currentSocket.isClosed()) {
                response.put("success", false);
                response.put("message", "未连接");
                return ResponseEntity.ok(response);
            }

            String message = request.get("message");

            if (message != null && !message.isEmpty()) {
                // 判断是否为HEX格式（只包含0-9 a-f A-F 空格）
                byte[] sendBytes;
                if (message.matches("^[0-9a-fA-F ]+$") && !message.matches("^[a-zA-Z ]+$")) {
                    // HEX格式
                    sendBytes = hexStringToBytes(message.replaceAll(" ", ""));
                } else {
                    // 普通文本
                    sendBytes = message.getBytes("UTF-8");
                }

                currentOutputStream.write(sendBytes);
                currentOutputStream.flush();
            }

            // 发送后等待并读取响应
            Thread.sleep(300);
            byte[] buf = new byte[BUFFER_SIZE];
            int len;
            try {
                len = currentInputStream.read(buf);
            } catch (java.net.SocketTimeoutException e) {
                response.put("success", true);
                response.put("message", "发送成功，无响应数据");
                return ResponseEntity.ok(response);
            }

            if (len > 0) {
                StringBuilder hexSb = new StringBuilder();
                for (int i = 0; i < len; i++) {
                    String s = Integer.toHexString(0xff & buf[i]);
                    if (s.length() == 1) hexSb.append("0");
                    hexSb.append(s).append(" ");
                }
                String raw = new String(buf, 0, len, "UTF-8");

                Map<String, String> data = new LinkedHashMap<>();
                data.put("hex", hexSb.toString().trim());
                data.put("raw", raw);
                response.put("data", data);
            }

            response.put("success", true);
            response.put("message", "发送成功");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            logger.error("发送异常: {}", e.getMessage());
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    /**
     * 断开Socket连接
     * POST /api/socket/disconnect
     */
    @PostMapping("/disconnect")
    public synchronized ResponseEntity<Map<String, Object>> disconnect() {
        disconnectInternal();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "已断开");
        return ResponseEntity.ok(response);
    }

    /**
     * 内部断开连接
     */
    private void disconnectInternal() {
        try { if (currentOutputStream != null) currentOutputStream.close(); } catch (Exception e) {}
        try { if (currentInputStream != null) currentInputStream.close(); } catch (Exception e) {}
        try { if (currentSocket != null) currentSocket.close(); } catch (Exception e) {}
        currentSocket = null;
        currentInputStream = null;
        currentOutputStream = null;
    }

    /**
     * HEX字符串转字节数组
     */
    private byte[] hexStringToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
}