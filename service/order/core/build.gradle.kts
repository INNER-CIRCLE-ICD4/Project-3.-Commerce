plugins {
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
}
dependencies {
    // Core domain dependencies only - no Spring Boot starters
    implementation("org.springframework:spring-context")
    implementation("org.springframework:spring-tx")
    
    // Validation
    api("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("org.hibernate.validator:hibernate-validator")
    
    // Jackson for JSON
    api("com.fasterxml.jackson.core:jackson-annotations")
    api("com.fasterxml.jackson.core:jackson-databind")
    
    // Annotations
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    // Testing
    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
