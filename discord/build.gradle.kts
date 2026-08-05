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
    compileOnly(project(":config"))
    compileOnly(project(":utils"))
    compileOnly(project(":common"))

    implementation("net.dv8tion:JDA:${jdaVersion}")
}
