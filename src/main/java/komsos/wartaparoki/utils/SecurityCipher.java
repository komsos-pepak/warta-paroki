package komsos.wartaparoki.utils;



import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

/**
 * @Author: TCMALTUNKAN - MEHMET ANIL ALTUNKAN
 * @Date: 30.12.2019:09:46, Pzt
 **/
public class SecurityCipher {
    private static SecretKeySpec secretKey;
    private static byte[] key;

    private SecurityCipher() {
        throw new AssertionError("Static!");
    }

    public static void setKey(String securityCipherKey) {
        MessageDigest sha;
        try {
            key = securityCipherKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(String strToEncrypt, String securityCipherKey) {
        if (strToEncrypt == null) return null;

        try {
            setKey(securityCipherKey);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static String decrypt(String strToDecrypt, String securityCipherKey) {
        if (strToDecrypt == null) return null;

        try {
            setKey(securityCipherKey);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            String token = new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
            return token ;
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
