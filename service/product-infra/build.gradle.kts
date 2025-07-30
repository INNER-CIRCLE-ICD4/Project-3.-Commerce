dependencies {
    // 내부 모듈 의존성
    implementation(project(":service:product-core"))
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}