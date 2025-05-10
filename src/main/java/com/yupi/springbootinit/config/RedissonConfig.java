package com.yupi.springbootinit.config;

import lombok.Data;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/12/21
 **/
@Configuration
@ConfigurationProperties(prefix = "spring.redis")
@Data
class RedissonConfig {

    private Integer port;
    private String host;
    private String password;
    private Integer database;
    private Integer timeout;
    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + host + ":" + port)
                .setDatabase(database)
                .setPassword(password)
                .setTimeout(timeout);
        RedissonClient redissonClient = Redisson.create(config);
        return redissonClient;
    }
}
