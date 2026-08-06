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
    implementation(project(":utils"))

    // Exposed as `api` so that modules depending on `config-v2` can work
    // directly with Configurate's `ConfigurationNode`/`ConfigurationLoader`
    // types (e.g. to read/write custom sections) without having to declare
    // their own dependency on Configurate.
    //
    // `configurate-yaml` backs .yml files (SnakeYAML, with comment
    // preservation via CommentedConfigurationNode) and `configurate-gson`
    // backs .json files (Gson) - both formats are modeled through the same
    // `ConfigurationNode` API, which is what lets `ConfigurateFileUtils`
    // implement loading/saving/merging once instead of per-format.
    api("org.spongepowered:configurate-core:4.2.0")
    api("org.spongepowered:configurate-yaml:4.2.0")
    api("org.spongepowered:configurate-gson:4.2.0")
}
