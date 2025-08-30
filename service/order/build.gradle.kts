plugins {
    id("base")
    id("java-library")
}

allprojects {
    group = "innercircle.commerce"
    version = "0.0.1-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply(plugin = "java-library")

    java {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    tasks.withType<JavaCompile> {
        options.encoding = "UTF-8"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        // Logging
        implementation("org.slf4j:slf4j-api")
        
        // Testing
        testImplementation("org.junit.jupiter:junit-jupiter")
        testImplementation("org.assertj:assertj-core:3.24.2")
        testImplementation("org.mockito:mockito-core")
        testImplementation("org.mockito:mockito-junit-jupiter")
    }

//    configure<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension> {
//        imports {
//            mavenBom("org.springframework.boot:spring-boot-dependencies:2.7.14")
//        }
//    }
}

tasks.bootJar {
    enabled = false
}

tasks.jar {
    enabled = true
}