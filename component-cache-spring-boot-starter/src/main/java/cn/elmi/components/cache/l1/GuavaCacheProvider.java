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
import java.util.concurrent.ConcurrentHashMap;

import cn.elmi.components.cache.CacheExpiredListener;
import cn.elmi.components.cache.core.props.CacheProp;
import cn.elmi.components.cache.utils.ApplicationContextUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import com.google.common.cache.CacheBuilder;
import cn.elmi.components.cache.Cache;
import cn.elmi.components.cache.CacheProvider;

/**
 * @author Arthur
 * @since 1.0
 */
@Component("guavaCache")
@EnableConfigurationProperties(CacheProp.class)
public class GuavaCacheProvider implements CacheProvider {

    private ConcurrentHashMap<String, Cache> caches = new ConcurrentHashMap<>();

    @Autowired
    private CacheProp cacheProp;

    @Override
    public String name() {
        return "guava";
    }

    @Override
    public void close() throws IOException {
        caches.clear();
    }

    @Override
    public <K, V> Cache<K, V> provide(String region) {
        return caches.containsKey(region) ? caches.get(region) : newCache(region);
    }

    public <K, V> Cache<K, V> newCache(final String region) {
        String defaultRegion = "default";
        String regionConf = cacheProp.getRegions()
                .get(cacheProp.getRegions().containsKey(region) ? region : defaultRegion);
        CacheExpiredListener listener = ApplicationContextUtil.getBean(CacheExpiredListener.class);
        Cache<K, V> cache = new GuavaCache<K, V>(region,
                CacheBuilder.from(regionConf).recordStats().removalListener(x -> {
                    listener.notifyElementExpired(region, x.getKey());
                }).build());
        caches.put(region, cache);
        return cache;
    }

}
