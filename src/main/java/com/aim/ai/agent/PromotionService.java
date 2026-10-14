package com.aim.ai.agent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PromotionService {

    private static final Logger log = LoggerFactory.getLogger(PromotionService.class);

    // 优惠券查询入参
    public record CouponQueryRequest(String merchantId) {}

    // 优惠券查询出参
    public record CouponQueryResponse(String merchantId, List<String> availableCoupons) {}

    // 优惠券发放申请入参
    public record CouponIssueRequest(String merchantId, String couponName) {}

    // 优惠券发放出参
    public record CouponIssueResponse(boolean success, String message, String couponId) {}

    /**
     * 业务方法 1：查询商户可用优惠券
     */
    public CouponQueryResponse queryCoupons(CouponQueryRequest request) {
        log.info("【业务执行】收到商户优惠券查询请求: merchantId={}", request.merchantId());
        // 模拟查库/微服务 RPC
        List<String> coupons = List.of("100减20看播满减券", "500减100大促进阶券");
        return new CouponQueryResponse(request.merchantId(), coupons);
    }

    /**
     * 业务方法 2：发放优惠券
     */
    public CouponIssueResponse issueCoupon(CouponIssueRequest request) {
        log.info("【业务执行】收到商户发券指令: merchantId={}, couponName={}", request.merchantId(), request.couponName());
        // 模拟库存扣减、幂等落库
        String couponId = "CPN-" + System.currentTimeMillis();
        return new CouponIssueResponse(true, "优惠券发放成功", couponId);
    }
}
