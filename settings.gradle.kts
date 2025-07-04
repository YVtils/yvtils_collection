pluginManagement {
    repositories {
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "yvtils_collection"
include("core")
include("common")
include("config")
include("vanish")
include("moderation")
include("discord")
include("fusion")
include("multiMine")
include("server")
include("sit")
include("status")
include("waypoint")
include("utils")
include("essentials")
include("message")
include("regions")
include("regions-core")
include("discord-core")
include("migration")