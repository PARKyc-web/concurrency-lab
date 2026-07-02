package com.parkyc.concurrency.coupon.service.result;

public record IssueCouponResult(
        boolean result,
        Long remainCoupon,
        String threadName
) {
}
