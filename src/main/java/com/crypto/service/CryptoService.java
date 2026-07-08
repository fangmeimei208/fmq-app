package com.crypto.service;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.security.spec.KeySpec;
import java.util.Base64;

@Service
public class CryptoService {
    
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final String SECRET_KEY_ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int ITERATION_COUNT = 65536;
    private static final int KEY_LENGTH = 256;
    
    /**
     * 加密方法
     * @param plainText 明文
     * @param key 密钥字符串
     * @return Base64编码的密文
     */
    public String encrypt(String plainText, String key) {
        try {
            // 生成随机盐和IV
            byte[] salt = new byte[16];
            byte[] iv = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);
            random.nextBytes(iv);
            
            // 从密钥生成AES密钥
            SecretKey secretKey = getKeyFromPassword(key, salt);
            
            // 创建加密器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);
            
            // 加密
            byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF-8"));
            
            // 组合：salt + iv + 密文
            byte[] combined = new byte[salt.length + iv.length + cipherText.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(iv, 0, combined, salt.length, iv.length);
            System.arraycopy(cipherText, 0, combined, salt.length + iv.length, cipherText.length);
            
            // Base64编码
            return Base64.getEncoder().encodeToString(combined);
        } catch (Exception e) {
            throw new RuntimeException("加密失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解密方法
     * @param cipherText Base64编码的密文
     * @param key 密钥字符串
     * @return 明文
     */
    public String decrypt(String cipherText, String key) {
        try {
            // Base64解码
            byte[] combined = Base64.getDecoder().decode(cipherText);
            
            // 提取salt、iv和密文
            byte[] salt = new byte[16];
            byte[] iv = new byte[16];
            byte[] encryptedData = new byte[combined.length - 32];
            
            System.arraycopy(combined, 0, salt, 0, 16);
            System.arraycopy(combined, 16, iv, 0, 16);
            System.arraycopy(combined, 32, encryptedData, 0, combined.length - 32);
            
            // 从密钥生成AES密钥
            SecretKey secretKey = getKeyFromPassword(key, salt);
            
            // 创建解密器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, ivSpec);
            
            // 解密
            byte[] plainText = cipher.doFinal(encryptedData);
            
            return new String(plainText, "UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("解密失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 从密码生成AES密钥
     */
    private SecretKey getKeyFromPassword(String password, byte[] salt) throws Exception {
        KeySpec spec = new PBEKeySpec(password.toCharArray(), salt, ITERATION_COUNT, KEY_LENGTH);
        SecretKeyFactory factory = SecretKeyFactory.getInstance(SECRET_KEY_ALGORITHM);
        byte[] keyBytes = factory.generateSecret(spec).getEncoded();
        return new SecretKeySpec(keyBytes, "AES");
    }
}