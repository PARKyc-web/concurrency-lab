package com.parkyc.concurrency.coupon.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/coupon")
@RestController
public class CouponController {

    @PostMapping("/issue")
    public String issueCoupon(){

        return "User, Coupon 정보를 받아서, 쿠폰을 발급한다.";
    }

    @PostMapping("/bulk")
    public String issueCouponByBulk(){

        return "User 정보를 받아서, 사용자가 발급받을 수 있는 모든 쿠폰을 발급한다.";
    }

    @GetMapping("/own-coupon")
    public String findOwnCoupon(){

        return "User 정보를 받아서, 소유하고 있는 쿠폰 목록을 출력한다.";
    }
}
