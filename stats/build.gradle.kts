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
    compileOnly(project(":utils"))
    compileOnly(project(":config-v2"))
    compileOnly(project(":common"))
    // Compile-time only, to reference GUI types - NOT shaded into this module's own
    // published artifact. The actual gui-<version> build used at runtime is resolved
    // dynamically by core's DynamicModuleLoader, matching the running server's Minecraft
    // version (InvUI, which `gui` wraps, dropped multi-version support in v2 - see
    // DynamicModuleRegistry.GUI_ARTIFACTS for the full explanation).
    compileOnly(project(":gui-26.1"))
}

tasks.test {
    useJUnitPlatform()
}