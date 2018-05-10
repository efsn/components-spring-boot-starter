package cn.elmi.components.cache;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * 
 * @author Arthur
 *
 */
@SpringBootApplication(scanBasePackages = { "com.hnair.components" })
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

}