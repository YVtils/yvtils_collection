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
 * GUI module for Minecraft `26.2.x`, built on InvUI 2.3.x.
 *
 * This module has NO source files of its own - see `gui-26.1/build.gradle.kts`
 * for why it points `sourceSets.main.kotlin` back at `gui-26.1`'s sources
 * instead of duplicating them. Only the InvUI version (and, via the root
 * `build.gradle.kts`'s `paperApiVersionOverrides`, the Paper API compile
 * target) differs between the two modules.
 *
 * If InvUI's public API ever changes incompatibly between the versions this
 * project targets, give this module its own `src/main/kotlin` instead of
 * sharing `gui-26.1`'s.
 */
sourceSets {
    main {
        kotlin {
            srcDir("../gui-26.1/src/main/kotlin")
        }
    }
}

dependencies {
    // exposed as `api` so that modules depending on `gui-26.2` can also
    // reference InvUI types (Gui, Item, Window, ...) directly without
    // having to declare their own dependency on InvUI.
    api("xyz.xenondevs.invui:invui:2.3.0")
    api("xyz.xenondevs.invui:invui-kotlin:2.3.0")

    compileOnly(project(":utils"))
    compileOnly(project(":config-v2"))
    compileOnly(project(":common"))
    // DataClassConfigGui reflects over data class properties/annotations directly (not just
    // through config-v2's inlined ObjectMapperFileUtils calls), so it needs its own
    // kotlin-reflect on the compile classpath.
    compileOnly(kotlin("reflect"))
}
