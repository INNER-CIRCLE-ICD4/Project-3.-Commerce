# 🔐 Web Security Module

**MSA 환경에서 Gateway 인증 정보를 손쉽게 처리하기 위한 공통 모듈**

Gateway에서 전달되는 사용자 인증 헤더(`X-User-ID`, `X-EMAIL`, `X-ROLES`, `X-AUTH-METHOD`)를 Spring MVC Controller에서 `@CurrentUser` 어노테이션으로 간편하게 사용할 수 있습니다.

## 🚀 **주요 기능**

- ✅ **Gateway 헤더 자동 파싱**: `X-User-ID`, `X-EMAIL`, `X-ROLES`, `X-AUTH-METHOD` 헤더를 자동으로 파싱
- ✅ **@CurrentUser 어노테이션**: Controller 메서드에서 간편한 사용자 정보 주입
- ✅ **타입 안전성**: `AuthenticatedUser` DTO로 타입 안전한 사용자 정보 제공
- ✅ **권한 체크 편의 메서드**: `hasRole()`, `isAdmin()`, `canAccess()` 등 유틸리티 메서드 제공
- ✅ **예외 처리**: 인증 헤더 누락 시 자동 예외 발생

## 📦 **의존성 추가**

### **build.gradle.kts**

```kotlin
dependencies {
    implementation(project(":common:web-security"))
}
```

## 🛠️ **사용 방법**

### **1. Controller에서 사용자 정보 주입**

```java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    @GetMapping("/{userId}")
    public ResponseEntity<UserResponse> getUser(
        @PathVariable Long userId,
        @CurrentUser AuthenticatedUser currentUser) {  // ✅ 자동 주입!
        
        log.info("요청 사용자: {}", currentUser.email());
        
        // 권한 체크
        if (!currentUser.canAccess(userId)) {
            throw new ForbiddenException("접근 권한이 없습니다");
        }
        
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @PostMapping
    public ResponseEntity<UserResponse> createUser(
        @RequestBody CreateUserRequest request,
        @CurrentUser AuthenticatedUser currentUser) {
        
        // 역할 기반 권한 체크
        if (!currentUser.hasRole("ADMIN")) {
            throw new ForbiddenException("관리자만 사용자를 생성할 수 있습니다");
        }
        
        return ResponseEntity.ok(userService.createUser(request, currentUser));
    }
}
```

### **2. AuthenticatedUser 활용**

```java
@CurrentUser AuthenticatedUser user

// 📋 기본 정보
user.userId()      // Long: 사용자 ID
user.email()       // String: 이메일
user.roles()       // List<String>: 역할 목록
user.authMethod()  // String: 인증 방법 (JWT 등)

// 🎯 편의 메서드
user.hasRole("ADMIN")           // 특정 역할 보유 여부
user.hasAnyRole("ADMIN", "SELLER") // 여러 역할 중 하나라도 보유
user.isAdmin()                  // 관리자 여부
user.isSeller()                 // 판매자 여부  
user.isBuyer()                  // 구매자 여부
user.canAccess(targetUserId)    // 특정 사용자 자원 접근 가능 여부
```

### **3. 선택적 인증 (Optional)**

```java
@GetMapping("/public-info")
public ResponseEntity<String> getPublicInfo(
    @CurrentUser(required = false) AuthenticatedUser currentUser) {  // ✅ 선택적
    
    if (currentUser != null) {
        return ResponseEntity.ok("안녕하세요, " + currentUser.email() + "님!");
    } else {
        return ResponseEntity.ok("익명 사용자입니다.");
    }
}
```

## ⚙️ **Gateway 설정**

### **Gateway에서 전달해야 하는 헤더**

```yaml
# Gateway에서 Backend Service로 전달하는 헤더
X-User-ID: "123456"              # 사용자 ID (Long)
X-EMAIL: "user@example.com"      # 사용자 이메일
X-ROLES: "BUYER,SELLER"          # 역할들 (쉼표로 구분)
X-AUTH-METHOD: "JWT"             # 인증 방법
```

### **Gateway Filter 예시**

```java
// Gateway의 UserContextFilter에서 설정
@Override
public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    
    return exchange.getPrincipal(JwtAuthenticationToken.class)
        .cast(JwtAuthenticationToken.class)
        .flatMap(token -> {
            
            ServerHttpRequest modifiedRequest = exchange.getRequest().mutate()
                .header("X-User-ID", token.getToken().getClaimAsString("sub"))
                .header("X-EMAIL", token.getToken().getClaimAsString("email"))  
                .header("X-ROLES", String.join(",", token.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority).toList()))
                .header("X-AUTH-METHOD", "JWT")
                .build();
                
            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        });
}
```

## 🔧 **설정 (선택사항)**

기본적으로 별도 설정 없이 바로 사용 가능하지만, 필요시 커스터마이징할 수 있습니다.

### **커스텀 ArgumentResolver (고급)**

```java
@Configuration
public class CustomSecurityConfig implements WebMvcConfigurer {

    @Bean
    public GatewayUserArgumentResolver gatewayUserArgumentResolver() {
        // 커스텀 구현
        return new CustomGatewayUserArgumentResolver();
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(gatewayUserArgumentResolver());
    }
}
```

## 🧪 **테스트**

### **Controller 테스트**

```java
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getUserWithAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/123")
                .header("X-User-ID", "123")
                .header("X-EMAIL", "test@example.com")
                .header("X-ROLES", "BUYER")
                .header("X-AUTH-METHOD", "JWT"))
                .andExpect(status().isOk());
    }

    @Test
    void getUserWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/123"))
                .andExpect(status().isUnauthorized());  // 인증 헤더 없음
    }
}
```

### **Integration 테스트**

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class IntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void testWithGatewayHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-ID", "123");
        headers.set("X-EMAIL", "test@example.com");  
        headers.set("X-ROLES", "ADMIN");
        headers.set("X-AUTH-METHOD", "JWT");

        HttpEntity<Void> request = new HttpEntity<>(headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            "/api/users/123", HttpMethod.GET, request, String.class);
            
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

## 🚨 **예외 처리**

### **인증 관련 예외**

```java
// 인증 헤더가 없거나 잘못된 경우
@CurrentUser AuthenticatedUser user  // → UnauthorizedException 발생

// 권한이 없는 경우  
if (!user.canAccess(resourceId)) {
    throw new ForbiddenException("접근 권한이 없습니다");
}
```

### **GlobalExceptionHandler에서 처리**

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("AUTH_001", e.getMessage()));
    }

    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse("AUTH_002", e.getMessage()));
    }
}
```

## 📊 **로깅**

### **디버그 로깅 활성화**

```yaml
# application.yml
logging:
  level:
    innercircle.common: DEBUG
```

### **로그 예시**

```bash
# 정상 동작 시
2024-12-19 10:30:15.123 DEBUG i.c.GatewayUserArgumentResolver : 인증 헤더 - UserId: 123, Email: user@test.com, Roles: BUYER, AuthMethod: JWT
2024-12-19 10:30:15.124 DEBUG i.c.GatewayUserArgumentResolver : 인증된 사용자 생성: AuthenticatedUser[userId=123, email=user@test.com, roles=[BUYER], authMethod=JWT]

# 헤더 누락 시  
2024-12-19 10:30:15.125 WARN  i.c.GatewayUserArgumentResolver : 필수 인증 헤더 누락 - UserId: null, Email: null
```

## ❗ **주의사항**

### **보안 고려사항**

1. **헤더 검증**: Gateway에서 헤더를 설정하므로, Backend Service는 내부 네트워크에서만 접근 가능해야 함
2. **헤더 노출 방지**: 응답 시 인증 헤더가 클라이언트에 노출되지 않도록 Gateway에서 제거 필요
3. **토큰 검증**: Gateway에서 JWT 토큰 검증을 완료한 후 헤더 설정

### **성능 고려사항**

1. **ArgumentResolver는 요청마다 실행**됨 - 가벼운 처리만 수행
2. **로깅 레벨 조절**: 운영환경에서는 DEBUG 로깅 비활성화 권장

## 🐛 **트러블슈팅**

### **문제 1: @CurrentUser가 동작하지 않음**

```bash
원인: ArgumentResolver가 등록되지 않음
해결: 
1. 의존성 확인: implementation(project(":common:web-security"))
2. 패키지 스캔 확인: @SpringBootApplication이 innercircle 패키지에 위치
3. 로그 확인: WebSecurityAutoConfiguration 로딩 여부
```

### **문제 2: 항상 null이 주입됨**

```bash
원인: Gateway 헤더가 전달되지 않음
해결:
1. Gateway Filter 설정 확인
2. 헤더명 정확성 확인 (X-User-ID, X-EMAIL, X-ROLES, X-AUTH-METHOD)
3. curl 테스트로 헤더 전달 확인
```

### **문제 3: UnauthorizedException 발생**

```bash
원인: 필수 헤더 (X-User-ID, X-EMAIL) 누락
해결:
1. Gateway에서 토큰 검증 및 헤더 설정 확인
2. @CurrentUser(required = false) 사용 고려
3. Postman/curl로 헤더 포함 테스트
```


## 📝 **변경 이력**

| 버전 | 날짜         | 작성자 |
|------|------------|-----|
| 1.0.0 | 2025-08-19 | 노성웅 |

---
