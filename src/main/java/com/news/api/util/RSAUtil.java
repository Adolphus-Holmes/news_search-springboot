package com.news.api.util;

import org.apache.tomcat.util.codec.binary.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassName: RSAUtil
 * @Description: RSA加解密工具类
 * 1.
 * 2.
 * @Author: zhongzk 28582157@qq.com
 * @Date: 2020/1/7 2:08 *
 * @Copyright: 字节码团队www.bjsurong.com. All rights reserved.  *
 */
public class RSAUtil {


    private static final int DEFAULT_RSA_KEY_SIZE = 2048;

    private static final String KEY_ALGORITHM = "RSA";

    public static void main(String [] args){
        Map<String,String> result = generateRsaKey(DEFAULT_RSA_KEY_SIZE);
        System.out.println("公钥为：" + result.get("publicKey") );
        System.out.println("私钥为：" + result.get("privateKey"));
        System.out.println(decrypt("KqirJRjHoxdQ6tpcu+7DDCKgfObcYtQ9HqgATuKFCNg4sCezFG6uq6o+jN6xGPdkyJRdygBNtKW99LfBUEG/Vo7RDZ/NS6VvhWfOMaiQgTKivWpEJhbL5er6MnDwAhptDavIRlp3imsC08sAdteGa88Ocp3pgxoxcZG1P3uUpSAEWj6ZySrIMXPdFnq1955HdZyrbjhjOF19aayV7JOxXwCFLAX2iEoCotRHTjWcFaPoTxcZtaBYl0XHXnAL8M343zRGaGHT3xG0qEutyXoqc/82niuAPOvf935nrhjXsOrGxfmOb+YfbKdOJia8cJORt/n1i2bE3s+ZC34w6Anyfw==","MIIEvAIBADANBgkqhkiG9w0BAQEFAASCBKYwggSiAgEAAoIBAQC50EzhzmdrpczRoJc2qObrTH0zqEqU9JqHIsrwcjQe1fIdyz7e74ncT6zqxjEh+sBdXP8fiyfcwwgZyYujGgC+BWffL9ojRyfKr3GX8kChUk3fuHaf079Sppld/3RD1XHC6/F11VRpUlraWaVeqpbqlLZL7Cqbgo1BIzROUC81BGWAfbeWjx4xKkws9YPZmj6p9yW8LUij6Q2350eekcyPENBLCdi9s2g/UpwmX3wObCVDFHoKUF8k47VkdZi/nx32Pv7UHST8VjH4eDtcbUyMBjOVCRRdBEYH2IPqlaE0rFgbVvjOJD3b7bHD9KDB/Ynz8lxMmTCYqGDEKvI8215jAgMBAAECggEAb67qtwJXmRVDwW/T04+7lAwaaNUrVtKamsTn05PhDfOsHgpOlLLHWvssxMQxPeo4eyVtVqlOYVt8X/uLCZKUhaI8J6Lcaz3WhfXjwd9ix1Pv6Js3+aLNKUFnh18Go8sEIJOuQxPhM5jDcOBXDEpegUq3M3UUuvnCKzectcFuTl96Ifvhfq7JoJfy9gUOedAeFKRi782+JA9EjH698ZBP/vkScc7MHxsLmevUmD8FLKUkzHRG1CvWriszN1RdlviGDYvz9q/1k0lOXcCrXnX8AMF8C19h81KnOXX3PPrAXD1ZVQ6Ik11PoNNmFsi2PgfV6uXpE32PE8bSK2evTIkPuQKBgQDr26aAkow5xgrivk2Rgck8BQlRBBuraRPbTK9ufKVHaCe+DScL1qvxt1lq+sBx3c8DykAfTK0H5bwks3SuEtR9HboQd7ttR/rsJ2K2UdcSZe11kCrBaXMYD5bzSgxEvC8JkiIHy3X+coKIE3lMe+vW51paL7M4XXK72ShRcy/5PwKBgQDJrpN11LDNU0E8OczMiHNhdX18g5raUJAH/R4cB9rfMWs1vwtfsDP06peUvytZbV7Q4z4mIqjq+WVIf2gF4+8w8/UwyfeqhD01+nnCNxA1mEEEfO8H3VE58/wtesIdQxBDhIJJrR9tSXaOWbI0xLusVmdHgYsarGv+yLJf3/cN3QKBgGbBIyEQt5bT+Rof3ptt98X2S8DIEWX0OXppRmTUWQ1rCyV5hXFa7T45qvM4m28AO5hjHNyGAzxVkkpLBKJyAbEgqldSUo2htmf8IL1dml2hmnqFfJsW3dW1tQX5VK79bWC2Ea1jrtxT9xphoY74zh6qXGq+LU55J+s/CJmznUrxAoGAKsQrHrxYNWHnHmc/R0vT86GfV2zsxEh7EkaZQOZlghiPnFkDh2nsyo2IURFslsz7Yx4cyqdk3FbAJGxn2X/o8593qb3aF71s4WpYsVdNx5+egd4gLbG/jKNxIQ+748qZw0dw/UCJvnnqeusIP+pLxZKY2e/dWG2hhOh38p6iW70CgYA//wbkHF6DQ8SFx8ATddTk9R0vfBhs96g5F6JS7PPsutYVR2GtW4hsoebgTDiHHLtgV6/UYoGII7P+Pc8qF+8T2Vu7cnVsfQ2W3iVxFJQTyl4X5ZVVKog5gtVMgaQxBJVwZBCQM0SUjD8ZB5du2kmHkhgrpW24ZAKE4lIndBqBCA=="));
    }

    /**
     * 生成RSA 公私钥,可选长度为1025,2048位.
     */
    public static Map<String,String> generateRsaKey(int keySize) {
        Map<String,String> result = new HashMap<>(2);
        try {
            KeyPairGenerator keyPairGen = KeyPairGenerator.getInstance(KEY_ALGORITHM);

            // 初始化密钥对生成器，密钥大小为1024 2048位
            keyPairGen.initialize(keySize, new SecureRandom());
            // 生成一个密钥对，保存在keyPair中
            KeyPair keyPair = keyPairGen.generateKeyPair();
            // 得到公钥字符串
            result.put("publicKey", new String(Base64.encodeBase64(keyPair.getPublic().getEncoded())));
            // 得到私钥字符串
            result.put("privateKey", new String(Base64.encodeBase64(keyPair.getPrivate().getEncoded())));
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * RSA私钥解密
     *
     * @param str        加密字符串
     * @param privateKey 私钥
     * @return 秘文
     * @throws Exception 解密过程中的异常信息
     */
    public static String decrypt(String str, String privateKey) {
        //64位解码加密后的字符串
        byte[] inputByte;
        String outStr = "";
        try {
            inputByte = Base64.decodeBase64(str.getBytes("UTF-8"));
            //base64编码的私钥
            byte[] decoded = Base64.decodeBase64(privateKey);
            RSAPrivateKey priKey = (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(decoded));
            //RSA解密
            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, priKey);
            outStr = new String(cipher.doFinal(inputByte));
        } catch (UnsupportedEncodingException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | InvalidKeySpecException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return outStr;
    }

}