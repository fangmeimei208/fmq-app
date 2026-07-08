package com.crypto.controller.sinotrans;

import com.crypto.service.sinotrans.SinotransAesService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/Sinotrans")
public class SinotransController {
    
    @Autowired
    private SinotransAesService sinotransAesService;
    
    /**
     * 加密接口
     */
    @PostMapping("/encrypt")
    public ResponseEntity<Map<String, Object>> encrypt(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String plainText = request.get("plainText");
            String key = request.get("key");
            
            if (plainText == null || plainText.isEmpty()) {
                response.put("success", false);
                response.put("error", "请输入需要加密的报文");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (key == null || key.isEmpty()) {
                response.put("success", false);
                response.put("error", "请输入密钥");
                return ResponseEntity.badRequest().body(response);
            }
            
            String encrypted = sinotransAesService.encrypt(plainText, key);
            response.put("success", true);
            response.put("result", encrypted);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 解密接口
     */
    @PostMapping("/decrypt")
    public ResponseEntity<Map<String, Object>> decrypt(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String cipherText = request.get("cipherText");
            String key = request.get("key");
            
            if (cipherText == null || cipherText.isEmpty()) {
                response.put("success", false);
                response.put("error", "请输入需要解密的密文");
                return ResponseEntity.badRequest().body(response);
            }
            
            if (key == null || key.isEmpty()) {
                response.put("success", false);
                response.put("error", "请输入密钥");
                return ResponseEntity.badRequest().body(response);
            }
            
            String decrypted = sinotransAesService.decrypt(cipherText, key);
            response.put("success", true);
            response.put("result", decrypted);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
