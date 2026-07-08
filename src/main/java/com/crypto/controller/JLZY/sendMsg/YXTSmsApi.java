package com.crypto.controller.JLZY.sendMsg;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 玖龙纸业 翼讯通发送短信回传接口 20260611103307000161
 * @author fangm
 *
 */
public class YXTSmsApi {

    public static void main(String[] args) {
        // 接口地址
        String apiUrl = "http://sms.189ek.com/yktsms/send";

        //AppID：y7u9hmHM9A3uXXO69lJoFoSPQfjxH5NS
        //AppKey：y0pLMzs2GRH89PbRAZz6vrvNmRQ9Oig9 
        
        // 参数配置（请替换为您的真实信息）
        String appid = "y7u9hmHM9A3uXXO69lJoFoSPQfjxH5NS";
        String mobile = "18778024113,13169110360";  // 多个号码用英文逗号分隔
        String content = "【玖龙纸业】测试短信";			// 
        String appkey = "y0pLMzs2GRH89PbRAZz6vrvNmRQ9Oig9";

        try {
            // 1. 拼接签名原串：appid + mobile + content + appkey
            String signStr = appid + mobile + content + appkey;
            System.out.println("签名原串: " + signStr);
            
            // 2. MD5加密得到sign（32位小写）
            String sign = md5(signStr);
            System.out.println("计算得到的sign: " + sign);
            
            // 3. content进行URLEncode编码得到msg
            String msg = URLEncoder.encode(content, StandardCharsets.UTF_8.name());
            System.out.println("URLEncode后的msg: " + msg);
            
            // 4. 构建POST请求参数
            StringBuilder postData = new StringBuilder();
            postData.append("appid=").append(URLEncoder.encode(appid, "UTF-8"));
            postData.append("&mobile=").append(URLEncoder.encode(mobile, "UTF-8"));
            postData.append("&msg=").append(msg);  // msg已经编码过，不需要再次编码
            postData.append("&sign=").append(URLEncoder.encode(sign, "UTF-8"));
            
            System.out.println("POST请求参数: " + postData.toString());
            
            // 5. 发送HTTP POST请求
            HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setUseCaches(false);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("Accept", "application/json, text/plain, */*");
            
            // 写入请求体
            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = postData.toString().getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
                os.flush();
            }
            
            // 6. 获取响应
            int responseCode = connection.getResponseCode();
            System.out.println("Response Code: " + responseCode);
            
            // 读取响应内容
            StringBuilder response = new StringBuilder();
            try (BufferedReader br = new BufferedReader(
                    new InputStreamReader(
                        responseCode >= 200 && responseCode < 300 ? 
                        connection.getInputStream() : connection.getErrorStream(),
                        StandardCharsets.UTF_8))) {
                String line;
                while ((line = br.readLine()) != null) {
                    response.append(line);
                }
            }
            
            //成功样例		0,I2606151102473949,成功
            //失败样例		-11,0,短信签名不正确，可用签名列表为：玖龙纸业
            System.out.println("Response: " + response.toString());
            connection.disconnect();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    /**
     * MD5加密，返回32位小写
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
            return hexString.toString().toLowerCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5加密失败", e);
        }
    }
}
