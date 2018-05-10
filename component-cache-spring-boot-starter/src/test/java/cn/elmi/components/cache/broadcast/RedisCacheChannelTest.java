package cn.elmi.components.cache.broadcast;

import cn.elmi.components.cache.CacheChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import cn.elmi.components.cache.Person;

@SpringBootTest
public class RedisCacheChannelTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private CacheChannel channel;

    /**
     * 根据票号查询
     */
    @Test
    public void test_1() {
        String region = "test";
        channel.put(region, "L1", "guava");
        channel.put(region, "L2", "redis");
        channel.put(region, 12, new Person());
        
        Person p = new Person();
        channel.put(region, p, "person");
        System.out.println(channel.get(region, "L1"));
        System.out.println(channel.get(region, "L2"));
        System.out.println(channel.get(region, 12));
        System.out.println(channel.get(region, p));
        channel.keys(region).forEach(System.out::println);
    }

}
