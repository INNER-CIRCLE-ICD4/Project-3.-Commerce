# JWT + Redis 인증 방식

## 개요

JWT 토큰을 Redis와 함께 사용하는 이유와 구현 방법을 설명합니다. JWT의 한계를 보완하고 보안을 강화하는 하이브리드 방식을 다룹니다.

## JWT의 한계점

### 🚨 **1. 토큰 무효화 불가능**
```java
// JWT는 서버에서 무효화할 수 없음
// 토큰이 만료될 때까지 계속 유효
String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...";
// 서버에서 이 토큰을 무효화할 방법이 없음
```

### 🚨 **2. 실시간 권한 변경 불가**
```java
// 사용자 역할이 변경되어도 JWT는 그대로 유효
// 토큰 만료까지 기다려야 함
User user = getUser();
user.setRole("ADMIN"); // 역할 변경
// 하지만 기존 JWT는 여전히 "USER" 역할로 유효
```

### 🚨 **3. 보안 이슈**
```java
// 토큰이 탈취되어도 무효화 불가
// 만료될 때까지 계속 사용 가능
```

## Redis를 사용하는 이유

### ✅ **1. 토큰 무효화 가능**
```java
// 로그아웃 시 토큰 무효화
@Service
public class AuthService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    public void logout(String token) {
        // Redis에서 토큰 제거
        redisTemplate.delete("token:" + token);
        // 또는 블랙리스트에 추가
        redisTemplate.opsForValue().set("blacklist:" + token, "true", Duration.ofHours(1));
    }
}
```

### ✅ **2. 실시간 권한 변경**
```java
// 사용자 권한 변경 시 토큰도 업데이트
@Service
public class UserService {
    
    @Autowired
    private RedisTemplate<String, UserInfo> redisTemplate;
    
    public void updateUserRole(Long userId, String newRole) {
        // 사용자 역할 변경
        userRepository.updateRole(userId, newRole);
        
        // 해당 사용자의 모든 토큰 무효화
        String pattern = "user:" + userId + ":*";
        Set<String> keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
```

### ✅ **3. 토큰 관리 기능**
```java
// 토큰 정보 저장
@Service
public class TokenService {
    
    @Autowired
    private RedisTemplate<String, TokenInfo> redisTemplate;
    
    public void storeToken(String token, UserInfo userInfo) {
        TokenInfo tokenInfo = new TokenInfo(
            userInfo.getId(),
            userInfo.getEmail(),
            userInfo.getRole(),
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(24)
        );
        
        // 토큰 정보를 Redis에 저장
        redisTemplate.opsForValue().set(
            "token:" + token, 
            tokenInfo, 
            Duration.ofHours(24)
        );
        
        // 사용자별 토큰 목록 관리
        redisTemplate.opsForSet().add(
            "user:" + userInfo.getId() + ":tokens", 
            token
        );
    }
}
```

## Redis + JWT 하이브리드 방식

### 🎯 **1. 토큰 발급 시**

#### **Member 서비스**
```java
@Service
public class AuthService {
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private RedisTemplate<String, TokenInfo> redisTemplate;
    
    public LoginResponse login(LoginRequest request) {
        // 사용자 인증
        Member member = memberService.authenticate(request.getEmail(), request.getPassword());
        
        // JWT 토큰 생성
        String token = jwtTokenProvider.generateToken(member);
        
        // Redis에 토큰 정보 저장
        TokenInfo tokenInfo = new TokenInfo(
            member.getId(),
            member.getEmail().getValue(),
            member.getRoles().stream()
                .map(role -> role.getRoleType().name())
                .collect(Collectors.joining(",")),
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(24)
        );
        
        redisTemplate.opsForValue().set(
            "token:" + token, 
            tokenInfo, 
            Duration.ofHours(24)
        );
        
        return new LoginResponse(token, MemberResponse.from(member));
    }
}
```

#### **TokenInfo DTO**
```java
public class TokenInfo {
    private Long userId;
    private String email;
    private String role;
    private LocalDateTime issuedAt;
    private LocalDateTime expiresAt;
    
    // 생성자, getter, setter
}
```

### 🎯 **2. 토큰 검증 시**

#### **Gateway**
```java
@Component
public class JwtAuthenticationFilter implements GatewayFilter {
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Autowired
    private RedisTemplate<String, TokenInfo> redisTemplate;
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String token = extractToken(exchange.getRequest());
        
        if (token != null) {
            // 1. JWT 검증
            if (!jwtTokenProvider.validateToken(token)) {
                return unauthorized(exchange);
            }
            
            // 2. Redis에서 토큰 확인
            TokenInfo tokenInfo = redisTemplate.opsForValue().get("token:" + token);
            if (tokenInfo == null) {
                return unauthorized(exchange);
            }
            
            // 3. 블랙리스트 확인
            Boolean isBlacklisted = redisTemplate.hasKey("blacklist:" + token);
            if (Boolean.TRUE.equals(isBlacklisted)) {
                return unauthorized(exchange);
            }
            
            // 4. 요청 헤더에 사용자 정보 추가
            ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-User-Id", tokenInfo.getUserId().toString())
                .header("X-User-Role", tokenInfo.getRole())
                .header("X-User-Email", tokenInfo.getEmail())
                .build();
            
            return chain.filter(exchange.mutate().request(request).build());
        }
        
        return unauthorized(exchange);
    }
    
    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }
}
```

## Redis 사용의 장점

### ✅ **1. 토큰 무효화**
```java
// 로그아웃 시 즉시 무효화
public void logout(String token) {
    redisTemplate.delete("token:" + token);
    redisTemplate.opsForValue().set("blacklist:" + token, "true", Duration.ofHours(1));
}
```

### ✅ **2. 실시간 권한 변경**
```java
// 사용자 역할 변경 시 모든 토큰 무효화
public void updateUserRole(Long userId, String newRole) {
    userRepository.updateRole(userId, newRole);
    
    // 해당 사용자의 모든 토큰 무효화
    Set<String> userTokens = redisTemplate.opsForSet().members("user:" + userId + ":tokens");
    for (String token : userTokens) {
        redisTemplate.delete("token:" + token);
    }
}
```

### ✅ **3. 토큰 관리**
```java
// 사용자별 토큰 목록 관리
public List<String> getUserTokens(Long userId) {
    return new ArrayList<>(redisTemplate.opsForSet().members("user:" + userId + ":tokens"));
}

// 토큰 사용 통계
public long getActiveTokenCount() {
    return redisTemplate.keys("token:*").size();
}

// 만료된 토큰 정리
public void cleanupExpiredTokens() {
    Set<String> tokenKeys = redisTemplate.keys("token:*");
    for (String key : tokenKeys) {
        TokenInfo tokenInfo = redisTemplate.opsForValue().get(key);
        if (tokenInfo != null && tokenInfo.getExpiresAt().isBefore(LocalDateTime.now())) {
            redisTemplate.delete(key);
        }
    }
}
```

## Redis 설정

### 🎯 **Redis 의존성 추가**
```kotlin
// build.gradle.kts
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.session:spring-session-data-redis")
}
```

### 🎯 **Redis 설정**
```yaml
# application.yml
spring:
  redis:
    host: localhost
    port: 6379
    password: # 필요시 설정
    database: 0
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 8
        max-idle: 8
        min-idle: 0
```

### 🎯 **Redis Configuration**
```java
@Configuration
@EnableRedisRepositories
public class RedisConfig {
    
    @Bean
    public RedisTemplate<String, TokenInfo> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, TokenInfo> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // JSON 직렬화 설정
        Jackson2JsonRedisSerializer<TokenInfo> serializer = new Jackson2JsonRedisSerializer<>(TokenInfo.class);
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);
        
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());
        
        template.afterPropertiesSet();
        return template;
    }
}
```

## 권장 구조

### 🎯 **옵션 1: JWT + Redis (권장)**
```
JWT (만료시간, 서명) + Redis (무효화, 권한 관리)
```

### 🎯 **옵션 2: 순수 Redis**
```
Redis만 사용 (토큰 자체를 Redis에 저장)
```

## 성능 고려사항

### ✅ **장점**
1. ✅ **토큰 무효화**: 로그아웃 시 즉시 무효화
2. ✅ **실시간 권한 변경**: 사용자 권한 변경 시 즉시 반영
3. ✅ **보안 강화**: 토큰 탈취 시 무효화 가능
4. ✅ **토큰 관리**: 사용자별 토큰 목록 관리

### ❌ **단점**
1. ❌ **추가 지연시간**: Redis 조회로 인한 지연
2. ❌ **인프라 복잡성**: Redis 서버 추가 필요
3. ❌ **비용 증가**: Redis 서버 운영 비용
4. ❌ **가용성 의존**: Redis 장애 시 인증 불가

## 구현 예시

### 🎯 **전체 플로우**

#### **1. 로그인 플로우**
```
Client → Member Service (/api/auth/login)
                ↓
        사용자 검증 + JWT 토큰 생성
                ↓
        Redis에 토큰 정보 저장
                ↓
        Client에게 토큰 반환
```

#### **2. API 호출 플로우**
```
Client → Gateway (JWT 검증 + Redis 확인) → Microservices
                ↓
            토큰에서 사용자 정보 추출
                ↓
        헤더에 사용자 정보 추가하여 전달
```

#### **3. 로그아웃 플로우**
```
Client → Member Service (/api/auth/logout)
                ↓
        Redis에서 토큰 제거
                ↓
        블랙리스트에 추가 (선택사항)
```

## 결론

**JWT + Redis 하이브리드 방식**을 권장합니다:

- ✅ **JWT의 장점**: 무상태, 성능, 표준
- ✅ **Redis의 장점**: 무효화, 실시간 권한 변경, 토큰 관리
- ✅ **보안 강화**: 토큰 탈취 시 무효화 가능
- ✅ **유연성**: 실시간 권한 변경 지원

이 방식을 통해 **JWT의 장점과 Redis의 유연성**을 모두 활용할 수 있습니다! 