dependencies {
    // 내부 모듈 의존성
    implementation(project(":service:product-core"))
    implementation(project(":service:product-infra"))
}

// Admin 모듈도 실행 가능한 애플리케이션
tasks.bootJar {
    enabled = true
    archiveClassifier.set("admin") // admin JAR로 구분
}

tasks.jar {
    enabled = false
}