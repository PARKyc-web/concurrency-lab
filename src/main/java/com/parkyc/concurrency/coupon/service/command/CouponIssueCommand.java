package com.parkyc.concurrency.coupon.service.command;

public record CouponIssueCommand(
    Long couponId
    /// 사용자 정보, UserId
)
{
    /// 쿠폰 발급 Command
}
