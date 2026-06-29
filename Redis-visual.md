# 선착순 재고 차감 동시성 제어 시각화 데모 — 설계 문서

- **작성일**: 2026-06-29 (구현 계획 확정 내용 반영)
- **상태**: 설계 확정 — 구현 계획 작성 완료
- **목적**: Java 경력직(3~6년) 포트폴리오 / 학습용

---

## 1. 한 줄 정의

> 같은 "선착순 재고 차감"을 **5가지 동시성 제어 방식**으로 실행해 보고,
> 락 획득 / 대기 / 해제 / 오버셀(재고 음수) 발생을 **실시간 애니메이션**으로 관찰하는 데모.

`docker compose up` 한 번으로 어디서든 동일하게 재현 가능한 것이 핵심 목표.

### 포트폴리오 관점의 셀링 포인트
- "개념을 이해하고 있다 + 이해한 것을 직접 구현했다"를 **화면으로** 증명
- 면접 단골 질문("락 대신 큐를 쓴 이유는?", "왜 분산 락인가?")에 **데모 화면으로 답변** 가능
- 환경 세팅 없이 `docker compose up`으로 재현 → 운영/배포 감각 어필

---

## 2. 기술 스택

| 영역 | 선택 | 비고 |
|------|------|------|
| 언어/프레임워크 | Java 21 + Spring Boot 3.3.x | 최신 LTS, 경력직 표준 |
| DB | PostgreSQL 16 | `SELECT ... FOR UPDATE` + 조건부 UPDATE 기반 낙관적 락 |
| 분산 락 | Redis + Redisson (`RLock`) | 분산 락 사실상 표준 |
| 큐 | **Redis 블로킹 큐** (Redisson, Phase 1) | 가볍게 시작. Kafka로 교체는 이후 단계 |
| 부하 생성 | Java 21 **가상 스레드** | N명 동시 구매자를 가볍게 생성 |
| 실시간 중계 | WebSocket (STOMP over SockJS), 토픽 `/topic/events` | 락 상태/재고 이벤트를 프론트로 푸시 |
| 프론트엔드 | Vanilla JS + Canvas | 런타임 외부 의존성 0 (SockJS/STOMP는 vendor 정적 파일) |
| 테스트 | JUnit 5 + **Testcontainers** (Postgres/Redis) | 임베디드 DB 미사용 — 실제 락 동작 검증 |
| 패키징 | Docker Compose | app + postgres + redis 묶음 |
| (이후) 부하측정 | k6 | 정밀 수치 자료 보강용 |

---

## 3. 다섯 가지 동시성 제어 전략

각 전략은 동일한 인터페이스(`StockDeductionStrategy`)를 구현하며, 화면에서 선택 가능.

| # | 전략 | 구현 핵심 | 화면에서 증명되는 것 |
|---|------|-----------|---------------------|
| 1 | **No Lock** | 조회→차감→저장 (락 없음, 손실 갱신 유발) | 동시 요청 시 **오버셀(재고 음수)** — 문제 발생 |
| 2 | **DB 비관적 락** | `SELECT ... FOR UPDATE` | 정합성 OK, 대신 **대기 줄·처리량 저하** |
| 3 | **DB 낙관적 락** | 버전 컬럼 + 조건부 `UPDATE` + 재시도 루프 | 충돌 시 **재시도·실패** |
| 4 | **Redis 분산 락** | Redisson `RLock` | 락 획득/대기, 분산 환경 대비 |
| 5 | **큐 방식** | Redis 블로킹 큐 + 단일 컨슈머 순차 처리 | 요청을 줄 세워 **경합 자체 제거** |

> 스토리 흐름: No Lock(문제) → 비관/낙관 락(정합성·비용) → 분산 락(확장) → 큐(경합 제거).
> "발전 과정"과 "트레이드오프"를 순서대로 보여줄 수 있도록 설계.

#### 핵심 설계 결정 (구현 계획에서 확정)
- **JPA `@Version`을 엔티티에 붙이지 않는다.** 붙이면 No Lock 전략에서도 낙관적 락 예외가 발생해 *오버셀이 재현되지 않기 때문*. 대신 엔티티에 일반 `version` 컬럼을 두고, 낙관적 락 전략만 조건부 `UPDATE ... WHERE version = ?`로 직접 구현한다.
- **동시성 오버셀은 자동화 테스트로 단언하지 않는다.** 본질적으로 비결정적이므로, 테스트는 "올바른 전략(2·3·4·5)은 절대 오버셀하지 않는다(성공 수 = 재고, 최종 재고 = 0)"만 단언하고, 오버셀 발생 자체는 화면에서 라이브로 시연한다.
- **낙관적 락 전략은 `OptimisticAttempt` 별도 빈으로 분리**한다. 같은 빈 내부 메서드 호출 시 `@Transactional` 프록시가 우회되는 함정을 피하기 위함(`REQUIRES_NEW` 재시도가 매번 새 트랜잭션으로 동작해야 함).

---

## 4. 아키텍처 & 데이터 흐름

### 컴포넌트 구성 (각 단일 책임)
- **부하 생성기(LoadGenerator)**: 화면 요청을 받아 가상 사용자 N명을 **가상 스레드**로 동시 실행, 결과 집계(`SimulationResult`)
- **전략(StockDeductionStrategy) 5종 + StrategyRegistry**: 공통 인터페이스, 각자 락/큐 방식으로 재고 차감. 레지스트리가 `StrategyType` → 빈 매핑
- **재고 도메인(Stock + StockRepository)**: 재고 상태와 차감 규칙, 락 쿼리(`findByIdForUpdate`, `deductIfVersionMatches`, `deductAtomic`)
- **이벤트 퍼블리셔(EventPublisher / WebSocketEventPublisher)**: 각 단계 이벤트를 `/topic/events`로 브로드캐스트
- **큐 전략 컨슈머**: Redis 블로킹 큐를 단일 데몬 스레드가 드레인, 결과는 `requestId` 기준 `CompletableFuture`로 호출자에 반환 (Phase 1은 인-JVM, Phase 2에서 Redis 토픽으로 교체해 멀티 인스턴스 대응)
- **프론트 뷰어(index.html)**: 이벤트를 받아 Canvas 애니메이션 + 요약 렌더링

### 흐름
```
[화면] 전략 선택 + 동시 요청 수 N 입력 + "시작" 클릭
  → [백엔드] LoadGenerator: 가상 사용자 N명 동시 실행
  → 각 요청 → 선택된 전략으로 재고 차감 시도
  → 매 이벤트(REQUESTED / LOCK_WAIT / LOCK_ACQUIRED / DEDUCTED /
             LOCK_RELEASED / REJECTED / OVERSOLD)를
       EventPublisher → WebSocket으로 실시간 푸시
  → [화면] Canvas에 요청 노드·락 대기 큐·재고 게이지로 애니메이션
  → 종료 후 요약 패널: 성공/실패/오버셀 수, 소요시간, 최종 재고, 처리량(TPS)
```

### 이벤트 모델 (초안)
```
StockEvent {
  requestId: String        // 가상 사용자 식별
  type: REQUESTED | LOCK_WAIT | LOCK_ACQUIRED | DEDUCTED
        | LOCK_RELEASED | REJECTED | OVERSOLD
  strategy: String         // 어떤 전략으로 실행 중인지
  remainingStock: int      // 이벤트 시점 재고
  timestampMs: long
}
```

---

## 5. 화면 (Vanilla JS + Canvas)

- **컨트롤 패널**: 전략 선택(5종), 동시 요청 수, 초기 재고, 시작/리셋 버튼
- **애니메이션 영역**: 요청 노드들이 락(또는 큐)으로 흐르고, 대기/획득/해제가 색/위치로 표현
- **재고 게이지**: 실시간 감소, 음수가 되면 빨강(오버셀 강조)
- **요약 패널**: 성공/실패/오버셀/소요시간/TPS

> 프론트는 "이벤트를 보여주는" 역할만. 모든 진실의 출처는 백엔드 이벤트 스트림.

---

## 6. 단계적 구현 (Phasing)

### Phase 1 — 단일 인스턴스 (1차 완성품)
- 5개 전략 전부 구현 (큐는 Redis 블로킹 큐 + 단일 컨슈머)
- 부하 생성기 + WebSocket 실시간 이벤트 스트리밍
- Vanilla JS Canvas 애니메이션
- `docker compose`: app + postgres + redis
- **여기까지로 포트폴리오 제출 가능한 완결품**

### Phase 2 — 멀티 인스턴스 + 분산 락의 진가
- 앱 인스턴스 2~3개 + Nginx 로드밸런서
- "JVM 락은 분산 환경에서 깨진다 → Redis 분산 락으로 해결"을 실제 재현
- (선택) 큐를 Redis 블로킹 큐 → Kafka로 교체하는 스토리

### Phase 3 (선택) — 정밀 부하 측정
- k6로 전략별 처리량/응답시간/실패율 측정 → 포트폴리오 수치 자료 보강

---

## 7. 범위 밖 (YAGNI / Non-Goals)
- 사용자 인증/회원 시스템
- 실제 결제/주문 도메인 (재고 차감만 다룸)
- 운영급 모니터링 스택(Prometheus/Grafana) — Phase 3 선택 사항
- 화려한 프론트엔드 프레임워크

---

## 8. 확정된 결정 사항 (구현 계획에서 해소)
- **패키지 구조**: `config` / `domain` / `event` / `strategy` / `load` / `web` 로 책임별 분리 (상세는 구현 계획 File Structure 참고)
- **WebSocket 토픽**: 단일 토픽 `/topic/events`로 통일 (전략별 채널 분리 안 함 — 이벤트에 `strategy` 필드가 있어 구분 가능)
- **부하 생성기 동시성 모델**: Java 21 **가상 스레드**(`Executors.newVirtualThreadPerTaskExecutor()`) 사용

### 남은 선택 사항 (Phase 2 이후)
- 멀티 인스턴스 시 큐 결과 반환을 인-JVM `CompletableFuture` → Redis 토픽으로 교체
- 큐 구현체 Redis 블로킹 큐 → Kafka 전환
- Nginx 로드밸런서 도입 및 JVM 락 vs 분산 락 대비 시연
