// 공통 라이브러리 모듈이므로 bootJar 비활성화, jar 활성화
tasks.named<org.springframework.boot.gradle.tasks.bundling.BootJar>("bootJar") {
    enabled = false
}


tasks.named<Jar>("jar") {
    enabled = true
    archiveClassifier = ""
}
