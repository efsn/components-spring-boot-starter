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

import com.github.benmanes.caffeine.cache.*;
import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.*;

/**
 * @author Arthur
 * @since 1.0
 */
@Slf4j
public class CaffeineExample {

    public static void main(String[] args) throws Exception {


        String specs = "initialCapacity=2, maximumSize=5";
        Caffeine c = Caffeine.from(specs);

        Caffeine caffeine = Caffeine.newBuilder()
                                    .initialCapacity(2)
                                    .maximumSize(5)
//                                    .maximumWeight(10)
                                    .weakKeys()
//                                    .weakValues()
//                                    .softValues()
                                    .expireAfterAccess(5, TimeUnit.MINUTES)
                                    .expireAfterWrite(1, TimeUnit.SECONDS)
//                                    .refreshAfterWrite(2, TimeUnit.MINUTES)
                                    .recordStats();

        Cache<String, Object> manualCache = caffeine.build();
        manualCache.getIfPresent("hello");

        Object val = manualCache.get("hello", x -> "world of manual");

        log.debug("hello {}", val);

        manualCache.put("Hello", val);

        log.debug("Hello {}", manualCache.getIfPresent("Hello"));

        manualCache.invalidate("Hello");

        log.debug("Hello {}", manualCache.getIfPresent("Hello"));

        ConcurrentMap<String, Object> map = manualCache.asMap();

        log.debug(map.toString());

        Policy<String, Object> policy = manualCache.policy();

        log.debug("=========================================================");


        LoadingCache<String, Object> loadingCache = caffeine.build(k -> "world of loading");
        log.debug("hello {}", loadingCache.get("x"));
        Object o = loadingCache.get("hello", k -> "world of loading get");
        log.debug("hello {}", loadingCache.get("hello"));



        log.debug("=========================================================");
        AsyncLoadingCache<String, Object> asyncLoadingCache = caffeine.buildAsync(k -> "world of async loading");

        CompletableFuture<Object> future = asyncLoadingCache.get("hello", k -> "world of async loading function");
        Object o1 = future.get(1, TimeUnit.MINUTES);

        log.debug("hello {}", o1);

        CompletableFuture<Object> future1 = asyncLoadingCache.get("hello", (k, e) ->  CompletableFuture.supplyAsync(() -> "world of async loading fi"));

        log.debug("hello {}", future1.get(1, TimeUnit.MINUTES));

    }

}
