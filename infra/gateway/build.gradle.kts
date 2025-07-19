dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-gateway-server-webflux")
    implementation("org.springframework.cloud:spring-cloud-starter-netflix-eureka-client")
    testImplementation("io.projectreactor:reactor-test")

    // macOS에서만 필요한 의존성 (현재 하드코딩된 것)
    if (System.getProperty("os.name").lowercase().contains("mac")) {
        runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.68.Final:osx-aarch_64")
    }

    implementation(project(":common:snowflake"))
}
