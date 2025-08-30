rootProject.name = "commerce"

include(
    "common",
    "common:snowflake",
    "common:logging",
    "service",
    "service:order:core",
    "service:order:api",
    "service:order:admin",
    "service:order:infra",
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
