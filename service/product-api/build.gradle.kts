dependencies {
    // 내부 모듈 의존성
    implementation(project(":service:product-core"))
}

// API 모듈은 실행 가능한 애플리케이션
tasks.bootJar {
    enabled = true
    archiveClassifier.set("") // 기본 JAR로 설정
}

tasks.jar {
    enabled = false
}