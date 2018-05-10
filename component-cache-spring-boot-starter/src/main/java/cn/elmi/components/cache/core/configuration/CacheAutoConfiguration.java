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

package cn.elmi.components.cache.core.configuration;

import java.net.URL;

import cn.elmi.components.cache.CacheChannel;
import cn.elmi.components.cache.CacheException;
import cn.elmi.components.cache.core.props.RedisProp;
import cn.elmi.components.cache.serializer.FstSerializer;
import cn.elmi.components.cache.serializer.JavaSerializer;
import cn.elmi.components.cache.serializer.Serializer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import cn.elmi.components.cache.core.props.CacheProp;

import lombok.extern.slf4j.Slf4j;
import net.sf.ehcache.CacheManager;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisSentinelPool;
import redis.clients.util.Pool;

/**
 * @author Arthur
 * @since 1.0
 */
@Slf4j
@Configuration
@EnableConfigurationProperties({ CacheProp.class, RedisProp.class })
public class CacheAutoConfiguration {

    @Autowired
    private CacheProp cacheProp;

    @Autowired
    private RedisProp redisProp;

    @Bean(destroyMethod = "close")
    @ConditionalOnMissingBean(CacheChannel.class)
    public Pool<Jedis> pool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(redisProp.getMaxTotal());
        poolConfig.setMaxIdle(redisProp.getMaxIdle());
        poolConfig.setMaxWaitMillis(redisProp.getMaxWaitMillis());

        String pwd = StringUtils.isEmpty(redisProp.getPasswd()) ? null : redisProp.getPasswd();
        return StringUtils.isEmpty(redisProp.getMaster())
                ? new JedisPool(poolConfig, redisProp.getHost(), redisProp.getPort(), redisProp.getTimeout(), pwd,
                        redisProp.getDbIndex())
                : new JedisSentinelPool(redisProp.getMaster(), redisProp.getNodes(), poolConfig, redisProp.getTimeout(),
                        pwd, redisProp.getDbIndex());
    }

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnMissingBean
    public CacheManager ehcacheManager() {
        // TODO
        String xml = "/components/cache/ehcache.xml";

        URL url = getClass().getClassLoader().getParent().getResource(xml);
        if (url == null) {
            url = getClass().getResource(xml);
        }
        if (url == null) {
            throw new CacheException("cannot find ehcache.xml !!!");
        }
        log.debug("component cache ehcacheManager create");
        return CacheManager.create(url);
    }

    @Bean
    @ConditionalOnMissingBean
    public Serializer serializer() {
        return "FST".equals(cacheProp.getSerializer()) ? new FstSerializer() : new JavaSerializer();
    }

}
