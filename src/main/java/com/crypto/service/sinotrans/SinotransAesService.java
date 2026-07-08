package com.crypto.service.sinotrans;

import org.springframework.stereotype.Service;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@Service
public class SinotransAesService {
    
	private static final String ALGORITHM = "AES";
	private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
	
	
	// 加密方法
	public static String encrypt(String input, String key) {
		try {
			SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
			Cipher cipher = Cipher.getInstance(TRANSFORMATION);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			byte[] encryptedBytes = cipher.doFinal(input.getBytes());
			return Base64.getEncoder().encodeToString(encryptedBytes);
		} catch (Exception e) {
			return "";
		}
	}

	// 解密方法
	public static String decrypt(String encryptedInput, String key)  {
	    try {
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), ALGORITHM);
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedInput);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes);
        }catch (Exception e){
	        return "";
        }

	}
	
	
	public static void main(String[] args) throws Exception {
		
		SinotransAesService aes = new SinotransAesService();		
		String info = "OteUEsc2IIx12lq8BIARoB4Gs7a3eSXLAQvl4OHVBVeSp+lxxKGRP+pW+dGH3jPak6HLWN4aD7VeK75nq3b/pnnMhaJ0wCrskADAfkfp5uo=";
		String content = "{\"appId\":\"CommAss_ToC\",\"buyNum\":\"1\",\"expressAccount\":\"18930283910\",\"expressAccountType\":\"MOBILE\",\"expressMail\":\"\",\"expressMobile\":\"18930283910\",\"expressName\":\"张三\",\"extendAttrs\":\"skutype=yijifen001,skuvalue=QGJF001004,[S]业务类型=CommAss_ToC,[S]服务充值账号=MOBILE,productId=10000586\",\"orderId\":\"10001\",\"reqMilTime\":\"1730447248199\",\"signData\":\"FA678445E0D5F09A0EBAE3E6822EF746\"} ";
		String key = "6543210123456789";
		System.out.println("原文=" + content);
		String sign = aes.encrypt(content, key);
		System.out.println("加密结果=" + sign);
		System.out.println("解密结果=" + aes.decrypt(info, key));
	}
    
}