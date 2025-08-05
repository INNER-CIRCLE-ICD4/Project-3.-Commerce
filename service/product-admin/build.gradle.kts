dependencies {
    // 내부 모듈 의존성
    implementation(project(":service:product-core"))

    // AWS S3
    implementation("io.awspring.cloud:spring-cloud-aws-starter:3.1.1")
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.767")
}

// Admin 모듈도 실행 가능한 애플리케이션
tasks.bootJar {
    enabled = true
    archiveClassifier.set("admin") // admin JAR로 구분
}

tasks.jar {
    enabled = false
}