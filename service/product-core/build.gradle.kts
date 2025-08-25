plugins { `java-library` }

dependencies {
    api(project(":common:snowflake"))
    api(project(":common:logging"))

    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    api("org.apache.commons:commons-lang3")

    api("com.h2database:h2")
    api("org.postgresql:postgresql")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    testCompileOnly("org.projectlombok:lombok")
    testAnnotationProcessor("org.projectlombok:lombok")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core")
    testImplementation("org.mockito:mockito-junit-jupiter")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}