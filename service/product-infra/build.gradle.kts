dependencies {
    implementation(project(":service:product-core"))

    // AWS S3
    implementation("io.awspring.cloud:spring-cloud-aws-starter:3.1.1")
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.767")

    // [추가] Lombok 명시적 추가
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    // [추가] JPA/Hibernate 명시적 추가 (전이 의존성 문제 해결 시도)
    implementation("jakarta.persistence:jakarta.persistence-api")
    implementation("org.hibernate.orm:hibernate-core")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
