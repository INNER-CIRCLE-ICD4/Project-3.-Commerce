dependencies {

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("com.github.ulisesbocchio:jasypt-spring-boot-starter:3.0.5")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

tasks.test {
    useJUnitPlatform()
}