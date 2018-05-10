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

import java.util.concurrent.ConcurrentHashMap;

import cn.elmi.components.cache.CacheException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import cn.elmi.components.cache.CacheProvider;

import net.sf.ehcache.CacheManager;

/**
 * @author Arthur
 * @since 1.0
 */
@Component("ehcache")
public class EhCacheProvider implements CacheProvider {
    private final static Logger log = LoggerFactory.getLogger(EhCacheProvider.class);

    @Autowired
    private CacheManager manager;
    private ConcurrentHashMap<String, EhCache> map = new ConcurrentHashMap<>();

    @Override
    public String name() {
        return "ehcache";
    }

    @Override
    public EhCache provide(String name) throws CacheException {
        EhCache ehcache = map.get(name);
        if (ehcache == null) {
            try {
                synchronized (map) {
                    ehcache = map.get(name);
                    if (ehcache == null) {
                        net.sf.ehcache.Cache cache = manager.getCache(name);
                        if (cache == null) {
                            log.warn("Could not find configuration [" + name + "]; using defaults.");
                            manager.addCache(name);
                            cache = manager.getCache(name);
                            log.debug("started EHCache region: " + name);
                        }
                        ehcache = new EhCache(cache);
                        map.put(name, ehcache);
                    }
                }
            } catch (net.sf.ehcache.CacheException e) {
                throw new CacheException(e);
            }
        }
        return ehcache;
    }

    /**
     * Callback to perform any necessary cleanup of the underlying cache
     * implementation during SessionFactory.close().
     */
    @Override
    public void close() {
        if (manager != null) {
            manager.shutdown();
            manager = null;
        }
    }

}
