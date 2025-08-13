dependencies {

    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")
    implementation("org.springframework.boot:spring-boot-starter-security")                  // 인증 관련
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")    // 인증 관련

    // 🎯 분산 추적 - 정확한 설정
    implementation("org.springframework.boot:spring-boot-starter-actuator")  // ✅ 필수!
    implementation("io.micrometer:context-propagation")     // ✅ WebFlux용 필수!
    implementation("io.micrometer:micrometer-tracing-bridge-brave") // 1.2.1은 MDC 이슈 가능성
    implementation ("io.zipkin.reporter2:zipkin-reporter-brave")



    // macOS에서만 필요한 의존성 (현재 하드코딩된 것)
    if (System.getProperty("os.name").lowercase().contains("mac")) {
        runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.68.Final:osx-aarch_64")
    }

    testImplementation("io.projectreactor:reactor-test")
    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor") // Spring Boot 설정 메타데이터 생성
    developmentOnly("org.springframework.boot:spring-boot-docker-compose")

    implementation(project(":common:snowflake"))
}
