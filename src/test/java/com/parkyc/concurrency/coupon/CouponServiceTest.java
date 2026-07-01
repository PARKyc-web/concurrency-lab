package com.parkyc.concurrency.coupon;

import com.parkyc.concurrency.coupon.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@RequiredArgsConstructor
@SpringBootTest
public class CouponServiceTest {

    private final CouponService couponService;

    @Test
    public void issueCoupon(){
        /// 단건 쿠폰 발급 테스트
    }

    @Test
    public void issueCouponByBulk(){
        /// 벌크 쿠폰 발급 테스트
    }
}
