dependencies {
    implementation(project(":service:product-core"))
    
    // AWS S3
    implementation("io.awspring.cloud:spring-cloud-aws-starter:3.1.1")
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.767")
    
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework:spring-web")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}