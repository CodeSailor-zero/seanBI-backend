package com.sean.seanBI.manager;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/12/21
 **/
@SpringBootTest
class RedisLimiterManagerTest {
    @Resource
    private RedisLimiterManager redisLimiterManager;

    @Test
    void doRateLimit() throws InterruptedException {
        String key = "1";
        for (int i = 0; i < 4; i++) {
            redisLimiterManager.doRateLimit(key);
            System.out.println("成功");
        }
        Thread.sleep(1000);
        for (int i = 0; i < 2; i++) {
            redisLimiterManager.doRateLimit(key);
            System.out.println("成功");
        }
    }
}