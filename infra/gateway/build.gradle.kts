dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")
    testImplementation("io.projectreactor:reactor-test")

    implementation(project(":common:snowflake"))
}