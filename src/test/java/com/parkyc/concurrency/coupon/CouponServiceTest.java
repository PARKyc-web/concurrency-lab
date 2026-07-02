package com.parkyc.concurrency.coupon;

import com.parkyc.concurrency.coupon.service.CouponService;
import com.parkyc.concurrency.coupon.repository.CouponRepository;
import com.parkyc.concurrency.coupon.service.result.IssueCouponResult;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor(onConstructor_ = @Autowired)
@SpringBootTest
public class CouponServiceTest {

    private final CouponService couponService;
    private final CouponRepository couponRepository;
    private final StringRedisTemplate redis;

    @Test
    public void issueCouponByRedis() throws Exception {

        Long couponQuantity = 1000L;
        /// 쿠폰 갯수 1,000개 세팅
        couponService.settingCouponQuantity(couponQuantity);

        int threadCount = 1500;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch standbyLatch = new CountDownLatch(threadCount);
        List<Future<IssueCouponResult>> futures = new ArrayList<>();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threadCount; i++) {
                futures.add(executor.submit(() -> {
                    standbyLatch.countDown();
                    startLatch.await();
                    return couponService.issueCoupon();
                }));
            }

            standbyLatch.await();
            long startTime = System.nanoTime();
            startLatch.countDown();

            List<IssueCouponResult> success = new ArrayList<>();
            List<IssueCouponResult> fail = new ArrayList<>();
            for (Future<IssueCouponResult> future : futures) {

                if(future.get().result()){
                    success.add(future.get());
                } else {
                    fail.add(future.get());
                }
            }
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            System.out.println("Redis issueCoupon elapsed time: " + elapsedMillis + "ms");

            /// 단건 쿠폰 발급 테스트
            assertThat(success).hasSize(couponQuantity.intValue());
            assertThat(success).allMatch(IssueCouponResult::result);

            assertThat(fail).hasSize(threadCount - couponQuantity.intValue());
            assertThat(fail).allMatch(f -> !f.result());

            assertThat(redis.opsForValue().get("coupon:quantity")).isEqualTo("0");
        }
    }

    @Test
    public void issueCouponByH2() throws Exception {

        Long couponQuantity = 1000L;
        /// 쿠폰 갯수 1,000개 세팅
        couponService.settingCouponQuantity(couponQuantity);

        int threadCount = 1500;
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch standbyLatch = new CountDownLatch(threadCount);
        List<Future<IssueCouponResult>> futures = new ArrayList<>();

        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < threadCount; i++) {
                futures.add(executor.submit(() -> {
                    standbyLatch.countDown();
                    startLatch.await();
                    return couponService.issueCouponByH2();
                }));
            }

            standbyLatch.await();
            long startTime = System.nanoTime();
            startLatch.countDown();

            List<IssueCouponResult> success = new ArrayList<>();
            List<IssueCouponResult> fail = new ArrayList<>();
            for (Future<IssueCouponResult> future : futures) {

                if(future.get().result()){
                    success.add(future.get());
                } else {
                    fail.add(future.get());
                }
            }
            long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
            System.out.println("H2 issueCoupon elapsed time: " + elapsedMillis + "ms");

            /// 단건 쿠폰 발급 테스트
            assertThat(success).hasSize(couponQuantity.intValue());
            assertThat(success).allMatch(IssueCouponResult::result);

            assertThat(fail).hasSize(threadCount - couponQuantity.intValue());
            assertThat(fail).allMatch(f -> !f.result());

            assertThat(couponRepository.findById(1L).orElseThrow().getIssuedQuantity()).isEqualTo(couponQuantity);
        }
    }

    @Test
    public void issueCouponByBulk(){
        /// 벌크 쿠폰 발급 테스트
    }
}
