/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */

dependencies {
    compileOnly(project(":utils"))
    compileOnly(project(":config-v2"))
    compileOnly(project(":common"))
    compileOnly(project(":gui-26.1"))
    compileOnly(kotlin("reflect"))
    testImplementation(kotlin("test"))
}

tasks.test { useJUnitPlatform() }
