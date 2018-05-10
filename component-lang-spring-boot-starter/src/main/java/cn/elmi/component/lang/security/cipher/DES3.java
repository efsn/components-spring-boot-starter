/**
 * Copyright (c) 2018 Arthur Chan (codeyn@163.com).
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the “Software”), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED “AS IS”, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package cn.elmi.component.lang.security.cipher;

import org.apache.commons.codec.binary.Base64;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import javax.crypto.Cipher;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.security.Key;
import java.security.Security;

/**
 * 对称密钥加密
 *
 * @author Arthur
 * @since 1.0
 */
public class DES3 {

    // 算法名称
    public static final String KEY_ALGORITHM = "desede";

    // 算法名称/加密模式/填充方式
    public static final String CIPHER_ALGORITHM = "desede/CBC/NoPadding";

    public static String des3DecodeCBC(String key, String keyiv, String data) throws Exception {
        return des3DecodeCBC(key.getBytes("UTF-8"), HexString2Bytes(keyiv), data);
    }

    public static String des3DecodeCBC(byte[] key, byte[] keyiv, String data) throws Exception {
        Key deskey = keyGenerator(key);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        IvParameterSpec ips = new IvParameterSpec(keyiv);
        cipher.init(Cipher.DECRYPT_MODE, deskey, ips);
        return new String(cipher.doFinal(Base64.decodeBase64(data)), "UTF-8");
    }

    public static String des3EncodeCBC(String key, String keyiv, String data) throws Exception {
        return des3EncodeCBC(key.getBytes("UTF-8"), HexString2Bytes(keyiv), data.getBytes("UTF-8"));
    }

    public static String des3EncodeCBC(byte[] key, byte[] keyiv, byte[] data) throws Exception {
        Security.addProvider(new BouncyCastleProvider());
        Key deskey = keyGenerator(key);
        Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
        IvParameterSpec ips = new IvParameterSpec(keyiv);
        cipher.init(Cipher.ENCRYPT_MODE, deskey, ips);
        return Base64.encodeBase64String(cipher.doFinal(data));
    }

    public static byte[] HexString2Bytes(String hexstr) {
        byte[] b = new byte[hexstr.length() / 2];
        int j = 0;
        for (int i = 0; i < b.length; i++) {
            char c0 = hexstr.charAt(j++);
            char c1 = hexstr.charAt(j++);
            b[i] = (byte) ((parse(c0) << 4) | parse(c1));
        }
        return b;
    }

    private static Key keyGenerator(byte[] key) throws Exception {
        DESedeKeySpec KeySpec = new DESedeKeySpec(key);
        SecretKeyFactory KeyFactory = SecretKeyFactory.getInstance(KEY_ALGORITHM);
        return ((Key) (KeyFactory.generateSecret(((java.security.spec.KeySpec) (KeySpec)))));
    }

    public static void main(String[] args) throws Exception {
        String key = "abcdefgjklqwertyudc1b1q9ao2s0vvs123456789yudc1b1q9ao2s0vvs11c20dc4c192bb838bb0942107a6033123456017f439577838e10165e09e8b5b45c32c";
        String keyiv = "a1b2c3d4e5f62c3d";
        String data = "XXXXXX0000067127";
        System.out.println("CBC加密解密");
        String str5 = des3EncodeCBC(key, keyiv, data);
        System.out.println(str5);

        String str6 = des3DecodeCBC(key, keyiv, str5);
        System.out.println(str6);

    }

    private static int parse(char c) {
        if (c >= 'a')
            return (c - 'a' + 10) & 0x0f;
        if (c >= 'A')
            return (c - 'A' + 10) & 0x0f;
        return (c - '0') & 0x0f;
    }

}
