dependencies {
    implementation(project(":service:product-core"))
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}