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
    implementation(project(":config"))
    implementation(project(":gui"))
}

val version = "10.0.0-dev.1"

tasks {
    runServer {
        minecraftVersion("1.21.11")
    }

    shadowJar {
        archiveBaseName.set("YVtils-YV_SMP")
        archiveVersion.set(version)
        archiveClassifier.set("")
        archiveFileName.set("YVtils-YV_SMP_v$version.jar")

        manifest {
            attributes["Main-Class"] = "yv.tils.core.YVtils"
        }
    }
}
