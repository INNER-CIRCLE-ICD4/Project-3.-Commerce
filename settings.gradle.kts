pluginManagement {
    plugins {
        kotlin("jvm") version "2.2.0"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "commerce"

include(
    "common",
    "common:snowflake",
    "common:logging",
    "service",
    "service:order",
    "service:review",
    "service:product-core",
    "service:product-infra",
    "service:product-api",
    "service:product-admin",
    "service:search",
    "service:member",
    "infra",
    "infra:gateway",
    "infra:config-server",
    "infra:eureka-server"
)
