rootProject.name = "commerce"

include(
    "common",
    "common:snowflake",
    "common:logging",
    "common:encryption",
    "common:web-security",
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