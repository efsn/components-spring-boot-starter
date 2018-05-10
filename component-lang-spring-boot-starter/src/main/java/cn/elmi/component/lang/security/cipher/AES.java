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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.Charsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.Charset;
import java.security.Key;
import java.security.NoSuchAlgorithmException;

/**
 * AES Coder<br/>
 * secret key length: 128bit, default: 128 bit<br/>
 * mode: ECB/CBC/PCBC/CTR/CTS/CFB/CFB8 to CFB128/OFB/OBF8 to OFB128<br/>
 * padding: Nopadding/PKCS5Padding/ISO10126Padding/
 *
 * @author Arthur
 * @since 1.0
 */
@Slf4j
public final class AES {
    private static final String KEY_ALGORITHM = "AES";
    private static final String DEFAULT_CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    private static final Charset UTF8 = Charsets.UTF_8;

    /**
     * AES 要求密钥长度为 128
     */
    private int keySize = 128;

    // 算法名称/加密模式/填充方式
    // DES共有四种工作模式-->>ECB：电子密码本模式、CBC：加密分组链接模式、CFB：加密反馈模式、OFB：输出反馈模式

    public static byte[] initSecretKey() {
        //
        KeyGenerator kg = null;
        try {
            kg = KeyGenerator.getInstance(KEY_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            log.error("AES init secret key error", e);
            return new byte[0];
        }

        // 初始化此密钥生成器，确定密钥大小
        kg.init(128);

        // 生成一个密钥
        SecretKey secretKey = kg.generateKey();

        return secretKey.getEncoded();
    }

    private static Key toKey(byte[] key) {
        return new SecretKeySpec(key, KEY_ALGORITHM);
    }

    public static String encrypt(String data, String keyStr) {
        log.debug("Data before AES encrypt: {}", data);
        log.debug("AES encrypt key: {}", keyStr);
        try {
            Key key = toKey(DigestUtils.md5(keyStr));
            String enc = new String(Base64.encodeBase64URLSafe(encrypt(data.getBytes(UTF8), key)), UTF8);
            log.debug("Data after AES encrypt: {}", enc);
            return enc;
        } catch (Exception e) {
            log.error("AES decrypt invalid", e);
            throw new IllegalArgumentException("Data invalid");
        }
    }

    public static byte[] encrypt(byte[] data, Key key) throws Exception {
        return encrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
    }

    public static byte[] encrypt(byte[] data, byte[] key) throws Exception {
        return encrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
    }

    public static byte[] encrypt(byte[] data, byte[] key, String cipherAlgorithm) throws Exception {
        return encrypt(data, toKey(key), cipherAlgorithm);
    }

    public static byte[] encrypt(byte[] data, Key key, String cipherAlgorithm) throws Exception {
        Cipher cipher = Cipher.getInstance(cipherAlgorithm);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public static String decrypt(String data, String keyStr) {
        log.debug("Data before AES decrypt: {}", data);
        log.debug("AES decrypt key: {}", keyStr);
        try {
            Key key = toKey(DigestUtils.md5(keyStr));
            String dec = new String(decrypt(Base64.decodeBase64(data.getBytes(UTF8)), key), UTF8);
            log.debug("Data after AES decrypt: {}", dec);
            return dec;
        } catch (Exception e) {
            log.error("AES decrypt invalid", e);
            throw new IllegalArgumentException("Data invalid");
        }
    }

    public static byte[] decrypt(byte[] data, byte[] key) throws Exception {
        return decrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
    }

    public static byte[] decrypt(byte[] data, Key key) throws Exception {
        return decrypt(data, key, DEFAULT_CIPHER_ALGORITHM);
    }

    public static byte[] decrypt(byte[] data, byte[] key, String cipherAlgorithm) throws Exception {
        return decrypt(data, toKey(key), cipherAlgorithm);
    }

    public static byte[] decrypt(byte[] data, Key key, String cipherAlgorithm) throws Exception {
        Cipher cipher = Cipher.getInstance(cipherAlgorithm);
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public static void main(String[] args) {
        String key = "abcdefgjklqwertyudc1b1q9ao2s0vvs123456789yudc1b1q9ao2s0vvs11c20dc4c192bb838bb0942107a6033123456017f439577838e10165e09e8b5b45c32c";
        String data = "afadasd234waf";
        System.out.println("加密前数据: string:" + data);

        String encryptData = encrypt(data, key);
        System.out.println("加密后数据: base64Str:" + encryptData);

        String decryptData = decrypt(encryptData, key);
        System.out.println("解密后数据: string:" + decryptData);
    }

}
