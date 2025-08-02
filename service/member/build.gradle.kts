dependencies {

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    // ✅ 경량 BCrypt 라이브러리 (Spring Security 불필요)
    implementation("at.favre.lib:bcrypt:0.10.2")
    runtimeOnly("com.h2database:h2")
    implementation(project(":common:snowflake"))
}