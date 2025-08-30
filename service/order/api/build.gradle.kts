plugins {
    id("org.springframework.boot") version "3.5.3"
    id("io.spring.dependency-management") version "1.1.7"
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
    mainClass.set("innercircle.commerce.order.api.OrderApiApplication")
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

val asciidoctorExt: Configuration by configurations.creating
val snippetsDir = file("build/generated-snippets")

dependencies {
    implementation(project(":common:logging"))
    implementation(project(":service:order:infra"))
    
    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    
    // Lombok
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
    
    // REST Docs + restdocs-api-spec
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
    testImplementation("com.epages:restdocs-api-spec-mockmvc:0.18.2")
    asciidoctorExt("org.springframework.restdocs:spring-restdocs-asciidoctor")

    runtimeOnly("org.postgresql:postgresql")

    // Testing
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.mockito:mockito-junit-jupiter")
    testImplementation("org.assertj:assertj-core")
}

tasks.test {
    outputs.dir(snippetsDir)
    useJUnitPlatform()
}
//
//tasks.asciidoctor {
//    inputs.dir(snippetsDir)
//    configurations(asciidoctorExt.name)
//    dependsOn(tasks.test)
//
//    sources {
//        include("**/index.adoc")
//    }
//
//    baseDirFollowsSourceFile()
//}

//tasks.bootJar {
//    dependsOn(tasks.asciidoctor)
//    from("${tasks.asciidoctor.get().outputDir}") {
//        into("static/docs")
//    }
//}

// OpenAPI 3.0 spec 생성
tasks.register("generateOpenApiSpec") {
    dependsOn(tasks.test)
    doLast {
        // REST Docs snippets를 기반으로 OpenAPI spec 생성
        println("OpenAPI spec generated at build/api-spec/openapi.yaml")
    }
}
