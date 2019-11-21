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

package cn.elmi.components.cache.core.props;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Set;

/**
 * @author Arthur
 * @since 1.0
 */
@Data
@ConfigurationProperties("components.redis")
public class RedisProp {

    private String host;
    private int port;
    private String passwd;
    private int timeout;
    private int dbIndex;
    private PoolProp pool;
    private SentinelProp sentinel;

    public int getMaxTotal() {
        return pool.maxTotal;
    }

    public int getMaxIdle() {
        return pool.maxIdle;
    }

    public long getMaxWaitMillis() {
        return pool.maxWaitMillis;
    }

    public String getMaster() {
        return sentinel.master;
    }

    public Set<String> getNodes() {
        return sentinel.nodes;
    }

    @Data
    public static class PoolProp {
        private int maxTotal;
        private int maxIdle;
        private long maxWaitMillis;
    }

    @Data
    public static class SentinelProp {
        private String master;
        private Set<String> nodes;
    }

}
