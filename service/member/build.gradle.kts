dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // ✅ 경량 BCrypt 라이브러리 (Spring Security 불필요)
    implementation("at.favre.lib:bcrypt:0.10.2")
    runtimeOnly("com.h2database:h2")
    implementation(project(":common:snowflake"))
    runtimeOnly("org.postgresql:postgresql")

    // Spring Cloud Config Client
    implementation("org.springframework.cloud:spring-cloud-starter-config")
    // Spring Boot Actuator (설정 새로고침용)
    implementation("org.springframework.boot:spring-boot-starter-actuator")
}

// 개발 환경에서만 테스트 비활성화
//if (project.hasProperty("skipTests")) {
//    tasks.withType<Test> {
//        enabled = false
//    }
//}