package com.parkyc.concurrency.coupon.controller;

import com.parkyc.concurrency.coupon.service.CouponService;
import com.parkyc.concurrency.coupon.service.result.IssueCouponResult;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RequestMapping("/coupon")
@RestController
public class CouponController {

    private final CouponService couponService;

    @PostMapping("/issue")
    public IssueCouponResult issueCoupon(){

        return couponService.issueCoupon();
    }

    @PostMapping("/bulk")
    public String issueCouponByBulk(){

        return "User 정보를 받아서, 사용자가 발급받을 수 있는 모든 쿠폰을 발급한다.";
    }

    @PostMapping("/set-quantity")
    public String settingCouponQuantity(@RequestBody Long couponQuantity){
        couponService.settingCouponQuantity(couponQuantity);

        return "쿠폰 동시성 테스트를 위해서 갯수를 설정한다.";
    }

    @GetMapping("/own-coupon")
    public String findOwnCoupon(){

        return "User 정보를 받아서, 소유하고 있는 쿠폰 목록을 출력한다.";
    }
}
