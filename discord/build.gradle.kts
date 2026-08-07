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

val jdaVersion = "6.4.1"

dependencies {
    compileOnly(project(":config-v2"))
    compileOnly(project(":utils"))
    compileOnly(project(":common"))
    // Provided at compile time only, resolved exactly once via the embedded runtime bundle
    // (see core/build.gradle.kts) - NOT shaded into this module's own published artifact,
    // since InvUI (gui-v2's dependency) keeps classloader-sensitive global state that must
    // not be duplicated across separately-resolved copies of this module.
    compileOnly(project(":gui-v2"))

    implementation("net.dv8tion:JDA:${jdaVersion}")
}
