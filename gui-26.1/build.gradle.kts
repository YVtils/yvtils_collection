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
 * GUI module for Minecraft `26.1.x`, built on InvUI 2.1.x.
 *
 * InvUI dropped multi-version support starting with v2 - each InvUI release
 * only targets ONE specific Minecraft version (see the compatibility table at
 * https://github.com/NichtStudioCode/InvUI). Supporting multiple Minecraft
 * versions therefore means multiple `gui-<version>` modules, each pinning the
 * InvUI release that matches its target version.
 *
 * This module (`gui-26.1`) is the canonical source of truth for the actual
 * GUI code - `gui-26.2` (and any future `gui-<version>` module) points its
 * `sourceSets.main.kotlin` back at THIS module's `src/main/kotlin` instead of
 * duplicating it, since InvUI's public API (`Gui`, `Item`, `Window`, ...) is
 * expected to stay source-compatible across the versions this project
 * targets. Re-verify that assumption whenever bumping to a new InvUI release
 * - if it ever breaks, that module will need its own source fork instead.
 *
 * `core` never embeds a fixed `gui-<version>` build at compile time; it
 * resolves whichever one matches the running server's actual Minecraft
 * version at runtime - see `DynamicModuleRegistry.GUI_ARTIFACTS` and
 * `DynamicModuleLoader` in `core`.
 */
dependencies {
    // exposed as `api` so that modules depending on `gui-26.1` can also
    // reference InvUI types (Gui, Item, Window, ...) directly without
    // having to declare their own dependency on InvUI.
    api("xyz.xenondevs.invui:invui:2.1.1")
    api("xyz.xenondevs.invui:invui-kotlin:2.1.1")

    compileOnly(project(":utils"))
    compileOnly(project(":config-v2"))
    compileOnly(project(":common"))
    // DataClassConfigGui reflects over data class properties/annotations directly (not just
    // through config-v2's inlined ObjectMapperFileUtils calls), so it needs its own
    // kotlin-reflect on the compile classpath.
    compileOnly(kotlin("reflect"))
}