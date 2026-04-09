package com.saas.analytics.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
public class AESUtil {

    private final SecretKeySpec secretKey;

    public AESUtil(@Value("${encryption.key}") String secret) {
        byte[] keyBytes = new byte[16];
        byte[] b = secret.getBytes(StandardCharsets.UTF_8);
        int len = Math.min(b.length, keyBytes.length);
        System.arraycopy(b, 0, keyBytes, 0, len);
        secretKey = new SecretKeySpec(keyBytes, "AES");
    }

    public String encrypt(String strToEncrypt) {
        if (strToEncrypt == null) return null;
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("Error while encrypting: " + e.getMessage());
        }
    }

    public String decrypt(String strToDecrypt) {
        if (strToDecrypt == null) return null;
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Error while decrypting: " + e.getMessage());
        }
    }
}
