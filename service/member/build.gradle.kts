dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.cloud:spring-cloud-starter-config") // Spring Cloud Config Client
    implementation("org.springframework.boot:spring-boot-starter-actuator") // Spring Boot Actuator (설정 새로고침용)
    implementation("at.favre.lib:bcrypt:0.10.2") // ✅ 경량 BCrypt 라이브러리 (Spring Security 불필요)
    implementation("org.springframework.boot:spring-boot-starter-security") // Spring Security (JWT만 사용, 나머지는 비활성화)
    implementation("io.micrometer:micrometer-tracing-bridge-brave") // ✅ Micrometer Tracing Brave (Zipkin 연동용)
    implementation("io.zipkin.reporter2:zipkin-reporter-brave")

    implementation("io.jsonwebtoken:jjwt-api:0.12.3")  //jwt 생성 관련 설정
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.3")    //jwt 생성 관련 설정
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.3") //jwt 생성 관련 설정

    runtimeOnly("com.h2database:h2")
    runtimeOnly("org.postgresql:postgresql")

    implementation(project(":common:encryption"))
    implementation(project(":common:snowflake"))
    implementation(project(":common:web-security"))

}

// 개발 환경에서만 테스트 비활성화
//if (project.hasProperty("skipTests")) {
//    tasks.withType<Test> {
//        enabled = false
//    }
//}