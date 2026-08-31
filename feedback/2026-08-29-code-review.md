# 부르릉(bureureung-fo) 코드 전체 피드백

- 작성일: 2026-08-29
- 대상 브랜치: `feature/user`
- 리뷰 범위: `src/main`, `src/test`, 빌드/설정 전반
- 리뷰어 관점: 백엔드 10년차 (Spring Boot / JPA / 보안 / 운영)

---

## 0. 지금까지 구현된 기능 정리 (어디까지 했는지)

### 0-1. API까지 열려 있는(= 실제로 호출 가능한) 기능

| 기능 | 엔드포인트 | 흐름 |
|---|---|---|
| 회원가입 | `POST /api/v1/users/signup` | 필수 약관 동의 검증 → 이메일 인증 완료 여부 확인 → 이메일/닉네임 중복 검증 → 비밀번호 BCrypt 암호화 → 회원 저장 → 약관 4종 저장 |
| 이메일 인증코드 발송 | `POST /api/v1/users/email/send` | 가입 이메일 중복 확인 → 6자리 코드 생성(SecureRandom) → Redis 저장(TTL 5분) → 메일 발송, 발송 실패 시 저장분 삭제(보상) |
| 이메일 인증코드 검증 | `POST /api/v1/users/email/verify` | 코드 조회 → 이미 인증됨/코드 불일치 검증 → 인증 처리 후 TTL 30분 연장 |
| 로그인 | `POST /api/v1/auth/login` | 이메일 조회 → 상태(ACTIVE) 확인 → 비밀번호 매칭 → access/refresh 발급 → refresh를 Redis 저장 |
| 토큰 재발급 | `POST /api/v1/auth/refresh` | refresh 파싱 → Redis 저장값과 대조(탈취 감지) → access/refresh 재발급(rotation) |
| 로그아웃 | `POST /api/v1/auth/logout` | Redis refresh 삭제 (`@AuthenticationPrincipal Long userId` 사용) |

### 0-2. 서비스 로직 + 테스트는 있으나 **컨트롤러가 없어 호출 불가**한 기능  → **여기서 작업이 멈춰 있음**

| 기능 | 구현 위치 | 상태 |
|---|---|---|
| 내 회원정보 조회 | `UserService.getProfile(userId)` | 서비스/단위테스트 완료. **UserController에 GET 엔드포인트 없음** |
| 회원정보 수정 | `UserService.updateProfile(userId, request)` | 서비스/단위테스트 완료(약관 변경 이력 저장 포함). **엔드포인트 없음** |
| 비밀번호 재확인(민감 작업 전) | `AuthService.verifyPassword(userId, request)` | 서비스/단위테스트 완료, `VerifyPasswordRequest` DTO 존재. **AuthController에 엔드포인트 없음** |

> 커밋 히스토리(`feat: 회원 정보 수정 service 구현`, `feat: 비밀번호 인증 구현`) 기준으로,
> **"서비스 계층까지 만들고 컨트롤러 연결 직전"**에서 중단된 상태입니다.
> 재개 시 우선순위: ①위 3개 컨트롤러 노출 → ②비밀번호확인→정보수정 플로우 연결 → ③회원 탈퇴.

### 0-3. 흔적만 있고 미구현

- **회원 탈퇴**: `FoUser.withdraw()` (deletedAt + status=DELETED) 메서드만 존재. 서비스/컨트롤러 없음.
- **닉네임 중복확인 단독 API**: `existsByNickname` 레포 메서드만 존재.
- **가게/메뉴/주문/결제/리뷰 도메인**: `ErrorCode`에 에러코드만 선점, 도메인 코드 없음. (배달 서비스 지향으로 보임)

### 0-4. 갖춰진 공통 인프라

전역 예외 처리(`GlobalExceptionHandler`), 공통 응답(`ApiResponse<T>`), JWT 필터 + `SecurityConfig`,
Redis 3종(`RefreshToken`/`EmailVerification`/`PasswordVerification`), JPA Auditing(`BaseEntity`),
P6Spy SQL 로깅, Testcontainers 통합/레포 테스트 인프라, Swagger 의존성(설정 클래스는 없음).

---

## 1. 잘한 점 (Good)

1. **패키지·레이어 구조가 깔끔하다.** `domain/{auth,user}` + `global` 분리, 각 도메인이 controller/service/repository/entity/dto/validation 표준 구조. 신규 도메인 추가 시 그대로 복제 가능한 형태.
2. **엔티티 캡슐화가 교과서적이다.** `@NoArgsConstructor(access = PROTECTED)` + 정적 팩토리(`of`)로 생성 강제, setter 없이 의도가 드러나는 변경 메서드(`withdraw()`, `update()`, `updateIsAgreed()`).
3. **DTO는 record + 정적 팩토리(`of`/`from`) 일관 사용.** 최신 Java 스타일을 잘 따랐고, 응답 조립 책임이 DTO에 모여 있어 컨트롤러/서비스가 얇다.
4. **API 응답·에러 체계가 실무 수준이다.** `ErrorCode` enum에 코드/HTTP status/메시지 일원화, `GlobalExceptionHandler`에서 비즈니스 예외·검증 실패(필드별 errors)·405·`DataIntegrityViolationException`·미처리 예외까지 계층적으로 처리.
5. **테스트 습관이 좋다.** 서비스는 Mockito 단위, 컨트롤러는 `@WebMvcTest`, 레포는 `@DataJpaTest` + Testcontainers, 통합은 `@SpringBootTest` + Testcontainers로 계층별 도구를 정확히 나눴다. 성공/실패 케이스를 모두 커버하고 `RegisterRequestFixture`로 테스트 데이터 중복을 제거했다.
6. **인증 보안 기본기가 있다.** BCrypt, stateless JWT, refresh 토큰 rotation + 저장소 대조로 탈취 감지, 로그인 실패를 `LOGIN_FAILED` 하나로 통일해 이메일 존재 여부 오라클을 막았다.
7. **이메일 인증에 보상 트랜잭션을 적용했다.** 메일 발송 실패 시 저장한 인증 정보를 삭제(`sendVerificationCode`의 try/catch).
8. **동시 가입 경합을 고려했다.** DB unique 제약 + `DataIntegrityViolationException` → 409 매핑으로, 애플리케이션 검증을 통과한 동시 요청도 방어.
9. **약관 모델링이 유연하다.** `Map<TermsType, Boolean>` 입력 + enum에 `required` 플래그와 검증 로직 캡슐화, 변경 이력 테이블(`FoUserTermsHistory`)까지 설계.
10. **최신 스택 채택.** Java 21, `spring.threads.virtual.enabled=true`(가상 스레드), Spring Boot 3.5.x, jjwt 0.13.

---

## 2. 미흡한 점 (개선 필요, 기능은 동작)

1. **구현했으나 노출 안 된 기능 3개(0-2 참조).** 서비스+테스트까지 끝내고 컨트롤러를 안 붙여서, 클라이언트 입장에서는 존재하지 않는 기능이다. 재개 지점.
2. **`UserService`가 auth 도메인 내부를 지나치게 깊게 참조한다.** `EmailVerification`, `EmailVerificationRepository` 등을 import(일부는 **미사용 import**)하고, 비밀번호 인증 토큰 검증 로직을 `UserService.updateProfile` 안에서 직접 수행한다. 도메인 간 결합은 `EmailVerificationService`/`AuthService` 같은 서비스 파사드로만 하고, 토큰 검증·소비는 `AuthService.consumePasswordVerification(userId, token)` 형태로 넘기는 것이 책임상 맞다.
3. **`AuthService`에 트랜잭션 경계가 전혀 없다.** `verifyPassword`는 `userRepository` 조회 + Redis 저장을 하는데 `@Transactional`이 없다. Redis라 JPA 트랜잭션과 무관하더라도, 최소한 "왜 불필요한지"를 명시적으로 판단해야 한다. `UserService`는 잘 붙어 있다.
4. **`updateProfile`이 트랜잭션 커밋 전에 Redis 토큰을 삭제한다.** 이후 DB 커밋이 실패하면 인증 토큰은 이미 사라져, 사용자는 비밀번호 확인부터 다시 해야 한다. 삭제를 커밋 후로 미루거나 실패 시 복구 전략이 필요하다.
5. **`updateProfile`에서 닉네임 변경 시 중복 검사를 하지 않는다.** unique 제약 덕에 500은 아니고 409가 나가지만, 회원가입 경로는 서비스단에서 검증(`existsByNickname`)하고 수정 경로는 안 하니 일관성이 없다.
6. **검증 메시지와 실제 규칙이 불일치한다.** `RegisterRequest`의 비밀번호 정규식은 `(?=.*[a-z])(?=.*\d)(?=.*[!@#$%^&*]).{8,20}` 로 **대문자를 요구하지 않는데** 메시지는 "영문 대소문자 ... 모두 포함". `EmailVerifyRequest`의 `@Size(min=6, max=6)`에는 메시지가 없다.
7. **`RegisterRequest`의 이메일 검증이 중복·과하다.** `@Email` + `@Pattern`(거의 같은 역할) + `@Size(min=5)`. `@Email` 하나로 충분하고, 길이 상한만 별도로 두면 된다.
8. **`JwtProvider`에 죽은 코드가 있다.** 예외 변환을 하지 않는 구버전 `getUserId(String)`가 `validateAndGetUserId(String)`와 공존한다. 미사용이면 제거.
9. **Swagger는 의존성만 추가돼 있고 문서화 어노테이션(`@Tag`, `@Operation`, `@Schema`)이 전무하다.** 컨트롤러가 늘기 전에 방향을 정할 것.
10. **설정 파일 3개에 JWT/JPA 설정이 흩어져 중복·불일치.** 공통값은 `application.yaml`에, 프로파일별 오버라이드만 `application-local.yml`/`application-test.yml`에 두는 원칙으로 정리.
11. **`FoUserTerms`/`FoUserTermsHistory`가 `foUserId`(Long)만 들고 연관관계 매핑이 없다.** 애그리게잇 경계 분리 의도라면 OK지만, 그렇다면 회원 삭제 시 약관 레코드 정합성(고아 레코드)을 관리할 주체가 없다. 탈퇴 구현 시 함께 설계.
12. **테스트 코드 오타/흠집.** `잘묏된`, `쟈발급`, 테스트 문자열 `<UNK>`, `RegisterRequestFixture.createWithPhone` 반환 타입이 `Object`. 사소하지만 리뷰에서 반복 지적될 부분.

---

## 3. 나쁜 점 (위험 / 우선 조치 권장)

1. **`.gitignore`의 `.env .local` 오타(공백 포함).** 원래 `.env.local`을 무시하려던 것으로 보이나 공백 때문에 패턴이 깨져 `.env.local`이 커밋될 수 있다. `DB_PASSWORD`, `JWT_SECRET`, `MAIL_PASSWORD` 노출 위험 → **즉시 수정**하고 `git log --all --full-history -- .env.local` 로 과거 커밋 여부 확인.
2. **JWT 설정 키 불일치로 특정 프로파일에서 기동 불가.** `application.yaml`은 `jwt.access-token-expiration`/`jwt.refresh-token-expiration`인데 `JwtProvider`는 `jwt.access-expiration`/`jwt.refresh-expiration`을 읽는다. 올바른 키는 `application-local.yml`에만 있어서 **local 프로파일에서만 우연히 동작**한다. base yaml의 키 이름을 코드에 맞게 고칠 것.
3. **refresh 토큰에 타입 구분이 없다.** access와 refresh가 서명·클레임 구조가 동일해서, 탈취한 access 토큰을 `/api/v1/auth/refresh`(permitAll)에 넣어도 서명 검증은 통과한다. 지금은 "Redis 저장값과 문자열 일치" 하나가 유일한 방어선이다. refresh 토큰에 `type: "refresh"` 클레임을 넣고 `refresh()`에서 명시적으로 검증할 것.
4. **`JwtAuthenticationFilter`가 에러 응답 JSON을 문자열로 수동 조립한다.** 에러 메시지에 `"`나 `\`가 들어가면 응답이 깨진다. `ObjectMapper` 직렬화 또는 `AuthenticationEntryPoint`/`ExceptionTranslationFilter` 위임으로 바꿀 것.
5. **`RedisConfig`가 자동설정을 덮어써서 `application.yaml`의 Redis `password`가 무시된다.** `@Value`로 host/port만 읽어 `LettuceConnectionFactory`를 수동 생성하기 때문에, 운영 Redis에 AUTH를 걸면 연결이 실패한다. Boot 자동설정이 만든 `RedisConnectionFactory`를 주입받아 커스텀 `RedisTemplate`에만 쓰도록 축소할 것.
6. **CI가 없다.** `.github`에 PR 템플릿만 있다. 테스트를 잘 짜 놓고 자동 실행이 없으면 회귀를 못 잡는다. GitHub Actions로 `./gradlew test`(+ Testcontainers) 파이프라인을 추가.
7. **스키마 마이그레이션 도구가 없다.** `ddl-auto`가 local `create` / base `validate`인데 Flyway·Liquibase가 없어 스키마 형상관리가 안 된다. 테이블이 적은 지금이 도입 비용이 가장 싸다.
8. **로그아웃이 access 토큰을 무효화하지 않는다.** refresh만 지우므로 이미 발급된 access는 만료까지 유효하다. stateless의 의도된 트레이드오프지만, access 만료를 짧게(예: 15~30분) 두고 이 동작을 문서화할 것. 강한 로그아웃이 필요하면 jti 블랙리스트.
9. **`LoginRequest`가 record라 `toString()`에 비밀번호 평문이 포함된다.** 현재 핸들러는 필드값을 로그에 남기지 않지만, 다른 지점에서 요청 객체나 `MethodArgumentNotValidException` 전체를 로깅하면 평문이 노출된다. 민감 필드는 로깅 정책으로 마스킹하거나 `toString` 재정의.
10. **이메일 "인증 완료" 상태가 Redis TTL에만 의존한다.** 인증 후 30분 안에 가입하지 않으면 만료되고, `register` 트랜잭션 도중 만료되는 race도 이론상 존재한다. 인증 완료를 별도 영속 플래그(테이블)로 남기는 편이 견고하다.

---

## 4. 재개 시 추천 순서

1. `.gitignore` `.env.local` 오타 수정 + 시크릿 커밋 이력 점검 (3-1)
2. `application.yaml` JWT 키 이름 정정 (3-2)
3. `GET /api/v1/users/me`(getProfile), 비밀번호 확인 → `PATCH /api/v1/users/me`(updateProfile), `POST /api/v1/auth/password/verify`(verifyPassword) 컨트롤러 노출 (0-2)
4. 회원 탈퇴(`withdraw`) 서비스/컨트롤러 + 약관 레코드 정리 정책 (0-3)
5. GitHub Actions CI(`./gradlew test`) 추가 (3-6)
6. Flyway 도입, `ddl-auto`를 전 프로파일 `validate`로 (3-7)
7. refresh 토큰 타입 클레임, 필터 에러 응답 직렬화, `RedisConfig` 축소 (3-3~3-5)
