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
    compileOnly(project(":gui-26.1"))
    compileOnly(project(":common"))
    compileOnly(project(":config"))
    compileOnly(project(":utils"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.0")
    testImplementation(project(":config"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test { useJUnitPlatform() }
