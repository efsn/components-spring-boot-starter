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
import org.springframework.stereotype.Service;

import cn.elmi.components.cache.Cache;

/**
 * @author Arthur
 * @since 1.0
 */
@Service("null")
public class NullCache<K, V> implements Cache<K, V> {

    @Override
    public V getValue(K key) throws CacheException {
        return null;
    }

    @Override
    public void put(K key, V value) throws CacheException {
    }

    @Override
    public Set<K> keys() throws CacheException {
        return null;
    }

    @Override
    public void evict(K key) throws CacheException {
    }

    @Override
    public void evict(List<K> keys) throws CacheException {
    }

    @Override
    public void clear() throws CacheException {
    }

    @Override
    public void close() throws IOException {

    }

}
