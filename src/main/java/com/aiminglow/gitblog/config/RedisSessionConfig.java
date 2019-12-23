package com.aiminglow.gitblog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.session.data.redis.config.annotation.web.http.EnableRedisHttpSession;

/**
 * @ClassName RedisSessionConfig
 * @Description redis session 配置类
 * @Author aiminglow
 */
@Configuration
@EnableRedisHttpSession(maxInactiveIntervalInSeconds = 1800)
public class RedisSessionConfig {
    /**
     * @Description: 这个方法注入的RedisSerializer是为了将存储在redis里面的信息以json形式存储，而不是字节码形式存储，方便查看。
     *
     * 要替换spring-redis-session的template的serializer，是需要注入的Bean的名字和那个包里面的一样。
     * 因为注入点被@Qualifier修饰了，就得对的上名称，必须注入beanName为springSessionDefaultRedisSerializer的bean
     *
     * @Param: []
     * @return: org.springframework.data.redis.serializer.RedisSerializer<java.lang.Object>
     * @Author: aiminglow
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new Jackson2JsonRedisSerializer<>(Object.class);
    }

    // 下面这种写法和上面的写法效果一样。虽然没有使用方法名，但是可以直接设置bean的名字
    /*@Bean("springSessionDefaultRedisSerializer")
    public RedisSerializer<Object> redisSerializer() {
        return new Jackson2JsonRedisSerializer<>(Object.class);
    }*/
}
