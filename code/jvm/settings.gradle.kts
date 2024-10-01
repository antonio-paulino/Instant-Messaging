plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "daw-2024-im-g07"

include("im-http-api")
include("im-services")
include("im-domain")
include("im-repository")
include("im-database")