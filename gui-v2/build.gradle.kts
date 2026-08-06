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
    // exposed as `api` so that modules depending on `gui-v2` can also
    // reference InvUI types (Gui, Item, Window, ...) directly without
    // having to declare their own dependency on InvUI.
    api("xyz.xenondevs.invui:invui:2.1.1")
    api("xyz.xenondevs.invui:invui-kotlin:2.1.1")

    compileOnly(project(":utils"))
    compileOnly(project(":config-v2"))
    compileOnly(project(":common"))
}