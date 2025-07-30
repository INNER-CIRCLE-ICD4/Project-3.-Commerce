plugins { `java-library` }

dependencies {
    api(project(":common:snowflake"))
    api(project(":common:logging"))

    api("org.springframework.boot:spring-boot-starter-web")
    api("org.springframework.boot:spring-boot-starter-validation")
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    testImplementation("org.mockito:mockito-junit-jupiter")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}