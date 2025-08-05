plugins {
    java
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
}

group = "innercircle"
version = "0.0.1-SNAPSHOT"

//Cloud 버전 설정
extra["springCloudVersion"] = "2025.0.0"


allprojects {

    repositories {
        mavenCentral()
    }

    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    dependencies {
        compileOnly("org.projectlombok:lombok")
        annotationProcessor("org.projectlombok:lombok")
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}


subprojects {
    if (path.startsWith(":infra:") || path.startsWith(":service:member")) {
        dependencyManagement {
            imports {
                mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
            }
        }
    }
}