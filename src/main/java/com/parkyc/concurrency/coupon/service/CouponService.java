package com.parkyc.concurrency.coupon.service;

import com.parkyc.concurrency.coupon.repository.CouponRepository;
import com.parkyc.concurrency.coupon.service.result.CouponInfo;
import com.parkyc.concurrency.redis.RedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CouponService {

    private final CouponRepository couponRepository;

    public CouponInfo issueCoupon(){

        return new CouponInfo();
        // 쿠폰 단건을 발급한다.
    }

    public List<CouponInfo> issueCouponByBulk(){

        return List.of(new CouponInfo());
        // 쿠폰을 여러개 발급한다.
    }

    public CouponInfo registCoupon(){
        /// 쿠폰 등록

        return new CouponInfo();
        // 쿠폰을 등록하고 쿠폰 정보를 리턴한다.
    }

}
