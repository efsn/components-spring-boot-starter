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

package cn.elmi.components.cache.utils;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Component;

import cn.elmi.components.cache.Cache;
import cn.elmi.components.cache.CacheProvider;

/**
 * @author Arthur
 * @since 1.0
 */
@Component
public final class CacheManager implements Closeable {

    @Resource(name = "ehcache")
    private CacheProvider ehcacheProvider;

    @Resource(name = "redis")
    private CacheProvider redisProvider;

    @Resource(name = "guavaCache")
    private CacheProvider guavaCacheProvider;

    public <K, V> Cache<K, V> getCache(int level, String region) {
        return ((level == 1) ? guavaCacheProvider : redisProvider).provide(region);
    }

    /**
     * 获取缓存中的数据
     * 
     * @param level
     *            Cache Level: L1 and L2
     * @param region
     *            Cache region name
     * @param key
     *            Cache key
     * @return Cache object
     */
    public <K, V> V get(int level, String region, K key) {
        if (region != null && key != null) {
            Cache<K, V> cache = getCache(level, region);
            if (cache != null) {
                return cache.getValue(key);
            }
        }
        return null;
    }

    /**
     * 写入缓存
     * 
     * @param level
     *            Cache Level: L1 and L2
     * @param region
     *            Cache region name
     * @param key
     *            Cache key
     * @param value
     *            Cache value
     */
    public <K, V> void set(int level, String region, K key, V value) {
        if (region != null && key != null && value != null) {
            Cache<K, V> cache = getCache(level, region);
            if (cache != null) {
                cache.put(key, value);
            }
        }
    }

    /**
     * 清除缓存中的某个数据
     * 
     * @param level
     *            Cache Level: L1 and L2
     * @param region
     *            Cache region name
     * @param key
     *            Cache key
     */
    public <K, V> void evict(int level, String region, K key) {
        if (region != null && key != null) {
            Cache<K, V> cache = getCache(level, region);
            if (cache != null) {
                cache.evict(key);
            }
        }
    }

    /**
     * 批量删除缓存中的一些数据
     * 
     * @param level
     *            Cache Level： L1 and L2
     * @param region
     *            Cache region name
     * @param keys
     *            Cache keys
     */
    public <K, V> void batchEvict(int level, String region, List<K> keys) {
        if (region != null && keys != null && keys.size() > 0) {
            Cache<K, V> cache = getCache(level, region);
            if (cache != null) {
                cache.evict(keys);
            }
        }
    }

    /**
     * Clear the cache
     * 
     * @param level
     *            Cache level
     * @param region
     *            cache region name
     */
    public <K, V> void clear(int level, String region) {
        Cache<K, V> cache = getCache(level, region);
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * list cache keys
     * 
     * @param level
     *            Cache level
     * @param region
     *            cache region name
     * @return Key List
     */
    public <K, V> Set<K> keys(int level, String region) {
        Cache<K, V> cache = getCache(level, region);
        return (cache != null) ? cache.keys() : null;
    }

    @Override
    public void close() throws IOException {
        redisProvider.close();
        guavaCacheProvider.close();
    }

}
