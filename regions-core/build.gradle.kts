/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

dependencies {
    implementation(project(":common"))
    implementation(project(":utils"))
    implementation(project(":config-v2"))
    implementation(project(":regions"))
    // `regions` itself only declares gui-v2 as `compileOnly` (it's meant to be resolved once
    // via core's dynamic-loading embedded runtime bundle instead - see core/build.gradle.kts)
    // - this standalone monolithic launcher has no such bundle, so it needs gui-v2 shaded in
    // directly here instead, same as multiMine-core already does.
    implementation(project(":gui-v2"))
}

val version = "1.0.0-beta.2"

tasks {
    runServer {
        minecraftVersion("1.21.5")
    }

    shadowJar {
        archiveBaseName.set("YVtils-RG")
        archiveVersion.set(version)
        archiveClassifier.set("")
        archiveFileName.set("YVtils-RG_v${version}.jar")

        manifest {
            attributes["Main-Class"] = "yv.tils.core.YVtils"
        }
    }
}