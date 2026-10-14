package com.aim.ai.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.Collections;
import java.util.UUID;

@Service
public class IdempotentPromotionService {

    private static final Logger log = LoggerFactory.getLogger(IdempotentPromotionService.class);

    /**
     * 生产级 Lua 脚本：只有当 Key 里的 Value 等于传进去的 requestId 时，才执行 DEL 删除；否则返回 0
     * 保证比对与删除的绝对原子性，彻底杜绝“误删别人的锁”
     */
    private static final String UNLOCK_LUA_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
            "    return redis.call('del', KEYS[1]) " +
            "else " +
            "    return 0 " +
            "end";

    private final StringRedisTemplate redisTemplate;

    public IdempotentPromotionService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 发放优惠券入参
     */
    public record CouponIssueRequest(String merchantId, String couponName) {}

    /**
     * 发放优惠券出参
     */
    public record CouponIssueResponse(boolean success, String message, String couponId) {}

    /**
     * 带分布式锁幂等防护与参数自愈纠错的发券工具方法
     *
     * @param request 大模型抽取的入参
     * @return 业务执行结果（或指导大模型自愈纠错的提示）
     */
    public CouponIssueResponse issueCouponWithLock(CouponIssueRequest request) {
        // 1. 参数自愈校验
        if (!StringUtils.hasText(request.merchantId()) || !request.merchantId().matches("^\\d+$")) {
            return new CouponIssueResponse(false, "【参数校验失败】商户ID必须为纯数字，请向用户询问正确的商户ID", null);
        }

        String lockKey = "aim:lock:coupon:" + request.merchantId() + ":" + request.couponName();
        // 2. 加锁关键点：Value 存当前线程唯一的 UUID 签名
        String lockValue = UUID.randomUUID().toString();

        // 3. 尝试抢锁（5 秒过期，SETNX 语义）
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, lockValue, Duration.ofSeconds(5));
        if (Boolean.FALSE.equals(acquired)) {
            return new CouponIssueResponse(false, "【操作频繁】该优惠券正在发放中，请勿重复操作", null);
        }

        try {
            // 4. 执行真实发券业务
            log.info("抢锁成功，执行发券逻辑... 商户: {}, 凭据: {}", request.merchantId(), lockValue);
            String couponId = "CPN-" + UUID.randomUUID().toString().substring(0, 8);
            return new CouponIssueResponse(true, "优惠券发放成功", couponId);
        } finally {
            // 5. 生产级标准释放：执行 Lua 脚本原子比对并安全删除，绝不误删别人的锁！
            DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>(UNLOCK_LUA_SCRIPT, Long.class);
            Long result = redisTemplate.execute(redisScript, Collections.singletonList(lockKey), lockValue);
            if (Long.valueOf(1L).equals(result)) {
                log.info("分布式锁安全释放成功，lockKey: {}", lockKey);
            } else {
                log.warn("锁释放跳过（锁已过期并被他人接管，防误删生效），lockKey: {}", lockKey);
            }
        }
    }
}
