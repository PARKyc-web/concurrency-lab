package com.parkyc.concurrency.coupon.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CollectionId;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "coupon")
public class Coupon {

    // 일반적인 쇼핑몰 쿠폰 및 게임의 쿠폰 2가지 유형을 다룰 수 있는 쿠폰 시스템
    // 게임의 쿠폰의 경우, 문자열을 입력하면 아이템을 제공하는 방식 동시성과는 거리가 있음
    // 그래서 현재 Concurrency의 Coupon에서 다루는 내용은 일반적인 쇼핑몰에서 다루는 쿠폰 (할인율을 제공)
    // 갯수가 한정되어있고, 받으면 할인율을 제공하는 배달의 민족에서 제공하는 쿠폰과 같은 느낌

    @Id
    @Column(name = "coupon_id", comment = "쿠폰 아이디")
    private Long couponId;

    @Column(name = "coupon_name", comment = "쿠폰 이름")
    private String couponName;

    @Column(name = "discount_rate", comment = "쿠폰 사용시 할인율")
    private Integer discountRate;

    @Column(name = "issued_coupon", comment = "발급된 쿠폰 수량")
    private Long issuedQuantity;

    @Column(name = "max_coupon_quantity", comment = "쿠폰 최대 발행 수량")
    private Long maxCouponQuantity;

    /** 쿠폰 발행 가능 시간, 쿠폰 사용가능 시간 등 좀 더 다양항 컬럼이 있어야 할 듯 */

    @CreatedDate
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // 우선 동시성 문제를 메인으로 다루기 위해서 우선 이정도로 마무리
}
