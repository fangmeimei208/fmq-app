package com.crypto.controller.express;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.crypto.common.AppUtils;
import com.fasterxml.jackson.databind.JsonNode;
import java.util.*;


/**
 * VOP Token预警定时任务
 * 每天10点、16点分别查询一次：VOP_Token_Alert表过期前后一天的数据，通过企业消息推送群消息
 */
@Component
public class ExpressTokenAlertScheduler {
   
    @Autowired
    private AppUtils appUtils;
    
    private static final Logger logger = LoggerFactory.getLogger(ExpressTokenAlertScheduler.class);

    // 企业微信机器人Webhook地址 发送到 测试群
    // private static final String WECHAT_WEBHOOK_URL = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=b7db80dd-a4f1-4946-bd70-4245058bc2ec";
    // 企业微信机器人Webhook地址 发送到 FLUX-SZ-SC【顾问+技术】群
    private static final String WECHAT_WEBHOOK_URL = "https://qyapi.weixin.qq.com/cgi-bin/webhook/send?key=cd5fbe42-18cd-4bbd-9534-cb7a3e71b8d3";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private RestTemplate restTemplate = new RestTemplate();
    
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 定时任务：每天10点、16点分别执行一次
     * cron表达式：秒 分 时 日 月 周
     */
    //@Scheduled(cron = "0 * * * * ?")
    @Scheduled(cron = "0 0 10,16 * * ?")
    public void checkAndSendAlert() {
    	
        logger.info("========== VOP Token预警定时任务开始 ==========");

        try {
            // 1. 查询VOP_Token_Alert表所有数据失效时间前后2天的数据
            StringBuffer sql = new StringBuffer();
            sql.append("SELECT * FROM VOP_Token_Alert ");
            sql.append("WHERE alert_sent = '0' ");
            sql.append("AND refresh_expires_time >= (UNIX_TIMESTAMP() - 2 * 24 * 3600) * 1000 ");
            sql.append("AND refresh_expires_time <= (UNIX_TIMESTAMP() + 2 * 24 * 3600) * 1000 ");
            sql.append("ORDER BY refresh_expires_time ");
            List<Map<String, Object>> records = jdbcTemplate.queryForList(sql.toString());

            if (records.isEmpty()) {
                logger.info("VOP_Token_Alert表中暂无失效前后2天的数据");
                logger.info("========== VOP Token预警定时任务结束 ==========");
                return;
            }

            logger.info("查询到 {} 条记录，开始逐条发送消息", records.size());

            // 2. 遍历每条记录，组装消息并发送
            int successCount = 0;
            int failCount = 0;

            for (int i = 0; i < records.size(); i++) {
                Map<String, Object> record = records.get(i);
                
                try {
                    // 获取字段值
                    String customerName = getStringValue(record, "customer_name");
                    String shopId = getStringValue(record, "shopId");
                    String platform = getStringValue(record, "platform");
                    String registrantName = getStringValue(record, "registrant_name");
                    String registrantMobile = getStringValue(record, "registrant_mobile");
                    long refreshExpiresTime = Long.parseLong(record.get("refresh_expires_time").toString());// 转成long类型

                    // 格式化北京时间
                    String beijingTime = appUtils.formatDateTime(refreshExpiresTime);

                    // 组装消息内容：您有新的快递事项需要跟进 \n>**相关信息**  \n>项目名称：<font color=\"info\">项目名称</font>  \n>快递平台：@miglioguan  \n>参与者：@miglioguan、@kunliu、@jamdeezhou、@kanexiong、@kisonwang  \n>  \n>会议室：<font color=\"info\">广州TIT 1楼 301</font>  \n>日　期：<font color=\"warning\">2018年5月18日</font>  \n>时　间：<font color=\"comment\">上午9:00-11:00</font>  \n>  \n>请准时参加会议。  \n>  \n>如需修改会议信息，请点击：[修改会议信息](https://work.weixin.qq.com)
                    String content = "Hi " + registrantName + "，您有新的快递事项需要跟进，请及时联系客户重新授权或续订，具体信息如下:\n"
                    		+ "【项目名称】" + customerName + "\n"
                    		+ "【快递平台】" + platform + "\n"
                    		+ "【店铺  ID】" + shopId + "\n"
                    		+ "【失效时间】" + beijingTime ;

                    logger.info("第{}条 - 客户: {}, 手机号: {}, 过期时间: {}", (i + 1), customerName, registrantMobile, beijingTime);

                    // 组装请求报文
                    Map<String, Object> message = buildWechatMessage(content, registrantMobile);

                    // 发送到企业微信并解析响应
                    SendResult result = sendToWechat(message);

                    if (result.isSuccess()) {
                        successCount++;
                        logger.info("✅ 发送成功 - 客户: {}, errcode: {}, errmsg: {}", customerName, result.getErrcode(), result.getErrmsg());
                    } else {
                        failCount++;
                        logger.warn("❌ 发送失败 - 客户: {}, errcode: {}, errmsg: {}", customerName, result.getErrcode(), result.getErrmsg());
                    }

                    // 避免发送过快，每条消息间隔300ms
                    if (i < records.size() - 1) {
                        Thread.sleep(300);
                    }

                } catch (Exception e) {
                    failCount++;
                    logger.error("处理记录时发生异常: {}", e.getMessage(), e);
                }
            }

            logger.info("定时任务执行完成 - 成功: {}, 失败: {}, 总计: {}", successCount, failCount, records.size());

        } catch (Exception e) {
            logger.error("定时任务执行异常: {}", e.getMessage(), e);
        }

        logger.info("========== VOP Token预警定时任务结束 ==========");
    }

    
    /**
     * 构建企业微信消息体
     * {
     *     "msgtype": "text",
     *     "text": {
     *         "content": "消息内容", 
     *         "mentioned_mobile_list": ["registrant_mobile"]
     *     }
     * }
     * 
     * @param content 消息内容
     * @param mobile 要@的手机号
     * @return 消息Map
     */
    public Map<String, Object> buildWechatMessage(String content, String mobile) {
        Map<String, Object> message = new LinkedHashMap<>();
        message.put("msgtype", "text"); 

        Map<String, Object> text = new LinkedHashMap<>();
        text.put("content", content);

        // 设置要@的手机号列表
        List<String> mentionedMobileList = new ArrayList<>();
        if (mobile != null && !mobile.isEmpty()) {
            mentionedMobileList.add(mobile);
        }
        text.put("mentioned_mobile_list", mentionedMobileList);

        message.put("text", text);

        return message;
    }
    

    /**
     * 发送消息到企业微信机器人并解析响应
     * 
     * 响应报文格式：{"errcode":0,"errmsg":"ok"}
     * errcode为0代表成功，非0代表失败
     * 
     * @param message 消息体
     * @return SendResult 发送结果
     */
    public SendResult sendToWechat(Map<String, Object> message) {
        SendResult result = new SendResult();
        
        try {
            // 将消息转为JSON字符串用于日志输出
            String requestJson = objectMapper.writeValueAsString(message);
            logger.debug("发送消息到企业微信: {}", requestJson);

            // 发送POST请求
            String response = restTemplate.postForObject(WECHAT_WEBHOOK_URL, message, String.class);

            logger.debug("企业微信响应: {}", response);

            if (response == null || response.trim().isEmpty()) {
                result.setSuccess(false);
                result.setErrcode(-1);
                result.setErrmsg("企业微信响应为空");
                return result;
            }

            // 解析响应JSON
            JsonNode rootNode = objectMapper.readTree(response);
            
            // 获取errcode字段
            JsonNode errcodeNode = rootNode.get("errcode");
            JsonNode errmsgNode = rootNode.get("errmsg");
            
            int errcode = errcodeNode != null ? errcodeNode.asInt() : -1;
            String errmsg = errmsgNode != null ? errmsgNode.asText() : "未知错误";
            
            result.setErrcode(errcode);
            result.setErrmsg(errmsg);
            
            // 判断errcode是否为0
            if (errcode == 0) {
                result.setSuccess(true);
            } else {
                result.setSuccess(false);
                // 根据errcode输出具体的错误信息
                String errorDetail = getWechatErrorMsg(errcode);
                logger.warn("企业微信返回错误 - errcode: {}, errmsg: {}, 说明: {}", errcode, errmsg, errorDetail);
            }
            
        } catch (Exception e) {
            logger.error("发送企业微信消息失败: {}", e.getMessage(), e);
            result.setSuccess(false);
            result.setErrcode(-1);
            result.setErrmsg("发送异常: " + e.getMessage());
        }
        
        return result;
    }

    
    /**
     * 获取企业微信错误码说明
     */
    public String getWechatErrorMsg(int errcode) {
        switch (errcode) {
            case 0:
                return "请求成功";
            case 93000:
                return "机器人webhook地址不合法";
            case 45009:
                return "接口调用超过限制";
            case 45033:
                return "被限流，请降低调用频率";
            case 48001:
                return "API功能未授权";
            default:
                return "未知错误码: " + errcode;
        }
    }

    
    /**
     * 获取字符串值
     */
    public String getStringValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }
    
    /**
     * 发送结果内部类
     */
    public static class SendResult {
        private boolean success;
        private int errcode;
        private String errmsg;

        public boolean isSuccess() {
            return success;
        }

        public void setSuccess(boolean success) {
            this.success = success;
        }

        public int getErrcode() {
            return errcode;
        }

        public void setErrcode(int errcode) {
            this.errcode = errcode;
        }

        public String getErrmsg() {
            return errmsg;
        }

        public void setErrmsg(String errmsg) {
            this.errmsg = errmsg;
        }
    }
}
