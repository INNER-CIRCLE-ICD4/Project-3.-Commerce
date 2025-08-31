plugins {
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
}

dependencies {
    api(project(":common:snowflake"))
    api(project(":service:order:core"))
    
    // Spring Data JPA
    api("org.springframework.boot:spring-boot-starter-data-jpa")
    
    // Database Drivers
    api("org.postgresql:postgresql:42.7.7")
    runtimeOnly("org.postgresql:postgresql")
    
    // H2 for testing
    testImplementation("com.h2database:h2")
    
    // Spring Web (for RestTemplate)
    implementation("org.springframework.boot:spring-boot-starter-web")
    
    // Spring Events (backup)
    implementation("org.springframework:spring-context")
    
    // Flyway for database migration
    api("org.flywaydb:flyway-core")
    
    // HikariCP (Connection Pool)
    implementation("com.zaxxer:HikariCP")
    
    // Jackson for JSON
    implementation("com.fasterxml.jackson.core:jackson-databind")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.testcontainers:testcontainers:1.20.3")
    testImplementation("org.testcontainers:postgresql:1.17.6")
    testImplementation("org.testcontainers:kafka:1.17.6")
    testImplementation("org.testcontainers:junit-jupiter:1.17.6")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}
