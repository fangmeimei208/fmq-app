package com.crypto.controller.express;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;

import com.crypto.common.AppUtils;
import com.crypto.controller.express.ExpressTokenAlertScheduler.SendResult;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

/**
 * 快递Token预警控制器（使用原生SQL）
 * 	目前支持唯品、菜鸟
 */
@RestController
@RequestMapping("/api/expressToken")
public class ExpressTokenController {

    @Autowired
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private AppUtils appUtils;
    
    @Autowired
    private ExpressTokenAlertScheduler tokenAlert;
    
    /**
     * 保存Token预警数据到MySQL
     * POST /api/expressToken/save
     */
    @PostMapping("/save")
    public ResponseEntity<Map<String, Object>> saveTokenAlert(@RequestBody Map<String, Object> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 获取参数
            String registrantName = (String) request.get("registrantName");
            String registrantPhone = (String) request.get("registrantPhone");
            String customerName = (String) request.get("customerName");
            String shopId = (String) request.get("shopId");
            String platform = (String) request.get("platform");
            // 获取refresh_expires_time（支持多种类型：Integer、Long、String）
            Long refreshExpiresTime = getLongValue(request, "refreshExpiresTime");

            // 参数验证
            if (registrantName == null || registrantName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "登记人姓名不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            if (registrantPhone == null || registrantPhone.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "登记人手机号不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            if (customerName == null || customerName.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "客户名称不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            if (shopId == null || shopId.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "店铺ID不能为空");
                return ResponseEntity.badRequest().body(response);
            }
            if (platform == null || platform.trim().isEmpty()) {
                response.put("success", false);
                response.put("message", "快递平台不能为空");
                return ResponseEntity.badRequest().body(response);
            }

            String refreshExpiresTimeBeijing = appUtils.formatDateTime(refreshExpiresTime);
            // 构造SQL插入语句
            StringBuilder sql = new StringBuilder();
            sql.append("INSERT INTO VOP_Token_Alert");  
            sql.append("(customer_name" );
            sql.append(",shopId");
            sql.append(",platform");
            sql.append(",registrant_name");
            sql.append(",registrant_mobile");
            sql.append(",refresh_expires_time" );
            sql.append(",refresh_expires_time_beijing");
            sql.append(",alert_sent");
            sql.append(",add_time");
            sql.append(",edit_time");
            sql.append(") " );
            sql.append("VALUES ( ?, ?, ?, ?, ?, ?, ?, 0, NOW(), NOW() )" );

            // 使用KeyHolder获取自增ID
            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
                ps.setString(1, customerName.trim());
                ps.setString(2, shopId.trim());
                ps.setString(3, platform.trim());
                ps.setString(4, registrantName.trim());
                ps.setString(5, registrantPhone.trim());
                ps.setLong(6, refreshExpiresTime);
                ps.setString(7, refreshExpiresTimeBeijing); // 格式化后的北京时间
                return ps;
            }, keyHolder);

            
            // 2026-06-11 add by fmq 顾问登记新记录后 通知方美淇
            // 组装消息内容
            String content = registrantName + "登记了一条快递token信息，具体信息如下:\n"
            		+ "【项目名称】" + customerName + "\n"
            		+ "【快递平台】" + platform + "\n"
            		+ "【店铺  ID】" + shopId + "\n" 
            		+ "【失效时间】" + refreshExpiresTimeBeijing ;

            // 组装请求报文
            Map<String, Object> message = tokenAlert.buildWechatMessage(content, "18778024113");
           
            // 发送到企业微信并解析响应
            SendResult result = tokenAlert.sendToWechat(message);

            // TODO
            if (result.isSuccess()) {

            } else {

            }
            
            response.put("success", true);           
            response.put("message", "数据保存成功，已存入MySQL数据库");
            response.put("refreshExpiresTimeBeijing", refreshExpiresTimeBeijing);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            response.put("success", false);
            response.put("message", "保存失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }


    /**
     * 条件查询记录
     * GET /api/expressToken/records/search
     */
    @GetMapping("/records/search")
    public ResponseEntity<Map<String, Object>> searchRecords(
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String shopId,
            @RequestParam(required = false) String registrantMobile,
            @RequestParam(required = false, defaultValue = "id") String sortField,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder) {
        
        Map<String, Object> response = new HashMap<>();
        try {
            // 白名单验证排序字段，防止SQL注入
            Set<String> allowedSortFields = new HashSet<>(Arrays.asList(
                "id", "registrant_name", "registrant_mobile", "customer_name", "shopId", "platform", "refresh_expires_time_beijing"
            ));
            
            if (!allowedSortFields.contains(sortField)) {
                sortField = "id";
            }
            if (!"asc".equalsIgnoreCase(sortOrder) && !"desc".equalsIgnoreCase(sortOrder)) {
                sortOrder = "desc";
            }

            // 构建动态SQL
            StringBuilder sql = new StringBuilder();
            sql.append("SELECT id, customer_name, shopId, platform, registrant_name, registrant_mobile, ");
            sql.append("refresh_expires_time, refresh_expires_time_beijing, alert_sent, alert_sent_time, add_time, edit_time ");
            sql.append("FROM VOP_Token_Alert WHERE 1=1 ");
            
            List<Object> params = new ArrayList<>();
            
            if (customerName != null && !customerName.trim().isEmpty()) {
                sql.append("AND customer_name LIKE ? ");
                params.add("%" + customerName.trim() + "%");
            }
            if (shopId != null && !shopId.trim().isEmpty()) {
                sql.append("AND shopId LIKE ? ");
                params.add("%" + shopId.trim() + "%");
            }
            if (registrantMobile != null && !registrantMobile.trim().isEmpty()) {
                sql.append("AND registrant_mobile = ? ");
                params.add(registrantMobile.trim());
            }
            
            sql.append("ORDER BY ").append(sortField).append(" ").append(sortOrder);

            List<Map<String, Object>> records = jdbcTemplate.queryForList(sql.toString(), params.toArray());

            List<Map<String, Object>> formattedRecords = new ArrayList<>();
            for (int i = 0; i < records.size(); i++) {
                Map<String, Object> record = records.get(i);
                Map<String, Object> formatted = formatRecord(record);
                formatted.put("rowNum", i + 1);
                formattedRecords.add(formatted);
            }

            response.put("success", true);
            response.put("count", formattedRecords.size());
            response.put("data", formattedRecords);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "查询失败：" + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }


    /**
     * 格式化记录中的时间字段
     */
    private Map<String, Object> formatRecord(Map<String, Object> record) {
        Map<String, Object> formatted = new LinkedHashMap<>();
        formatted.put("id", record.get("id"));
        formatted.put("customerName", record.get("customer_name"));
        formatted.put("shopId", record.get("shopId"));
        formatted.put("platform", record.get("platform"));
        formatted.put("registrantName", record.get("registrant_name"));
        formatted.put("registrantMobile", record.get("registrant_mobile"));
        formatted.put("refreshExpiresTimeBeijing", appUtils.formatDateTime(record.get("refresh_expires_time_beijing")));
        formatted.put("alertSent", record.get("alert_sent"));
        formatted.put("alertSentTime", appUtils.formatDateTime(record.get("alert_sent_time")));
        formatted.put("addTime", appUtils.formatDateTime(record.get("add_time")));
        formatted.put("editTime", appUtils.formatDateTime(record.get("edit_time")));
        return formatted;
    }
    
    /**
     * 从Map中安全获取Long值（支持Integer、Long、String类型）
     */
    private Long getLongValue(Map<String, Object> map, String key) {
        Object value = map.get(key);
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.valueOf((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        // 其他类型尝试toString后转换
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
}