package com.yupi.springbootinit.manager;

import com.yupi.springbootinit.common.ErrorCode;
import com.yupi.springbootinit.exception.BusinessException;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author Sean
 * @version 1.0
 * &#064;date 2024/12/21
 **/
@Service
public class RedisLimiterManager {
    @Resource
    private RedissonClient redissonClient;

    /**
     * 限流操作
     * @param key 区分不同的限流器，例如用户的ID等等
     */
    public void doRateLimit(String key) {
//        创建一个 rateLimiter的限流器，每 1 秒 发 2 个限流令牌
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(key);
        rateLimiter.trySetRate(RateType.OVERALL,2,1, RateIntervalUnit.SECONDS);
        //一秒内只能获取一次限流令牌，返回false则表示令牌已经用完了
        boolean result = rateLimiter.tryAcquire(1);
        if (!result) {
            throw new BusinessException(ErrorCode.TOO_MANY_REQUEST);
        }
    }
}
