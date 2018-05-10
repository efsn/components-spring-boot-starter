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

import cn.elmi.components.cache.Cache;
import cn.elmi.components.cache.CacheException;
import cn.elmi.components.cache.CacheExpiredListener;
import cn.elmi.components.cache.utils.ApplicationContextUtil;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListenerAdapter;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Arthur
 * @since 1.0
 */
public class EhCache<K, V> extends CacheEventListenerAdapter implements Cache<K, V> {

    private net.sf.ehcache.Cache cache;
    private CacheExpiredListener listener;

    /**
     * Creates a new Hibernate pluggable cache based on a cache name.
     *
     * @param cache The underlying EhCache instance to use.
     */
    public EhCache(net.sf.ehcache.Cache cache) {
        this.cache = cache;
        cache.getCacheEventNotificationService().registerListener(this);
        listener = ApplicationContextUtil.getBean(CacheExpiredListener.class);
    }

    @Override
    public Set<K> keys() throws CacheException {
        return new HashSet<K>(cache.getKeys());
    }

    @Override
    public V getValue(K key) throws CacheException {
        try {
            if (key == null)
                return null;
            else {
                Element element = cache.get(key);
                if (element != null) {
                    return (V) element.getObjectValue();
                }
            }
            return null;
        } catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void put(K key, V value) throws CacheException {
        try {
            Element element = new Element(key, value);
            cache.put(element);
        } catch (Exception e) {
            throw new CacheException(e);
        }

    }

    @Override
    public void evict(K key) throws CacheException {
        try {
            cache.remove(key);
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void evict(List<K> keys) throws CacheException {
        cache.removeAll(keys);
    }

    @Override
    public void clear() throws CacheException {
        try {
            cache.removeAll();
        } catch (IllegalStateException e) {
            throw new CacheException(e);
        } catch (net.sf.ehcache.CacheException e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void close() {
        try {
            cache.getCacheManager().removeCache(cache.getName());
        } catch (Exception e) {
            throw new CacheException(e);
        }
    }

    @Override
    public void notifyElementExpired(Ehcache cache, Element elem) {
        if (listener != null) {
            listener.notifyElementExpired(cache.getName(), elem.getObjectKey());
        }
    }

}