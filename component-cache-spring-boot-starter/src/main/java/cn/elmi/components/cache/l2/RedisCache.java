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

package cn.elmi.components.cache.l2;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import cn.elmi.components.cache.serializer.Serializer;
import org.springframework.util.CollectionUtils;

import cn.elmi.components.cache.Cache;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

/**
 * @author Arthur
 * @since 1.0
 */
@Slf4j
@Data
public class RedisCache<K, V> implements Cache<K, V> {

    private String region;
    private Pool<Jedis> pool;
    private Serializer serializer;

    public RedisCache(Pool<Jedis> pool, Serializer serializer, String region) {
        this.serializer = serializer;
        this.region = region;
        this.pool = pool;
    }

    private String getKeyName(K key) {
        return region + (key instanceof Number ? ":I:" : key instanceof CharSequence ? ":S:" : ":O:") + key;
    }

    @Override
    public V getValue(K key) {
        try (Jedis cache = pool.getResource()) {
            if (null != key) {
                byte[] b = cache.get(getKeyName(key).getBytes());
                if (b != null) {
                    return (V) serializer.deserialize(b);
                }
            }
        } catch (Exception e) {
            log.error("Error occured when get data from redis", e);
            if (e instanceof IOException || e instanceof NullPointerException) {
                evict(key);
            }
        }
        return null;
    }

    @Override
    public void put(K key, V value) {
        if (value == null) {
            evict(key);
        } else {
            try (Jedis cache = pool.getResource()) {
                cache.set(getKeyName(key).getBytes(), serializer.serialize(value));
            } catch (Exception e) {
                log.error(MessageFormat.format("Put {0} in redis fail", getKeyName(key)), e);
            }
        }
    }

    @Override
    public void evict(K key) {
        try (Jedis cache = pool.getResource()) {
            cache.del(getKeyName(key));
        } catch (Exception e) {
            log.error(MessageFormat.format("Delete {0} from redis fail", getKeyName(key)), e);
        }
    }

    @Override
    public void evict(List<K> keys) {
        if (!CollectionUtils.isEmpty(keys)) {
            try (Jedis cache = pool.getResource()) {
                cache.del(keys.stream().map(k -> getKeyName(k)).toArray(String[]::new));
            } catch (Exception e) {
                log.error(MessageFormat.format("Delete {0} from redis fail", keys), e);
            }
        }
    }

    @Override
    @Deprecated
    public Set<K> keys() {
        return null;
    }

    public Set<String> redisKeys() {
        try (Jedis cache = pool.getResource()) {
            return cache.keys(region + ":*").stream().map(k -> k.substring(region.length() + 3))
                    .collect(Collectors.toSet());
        } catch (Exception e) {
            log.error("Keys of redis fail", e);
        }
        return null;
    }

    @Override
    public void clear() {
        try (Jedis cache = pool.getResource()) {
            String[] keys = cache.keys(region + ":*").toArray(new String[0]);
            cache.del(keys);
        } catch (Exception e) {
            log.error("Clear redis fail", e);
        }
    }

    @Override
    public void close() {
        clear();
    }

    public static String adjustKey(String key) {
        return key == null ? key : key.replaceAll("\\:", "");
    }

}
