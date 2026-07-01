package com.parkyc.concurrency.coupon.repository;

import com.parkyc.concurrency.coupon.domain.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface CouponRepository extends JpaRepository<Coupon, Long> {


    // Query를 통해서 작성한 쿼리는 기본적으로 SELECT라고 판단함.
    // UPDATE 문을 실행하기 때문에 @Modifying 어노테이션을 통해서 수정문이라는 것을 알려준다.
    //
    // 벌크 UPDATE는 영속성 컨텍스트를 거치지 않고 DB에 직접 실행된다.
    // 따라서 쿼리 실행 전 변경 사항을 DB에 반영하기 위해 flushAutomatically = true,
    // 쿼리 실행 후 오래된 엔티티 상태를 제거하기 위해 clearAutomatically = true 설정

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE Coupon
           SET issuedQuantity = issuedQuantity + 1
         WHERE issuedQuantity < maxCouponQuantity
           AND couponId = :couponId
    """)
    int increaseIssuedCouponCount(Long couponId);

}
