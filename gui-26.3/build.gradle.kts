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

/*
 * GUI module for Minecraft `26.3.x`, built on InvUI 2.5.x.
 *
 * Shares `gui-26.1`'s Kotlin sources; see `gui-26.1/build.gradle.kts` for why.
 * Only the InvUI version and the Paper API compile target (configured in the
 * root `build.gradle.kts`'s `paperApiVersionOverrides`) differ.
 *
 * If InvUI's public API changes incompatibly, give this module its own
 * `src/main/kotlin` instead of sharing `gui-26.1`'s.
 */
sourceSets {
    main {
        kotlin {
            srcDir("../gui-26.1/src/main/kotlin")
        }
    }
}

dependencies {
    // Expose InvUI types to modules depending on `gui-26.3`.
    api("xyz.xenondevs.invui:invui:2.5.1")
    api("xyz.xenondevs.invui:invui-kotlin:2.5.1")

    compileOnly(project(":utils"))
    compileOnly(project(":config-v2"))
    compileOnly(project(":common"))
    // DataClassConfigGui reflects over data class properties/annotations directly.
    compileOnly(kotlin("reflect"))
}
