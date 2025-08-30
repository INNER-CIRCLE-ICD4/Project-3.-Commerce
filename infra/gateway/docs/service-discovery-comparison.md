# AWS 서비스 디스커버리 대체 기술 비교

## 개요

AWS에서 서비스 디스커버리를 대체할 수 있는 기술들이 많은 이유는 **각각의 사용 목적과 특성이 다르기 때문**입니다. 각 서비스의 특징과 장단점을 분석합니다.

## 1. AWS Cloud Map

### 특징
- **전용 서비스 디스커버리 서비스**
- **자동 서비스 등록/해제**
- **헬스체크 지원**
- **네임스페이스 관리**

### 장점 ✅
- 서비스 디스커버리에 특화
- 자동 헬스체크
- DNS 기반 통신
- ECS와 완벽 통합
- 자동 서비스 등록/해제
- 스케일링 시 자동 인스턴스 관리
- 비정상 인스턴스 자동 제거

### 단점 ❌
- 추가 비용 발생
- 복잡한 설정
- AWS 전용 서비스

### 사용 사례
```yaml
# Cloud Map 설정
Resources:
  PrivateNamespace:
    Type: AWS::ServiceDiscovery::PrivateDnsNamespace
    Properties:
      Name: ecommerce.internal
      Vpc: vpc-12345678

  MemberService:
    Type: AWS::ServiceDiscovery::Service
    Properties:
      Name: member
      NamespaceId: !Ref PrivateNamespace
      DnsConfig:
        DnsRecords:
          - Type: A
            TTL: 10
      HealthCheckConfig:
        Type: HTTP
        ResourcePath: /health
```

## 2. Route 53

### 특징
- **DNS 관리 서비스**
- **지리적 라우팅**
- **헬스체크**
- **트래픽 관리**

### 장점 ✅
- 글로벌 DNS 관리
- 지리적 라우팅
- 고가용성
- 트래픽 분산
- 비용 효율적
- 표준 DNS 프로토콜

### 단점 ❌
- 서비스 디스커버리 전용 아님
- 동적 서비스 등록 어려움
- ECS와 자동 통합 안됨
- 수동 관리 필요

### 사용 사례
```yaml
# Route 53 설정
Resources:
  MemberServiceRecord:
    Type: AWS::Route53::RecordSet
    Properties:
      HostedZoneId: Z1234567890
      Name: member.ecommerce.com
      Type: A
      AliasTarget:
        DNSName: !GetAtt ApplicationLoadBalancer.DNSName
        HostedZoneId: !GetAtt ApplicationLoadBalancer.CanonicalHostedZoneID
```

## 3. Application Load Balancer (ALB)

### 특징
- **로드 밸런싱**
- **헬스체크**
- **Path-based 라우팅**
- **SSL/TLS 종료**

### 장점 ✅
- 자동 헬스체크
- 로드 밸런싱
- SSL/TLS 처리
- Path 기반 라우팅
- 설정 간단
- HTTP 레벨 기능

### 단점 ❌
- 서비스 간 직접 통신 어려움
- DNS 기반이 아님
- 추가 지연시간
- 비용 발생

### 사용 사례
```yaml
# ALB 설정
Resources:
  ApplicationLoadBalancer:
    Type: AWS::ElasticLoadBalancingV2::LoadBalancer
    Properties:
      Type: application
      Scheme: internet-facing

  MemberTargetGroup:
    Type: AWS::ElasticLoadBalancingV2::TargetGroup
    Properties:
      Port: 8080
      Protocol: HTTP
      HealthCheckPath: /health
```

## 4. Network Load Balancer (NLB)

### 특징
- **고성능 로드 밸런싱**
- **TCP/UDP 지원**
- **고정 IP**
- **초당 수백만 요청**

### 장점 ✅
- 고성능
- 고정 IP
- TCP 레벨 로드 밸런싱
- 내부 통신에 적합
- 초당 수백만 요청 처리

### 단점 ❌
- 비용이 높음
- HTTP 레벨 기능 제한
- 설정 복잡
- 서비스 디스커버리 기능 없음

### 사용 사례
```yaml
# 내부 NLB 설정
Resources:
  InternalNLB:
    Type: AWS::ElasticLoadBalancingV2::LoadBalancer
    Properties:
      Type: network
      Scheme: internal  # 내부 통신용
      Subnets:
        - subnet-12345678
```

## 5. API Gateway

### 특징
- **API 관리**
- **인증/인가**
- **Rate Limiting**
- **API 버전 관리**

### 장점 ✅
- API 관리 기능
- 인증/인가
- Rate Limiting
- API 문서화
- 버전 관리
- 트래픽 제어

### 단점 ❌
- 서비스 간 직접 통신 어려움
- 추가 지연시간
- 비용 높음
- 복잡한 설정

### 사용 사례
```yaml
# API Gateway 설정
Resources:
  ApiGateway:
    Type: AWS::ApiGateway::RestApi
    Properties:
      Name: e-commerce-api

  MemberIntegration:
    Type: AWS::ApiGateway::Integration
    Properties:
      RestApiId: !Ref ApiGateway
      Type: HTTP_PROXY
      IntegrationHttpMethod: POST
      Uri: http://member-service.internal:8080/members
```

## 6. VPC Endpoints

### 특징
- **프라이빗 연결**
- **보안 강화**
- **비용 절약**

### 장점 ✅
- 보안 강화
- 비용 절약
- 프라이빗 연결
- AWS 서비스와 안전한 통신

### 단점 ❌
- 서비스 디스커버리 기능 없음
- AWS 서비스 전용
- 제한된 기능

### 사용 사례
```yaml
# VPC Endpoint 설정
Resources:
  S3Endpoint:
    Type: AWS::EC2::VPCEndpoint
    Properties:
      VpcId: vpc-12345678
      ServiceName: com.amazonaws.region.s3
      VpcEndpointType: Gateway
```

## 서비스별 비교표

| 서비스 | 목적 | 성능 | 비용 | 복잡도 | ECS 통합 | 자동화 | 적합한 사용 |
|--------|------|------|------|--------|----------|--------|-------------|
| **Cloud Map** | 서비스 디스커버리 | 높음 | 중간 | 중간 | ✅ 완벽 | ✅ 높음 | MSA 내부 통신 |
| **Route 53** | DNS 관리 | 높음 | 낮음 | 낮음 | ❌ 수동 | ❌ 낮음 | 글로벌 DNS |
| **ALB** | 로드 밸런싱 | 높음 | 중간 | 낮음 | ✅ 부분 | ✅ 중간 | HTTP 트래픽 |
| **NLB** | 고성능 로드 밸런싱 | 매우 높음 | 높음 | 중간 | ✅ 부분 | ✅ 중간 | TCP 트래픽 |
| **API Gateway** | API 관리 | 중간 | 높음 | 높음 | ❌ 없음 | ❌ 낮음 | API 게이트웨이 |
| **VPC Endpoints** | 프라이빗 연결 | 높음 | 낮음 | 낮음 | ❌ 없음 | ❌ 없음 | AWS 서비스 연결 |

## 권장 아키텍처

### 옵션 1: Cloud Map + ALB (권장)
```
Internet → ALB → ECS Services
         ↓
Cloud Map → 서비스 디스커버리
```

### 옵션 2: Route 53 + NLB
```
Internet → Route 53 → NLB → ECS Services
```

### 옵션 3: API Gateway + ALB
```
Internet → API Gateway → ALB → ECS Services
```

## ECS 환경에서의 권장사항

### Cloud Map이 가장 적합한 이유

1. **완벽한 ECS 통합**
   - 서비스 시작/종료 시 자동 등록/해제
   - 스케일링 시 자동 인스턴스 관리
   - 헬스체크 자동화

2. **안정성**
   - 비정상 인스턴스 자동 제거
   - DNS 기반 로드 밸런싱
   - 장애 복구 자동화

3. **간편성**
   - ECS와 완벽 통합
   - 설정 최소화
   - 관리 부담 감소

### 실제 사용 예시

#### 서비스 간 통신
```java
// Member 서비스에서 Product 서비스 호출
@Service
public class ProductClient {
    
    @Value("${product.service.url}")
    private String productServiceUrl; // product.ecommerce.internal:8080
    
    public ProductResponse getProduct(Long productId) {
        String url = productServiceUrl + "/products/" + productId;
        return restTemplate.getForObject(url, ProductResponse.class);
    }
}
```

#### application.yml 설정
```yaml
# Member 서비스 설정
product:
  service:
    url: http://product.ecommerce.internal:8080

order:
  service:
    url: http://order.ecommerce.internal:8080

review:
  service:
    url: http://review.ecommerce.internal:8080
```

## 결론

**ECS 환경에서는 Cloud Map이 가장 적합**합니다:

- ✅ **자동화**: 서비스 등록/해제 자동화
- ✅ **안정성**: 헬스체크 및 장애 복구
- ✅ **간편성**: ECS와 완벽 통합
- ✅ **성능**: DNS 기반 고성능 통신

다른 서비스들은 각각의 특정 목적에 최적화되어 있어서, **사용 사례에 따라 적절한 조합**을 선택해야 합니다. 