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

package cn.elmi.components.cache.broadcast;

import cn.elmi.components.cache.CacheChannel;
import cn.elmi.components.cache.CacheException;
import cn.elmi.components.cache.CacheExpiredListener;
import cn.elmi.components.cache.core.configuration.CacheAutoConfiguration;
import cn.elmi.components.cache.model.CacheElement;
import cn.elmi.components.cache.model.Command;
import cn.elmi.components.cache.utils.CacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPubSub;
import redis.clients.util.Pool;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Arthur
 * @since 1.0
 */
@ConditionalOnBean(Pool.class)
@AutoConfigureAfter(CacheAutoConfiguration.class)
@Slf4j
public class RedisCacheChannel extends JedisPubSub implements CacheExpiredListener, CacheChannel {

    public final static byte L1 = 1;
    public final static byte L2 = 2;

    private static final String COMMAND_CHARSET = "ISO-8859-1";
    private static final String CHANNEL = "redis_channel";

    @Autowired
    private Pool<Jedis> pool;

    @Autowired
    private CacheManager cacheManager;

    @PostConstruct
    public void subscribe() {
        try {
            new ThreadPoolExecutor(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), r -> {
                Thread t = new Thread(Thread.currentThread().getThreadGroup(), r, "cache-channel-subscribe", 0);
                if (t.isDaemon()) {
                    t.setDaemon(false);
                }
                if (t.getPriority() != Thread.NORM_PRIORITY) {
                    t.setPriority(Thread.NORM_PRIORITY);
                }
                return t;
            }).execute(() -> {
                try (Jedis jedis = pool.getResource()) {
                    log.info("Jedis subscribe begin");
                    jedis.subscribe(this, RedisCacheChannel.CHANNEL);
                }
            });

            log.info("Connected to redis channel");
        } catch (Exception e) {
            log.error("Connected to redis channel fail", e);
        }
    }

    @Override
    public <K, V> CacheElement<K, V> get(String region, K key) {
        CacheElement<K, V> obj = new CacheElement<K, V>();
        obj.setRegion(region);
        obj.setKey(key);
        if (region != null && key != null) {
            obj.setValue(cacheManager.get(L1, region, key));
            if (obj.getValue() == null) {
                obj.setValue(cacheManager.get(L2, region, key));
                if (obj.getValue() != null) {
                    obj.setLevel(L2);
                    cacheManager.set(L1, region, key, obj.getValue());
                }
            } else {
                obj.setLevel(L1);
            }
        }
        return obj;
    }

    @Override
    public <K, V> CacheElement<K, V> get(String region, K key, Callable<V> call) {
        CacheElement<K, V> elm = get(region, key);

        if (null == elm.getValue()) {
            try {
                V v = call.call();
                if (null != v) {
                    elm.setValue(v);
                    put(region, key, v);
                }
            } catch (Exception e) {
                log.error("Cache channel call error", e);
            }
        }

        return elm;
    }

    /**
     * 分几种情况<br/>
     * Object obj1 = CacheManager.get(LEVEL_1, region, key);<br/>
     * Object obj2 = CacheManager.get(LEVEL_2, region, key);<br/>
     * 1. L1 和 L2 都没有<br/>
     * 2. L1 有 L2 没有（这种情况不存在，除非是写 L2 的时候失败<br/>
     * 3. L1 没有，L2 有<br/>
     * 4. L1 和 L2 都有<br/>
     */
    @Override
    public <K, V> void put(String region, K key, V value) {
        if (region != null && key != null) {
            if (value == null)
                evict(region, key);
            else {
                sendEvictCmd(region, key);
                cacheManager.set(L1, region, key, value);
                cacheManager.set(L2, region, key, value);
            }
        }
        log.info("write data to cache region={},key={},value={}", region, key, value);
    }

    @Override
    public <K> void evict(String region, K key) {
        cacheManager.evict(L1, region, key);
        cacheManager.evict(L2, region, key);
        sendEvictCmd(region, key);
    }

    @Override
    public <K> void batchEvict(String region, List<K> keys) {
        cacheManager.batchEvict(L1, region, keys);
        cacheManager.batchEvict(L2, region, keys);
        sendBatchEvictCmd(region, keys);
    }

    @Override
    public void clear(String region) throws CacheException {
        cacheManager.clear(L1, region);
        cacheManager.clear(L2, region);
        sendClearCmd(region);
    }

    @Override
    public <K> Set<K> keys(String region) throws CacheException {
        return cacheManager.keys(L1, region);
    }

    @Override
    public <K> void notifyElementExpired(String region, K key) {
        log.debug("Cache data expired, region=" + region + ",key=" + key);

        // 删除二级缓存
        if (key instanceof List) {
            cacheManager.batchEvict(L2, region, (List) key);
        } else {
            cacheManager.evict(L2, region, key);
        }

        // 发送广播
        sendEvictCmd(region, key);
    }

    /**
     * 发送清除缓存的广播命令
     */
    private <K, V> void sendBatchEvictCmd(String region, List<K> key) {
        // 发送广播

        Command<List<K>> cmd = new Command<List<K>>(Command.OPT_DELETE_KEY, region, key);
        try (Jedis jedis = pool.getResource();) {
            jedis.publish(CHANNEL, new String(cmd.toBuffers(), COMMAND_CHARSET));
        } catch (Exception e) {
            log.error("Unable to delete cache,region=" + region + ",key=" + key, e);
        }
    }

    /**
     * 发送清除缓存的广播命令
     */
    private <K, V> void sendEvictCmd(String region, K key) {
        // 发送广播
        Command<K> cmd = new Command<K>(Command.OPT_DELETE_KEY, region, key);
        try (Jedis jedis = pool.getResource();) {
            jedis.publish(CHANNEL, new String(cmd.toBuffers(), COMMAND_CHARSET));
        } catch (Exception e) {
            log.error("Unable to delete cache,region=" + region + ",key=" + key, e);
        }
    }

    /**
     * 发送清除缓存的广播命令
     */
    private <K, V> void sendClearCmd(String region) {
        // 发送广播
        Command<K> cmd = new Command<K>(Command.OPT_CLEAR_KEY, region);
        try (Jedis jedis = pool.getResource();) {
            jedis.publish(CHANNEL, new String(cmd.toBuffers(), COMMAND_CHARSET));
        } catch (Exception e) {
            log.error("Unable to clear cache,region=" + region, e);
        }
    }

    /**
     * 删除一级缓存的键对应内容
     */
    @SuppressWarnings("unchecked")
    protected <K, V> void onDeleteCacheKey(String region, K key) {
        if (key instanceof List) {
            cacheManager.batchEvict(L1, region, (List<K>) key);
        } else {
            cacheManager.evict(L1, region, key);
        }
        log.debug("Received cache evict message, region={}, key={}", region, key);
    }

    /**
     * 清除一级缓存的键对应内容
     */
    protected void onClearCacheKey(String region) {
        cacheManager.clear(L1, region);
        log.debug("Received cache clear message, region={}", region);
    }

    /**
     * 消息接收
     */
    @Override
    public void onMessage(String channel, String message) {
        // 无效消息
        if (message != null && message.length() < 1) {
            log.warn("Message is empty.");
            return;
        }

        try {
            Command<?> cmd = Command.parse(message.getBytes(COMMAND_CHARSET));
            if (cmd == null) {
                return;
            }

            switch (cmd.getOperator()) {
                case Command.OPT_DELETE_KEY:
                    onDeleteCacheKey(cmd.getRegion(), cmd.getKey());
                    break;
                case Command.OPT_CLEAR_KEY:
                    onClearCacheKey(cmd.getRegion());
                    break;
                default:
                    log.warn("Unknown message type = " + cmd.getOperator());
            }
        } catch (Exception e) {
            log.error("Unable to handle received msg", e);
        }
    }

    @Override
    public void close() {
        try {
            cacheManager.close();
        } catch (Exception e) {
            log.error("Cache channel close fail", e);
        }
    }

}
