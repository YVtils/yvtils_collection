/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */

dependencies {
    compileOnly(project(":utils"))
    compileOnly(project(":config"))
    // Published normally: Paper resolves these in the library tier. No private shared runtime.
    implementation("org.xerial:sqlite-jdbc:3.53.4.0")
    implementation("io.prometheus:prometheus-metrics-core:1.10.0")
    implementation("io.prometheus:prometheus-metrics-exposition-textformats:1.10.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.14.0")
    testImplementation(project(":config"))
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    // Paper/Log4j may create logs even in pure unit tests. Keep generated files under build/.
    val testDirectory = layout.buildDirectory.dir("test-work")
    workingDir(testDirectory)
    doFirst { testDirectory.get().asFile.mkdirs() }
}
