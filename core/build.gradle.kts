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
    implementation(project(":essentials"))
    implementation(project(":config"))
    implementation(project(":sit"))
    implementation(project(":message"))
    implementation(project(":multiMine"))
    implementation(project(":status"))
    implementation(project(":server"))
    implementation(project(":regions"))
    implementation(project(":discord"))
}

tasks {
    runServer {
        minecraftVersion("1.21.5")
    }
}