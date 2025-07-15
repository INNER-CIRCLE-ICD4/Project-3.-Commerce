val springCloudVersion by extra("2025.0.0")
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation(project(":common:snowflake"))
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")
    testImplementation("io.projectreactor:reactor-test")
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion")
    }
}
