// common/web-security/build.gradle.kts
plugins {
    id("java-library")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

dependencies {

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-logging")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // 테스트
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}