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

import java.util.concurrent.ConcurrentHashMap;

import cn.elmi.components.cache.serializer.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.elmi.components.cache.Cache;
import cn.elmi.components.cache.CacheProvider;

import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

/**
 * @author Arthur
 * @since 1.0
 */
@Component("redis")
public class RedisCacheProvider implements CacheProvider {

    @Autowired
    private Pool<Jedis> pool;

    @Autowired
    private Serializer serializer;

    private ConcurrentHashMap<String, RedisCache> map = new ConcurrentHashMap<>();

    @Override
    public String name() {
        return "redis";
    }

    @Override
    public <K, V> Cache<K, V> provide(String region) {
        RedisCache<K, V> cache = map.get(region);
        if (null == cache) {
            cache = new RedisCache<K, V>(pool, serializer, region);
            map.put(region, cache);
        }
        return cache;
    }

    @Override
    public void close() {
        pool.destroy();
    }

}
