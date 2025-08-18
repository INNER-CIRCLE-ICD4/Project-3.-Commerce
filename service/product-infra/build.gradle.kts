dependencies {
    implementation(project(":service:product-core"))

    // AWS S3
    implementation("io.awspring.cloud:spring-cloud-aws-starter:3.1.1")
    implementation("com.amazonaws:aws-java-sdk-s3:1.12.767")

    implementation("com.h2database:h2")
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}