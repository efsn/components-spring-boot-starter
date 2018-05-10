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

package cn.elmi.components.cache.l1;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import cn.elmi.components.cache.CacheException;
import cn.elmi.components.cache.Cache;

import lombok.Data;

/**
 * @author Arthur
 * @since 1.0
 */
@Data
public class GuavaCache<K, V> implements Cache<K, V> {

    private String region;
    private final com.google.common.cache.Cache<K, V> cache;

    public GuavaCache(String region, com.google.common.cache.Cache<K, V> cache) {
        this.cache = cache;
    }

    @Override
    public void evict(Object key) {
        cache.invalidate(key);
    }

    @Override
    public void clear() {
        cache.cleanUp();
    }

    @Override
    public Set<K> keys() throws CacheException {
        return cache.asMap().keySet();
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public void evict(List<K> keys) {
        keys.forEach(k -> evict(k));
    }

    @Override
    public void put(K key, V value) {
        cache.put(key, value);
    }

    @Override
    public V getValue(K key) {
        return cache.getIfPresent(key);
    }

}