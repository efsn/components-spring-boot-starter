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

package cn.elmi.components.cache;

import cn.elmi.components.cache.model.CacheElement;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * @author Arthur
 * @since 1.0
 */
public interface CacheChannel {

    /**
     * 获取缓存中的数据
     *
     * @param region: Cache Region name
     * @param key:    Cache key
     * @return cache object
     */
    <K, V> CacheElement<K, V> get(String region, K key);

    <K, V> CacheElement<K, V> get(String region, K key, Callable<V> call);

    /**
     * 写入缓存
     *
     * @param region: Cache Region name
     * @param key:    Cache key
     * @param value:  Cache value
     */
    <K, V> void put(String region, K key, V value);

    /**
     * 删除缓存
     *
     * @param region: Cache Region name
     * @param key:    Cache key
     */
    <K> void evict(String region, K key);

    /**
     * 批量删除缓存
     *
     * @param region: Cache region name
     * @param keys:   Cache key
     */
    <K> void batchEvict(String region, List<K> keys);

    /**
     * Clear the cache
     *
     * @param region: Cache region name
     */
    void clear(String region) throws CacheException;

    /**
     * Get cache region keys
     *
     * @param region: Cache region name
     * @return key list
     */
    <K> Set<K> keys(String region) throws CacheException;

    /**
     * 关闭到通道的连接
     */
    void close();

}
