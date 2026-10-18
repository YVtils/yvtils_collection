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
    compileOnly(project(":config"))
    compileOnly(project(":utils"))
    compileOnly(project(":common"))
    compileOnly(project(":gui-26.1"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.0")
    testImplementation("org.mockito:mockito-core:5.23.0")
    testImplementation("io.papermc.paper:paper-api:26.1.2.build.74-stable")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    workingDir = layout.buildDirectory.get().asFile
}
