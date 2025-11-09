/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

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
include("multiMine-core")
include("gui")