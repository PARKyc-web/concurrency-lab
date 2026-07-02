package com.parkyc.concurrency.coupon.service;

import com.parkyc.concurrency.coupon.domain.Coupon;
import com.parkyc.concurrency.coupon.repository.CouponRepository;
import com.parkyc.concurrency.coupon.service.result.CouponInfo;
import com.parkyc.concurrency.coupon.service.result.IssueCouponResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Service
public class CouponService {

    private static final Long TEST_COUPON_ID = 1L;

    private final CouponRepository couponRepository;
    private final StringRedisTemplate redis;

    public IssueCouponResult issueCoupon(){
        Long remainCoupon = redis.opsForValue().decrement("coupon:quantity");
        if(remainCoupon < 0){
            redis.opsForValue().increment("coupon:quantity");
            return new IssueCouponResult(false, 0L, Thread.currentThread().getName());
        }

        return new IssueCouponResult(true, remainCoupon, Thread.currentThread().getName());
        /// 쿠폰 단건을 발급한다.
    }

    @Transactional
    public IssueCouponResult issueCouponByH2(){
        int updatedCount = couponRepository.increaseIssuedCouponCount(TEST_COUPON_ID);
        if(updatedCount == 0){
            return new IssueCouponResult(false, 0L, Thread.currentThread().getName());
        }

        Coupon coupon = couponRepository.findById(TEST_COUPON_ID).orElseThrow();
        Long remainCoupon = coupon.getMaxCouponQuantity() - coupon.getIssuedQuantity();

        return new IssueCouponResult(true, remainCoupon, Thread.currentThread().getName());
    }


    public List<CouponInfo> issueCouponByBulk(){

        return List.of(new CouponInfo());
        /// 쿠폰을 여러개 발급한다.
    }

    @Transactional
    public void settingCouponQuantity(Long quantity){

        /// Redis에 쿠폰 갯수 설정
        redis.opsForValue().set("coupon:quantity", String.valueOf(quantity));

        /// H2 DB에 쿠폰 갯수 설정
        Coupon coupon = couponRepository.findById(TEST_COUPON_ID)
                .orElseGet(() -> Coupon.create(TEST_COUPON_ID, "동시성 테스트 쿠폰", 10, quantity));
        coupon.resetQuantity(quantity);
        couponRepository.save(coupon);
    }

    public CouponInfo registCoupon(){

        return new CouponInfo();
        /// 쿠폰을 등록하고 쿠폰 정보를 리턴한다.
    }

}
