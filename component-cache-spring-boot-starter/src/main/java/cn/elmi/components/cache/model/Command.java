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

package cn.elmi.components.cache.model;

import cn.elmi.components.cache.serializer.Serializer;
import cn.elmi.components.cache.utils.ApplicationContextUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.Arrays;
import java.util.Random;

/**
 * 命令消息封装 格式： 第1个字节为命令代码，长度1 [OPT] 第2、3个字节为region长度，长度2 [R_LEN] 第4、N 为
 * region值，长度为 [R_LEN] 第N+1、N+2 为 key 长度，长度2 [K_LEN] 第N+3、M为 key值，长度为 [K_LEN]
 *
 * @author Arthur
 * @since 1.0
 */
@Slf4j
@Data
public class Command<K> {

    private final static int SRC_ID = genRandomSrc(); // 命令源标识，随机生成
    public final static byte OPT_DELETE_KEY = 0x01; // 删除缓存
    public final static byte OPT_CLEAR_KEY = 0x02; // 清除缓存

    private int src;
    private byte operator;
    private String region;
    private K key;
    private static Serializer serializer = ApplicationContextUtil.getBean(Serializer.class);

    private static int genRandomSrc() {
        long ct = System.currentTimeMillis();
        Random rnd_seed = new Random(ct);
        return (int) (rnd_seed.nextInt(10000) * 1000 + ct % 1000);
    }

    public Command(byte o, String r) {
        this.src = SRC_ID;
        this.operator = o;
        this.region = r;
    }

    public Command(byte o, String r, K k) {
        this(o, r);
        this.key = k;
    }

    public byte[] toBuffers() {
        byte[] keyBuffers = null;
        try {
            keyBuffers = serializer.serialize(key);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        int r_len = region.getBytes().length;
        int k_len = keyBuffers.length;

        byte[] buffers = new byte[11 + r_len + k_len];
        int idx = 0;
        System.arraycopy(int2bytes(this.src), 0, buffers, idx, 4);
        idx += 4;
        buffers[idx] = operator;
        idx += 1;
        System.arraycopy(int2bytes(r_len), 0, buffers, idx, 2);
        idx += 2;
        System.arraycopy(region.getBytes(), 0, buffers, idx, r_len);
        idx += r_len;
        System.arraycopy(int2bytes(k_len), 0, buffers, idx, 4);
        idx += 4;
        System.arraycopy(keyBuffers, 0, buffers, idx, k_len);
        return buffers;
    }

    @SuppressWarnings("unchecked")
    public static <K> Command<K> parse(byte[] buffers) {
        Command<K> cmd = null;
        try {
            int idx = 4;
            byte opt = buffers[idx++];
            int r_len = bytes2int(new byte[]{buffers[idx++], buffers[idx++], 0, 0});
            if (r_len > 0) {
                String region = new String(buffers, idx, r_len);
                idx += r_len;
                int k_len = bytes2int(Arrays.copyOfRange(buffers, idx, idx + 4));
                idx += 4;
                if (k_len > 0) {
                    // String key = new String(buffers, idx, k_len);
                    byte[] keyBuffers = new byte[k_len];
                    System.arraycopy(buffers, idx, keyBuffers, 0, k_len);
                    K key = (K) serializer.deserialize(keyBuffers);
                    cmd = new Command<K>(opt, region, key);
                    cmd.src = bytes2int(buffers);
                }
            }
        } catch (Exception e) {
            log.error("Unabled to parse received command.", e);
        }
        return cmd;
    }

    private static byte[] int2bytes(int i) {
        byte[] b = new byte[4];

        b[0] = (byte) (0xff & i);
        b[1] = (byte) ((0xff00 & i) >> 8);
        b[2] = (byte) ((0xff0000 & i) >> 16);
        b[3] = (byte) ((0xff000000 & i) >> 24);

        return b;
    }

    private static int bytes2int(byte[] bytes) {
        int num = bytes[0] & 0xFF;
        num |= ((bytes[1] << 8) & 0xFF00);
        num |= ((bytes[2] << 16) & 0xFF0000);
        num |= ((bytes[3] << 24) & 0xFF000000);
        return num;
    }

    public boolean isLocalCommand() {
        return this.src == SRC_ID;
    }

}
