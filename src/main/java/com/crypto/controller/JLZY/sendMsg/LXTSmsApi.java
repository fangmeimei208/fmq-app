package com.crypto.controller.JLZY.sendMsg;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 玖龙纸业 乐讯通发送短信回传接口 20260611103307000161
 * 开放平台：http://yun.loktong.com/book/apimd/text_send.html
 * @author fangm
 *
 */
public class LXTSmsApi {

    public static void main(String[] args) {
        // 接口地址
        String apiUrl = "http://www.lokapi.cn/smsUTF8.aspx";

        /**
         *  username:Dgjlzy
			password:13532409252
			token:1b33a195
			templateid:D8BCD301
			param:13169110360|张三|2541
         */
        // 参数配置（请替换为您的真实信息）
        String action = "sendtemplate"; 					// 固定值
        String username = "Dgjlzy";							// 按项目进行调整
        String password = "13532409252"; 					// 原始密码，代码中会自动MD5	按项目进行调整
        String token = "1b33a195";							// 按项目进行调整
        String templateid = "D8BCD301"; 					// 按项目进行调整
        // String templateid = "BB2FCA14"; 					// 按项目进行调整
        String param = "18778024113|张三的验证码是2541"; 		// 按项目进行调整
        String dstime = ""; 								// 空表示立即发送
        String rece = "json";
        long timestamp = System.currentTimeMillis(); // 13位毫秒时间戳

        try {
            // 1. 密码MD5加密（32位大写）
            String passwordMd5 = md5(password);

            /**
             *  2. 拼接签名原串
             *  签名由参数action,username,password,token,timestamp进行MD5加密组成
             *  比如这些值拼接后为action=sendtemplate&username=zhangsan&password=E10ADC3949BA59ABBE56E057F20F883E&token=588aaaaa&timestamp=636949832321055780，
             *  那么就MD5加密这个参数字符串得到结果后作为sign的值sign=96E79218965EB72C92A54
             */
            String signStr = "action=" + action +
                    "&username=" + username +
                    "&password=" + passwordMd5 +
                    "&token=" + token +
                    "&timestamp=" + timestamp;
            String sign = md5(signStr);

            /**
             * 3. 构建POST请求参数
             * action=sendtemplate&username=zhangsan&password=E10ADC3949BA59ABBE56E057F20F883E&token=894gbhy&templateid=638fgths&param=手机号1|参数1|参数2@手机号2|参数1|参数2&rece=json&timestamp=636949832321055780&sign=96E79218965EB72C92A54
             */
            StringBuilder postData = new StringBuilder();
            postData.append("action=").append(URLEncoder.encode(action, "UTF-8"));
            postData.append("&username=").append(URLEncoder.encode(username, "UTF-8"));
            postData.append("&password=").append(URLEncoder.encode(passwordMd5, "UTF-8"));
            postData.append("&token=").append(URLEncoder.encode(token, "UTF-8"));
            postData.append("&templateid=").append(URLEncoder.encode(templateid, "UTF-8"));
            postData.append("&param=").append(URLEncoder.encode(param, "UTF-8"));
            if (dstime != null && !dstime.isEmpty()) {
                postData.append("&dstime=").append(URLEncoder.encode(dstime, "UTF-8"));
            }
            postData.append("&rece=").append(URLEncoder.encode(rece, "UTF-8"));
            postData.append("&timestamp=").append(URLEncoder.encode(String.valueOf(timestamp), "UTF-8"));
            postData.append("&sign=").append(URLEncoder.encode(sign, "UTF-8"));

            // 4. 发送请求
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = postData.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            // 5. 读取响应
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);

            BufferedReader in = new BufferedReader(new InputStreamReader(
                    responseCode >= 200 && responseCode < 300 ? connection.getInputStream() : connection.getErrorStream(),
                    StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                response.append(line);
            }
            in.close();

            System.out.println("Response: " + response.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    
    /**
     * MD5加密，返回32位大写
     */
    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();
            for (byte b : messageDigest) {
                String hex = Integer.toHexString(0xFF & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
