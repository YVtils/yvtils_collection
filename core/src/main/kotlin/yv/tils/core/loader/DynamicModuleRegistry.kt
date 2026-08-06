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

package yv.tils.core.loader

/**
 * Maps a module name (as used in `modules.txt`) to its published Maven
 * artifactId, version, and lifecycle entry-point class (implementing
 * `yv.tils.utils.modules.Module.YVtilsModule`).
 *
 * This is a stand-in for a more robust discovery mechanism (e.g. a manifest
 * attribute or `META-INF/services` entry baked into each published module jar)
 * since today's naming isn't fully consistent across modules
 * (e.g. `essentials` -> `EssentialYVtils`, `gui` -> `GUIYVtils`).
 *
 * Versioning is independent per module (not tied to the launcher's own
 * version) - bump the `version` here when a module gets a new release.
 */
object DynamicModuleRegistry {
    data class ModuleArtifact(
        val artifactId: String,
        val version: String,
        val entryPointClass: String,
        /**
         * A short, human-readable description shown in `/yvtils modules`.
         * Unlike [yv.tils.utils.modules.Module.YVtilsModuleData.description]
         * (only populated once a module is actually resolved/instantiated),
         * this is available even for modules that are currently disabled -
         * their entry-point class was never instantiated, so no
         * `YVtilsModuleData` exists for them yet.
         */
        val description: String = "",
        /**
         * Whether this module is excluded from the dynamic-module system
         * entirely, instead of being a normal togglable module.
         *
         * Used for modules that aren't meaningful for a server admin to
         * toggle on/off directly:
         * - `gui-v2` is a shared library other modules depend on, not an
         *   independently useful feature (see the class docs on
         *   `yv.tils.gui.core.InvUIBootstrap` for why its own lifecycle
         *   toggle is largely meaningless) - and for `core` specifically,
         *   it's already bundled directly via a Gradle `implementation`
         *   dependency, so it doesn't need to be dynamically fetched here
         *   at all.
         * - `migration` is an internal, one-off data-migration tool, not a
         *   persistent feature toggle.
         *
         * Hidden modules are excluded from [ModuleConfig.readEnabledModules]'s
         * result unconditionally - not shown/toggle-able in the in-game GUI,
         * not listed in the generated `modules.yml`, and any manually
         * hand-added `true` line for one is ignored. This is deliberately
         * "always excluded" rather than "always force-enabled": the result
         * feeds [DynamicModuleLoader], which resolves every enabled module's
         * artifact over the network - force-enabling a hidden module would
         * mean it's unconditionally Maven-resolved on every boot, which
         * breaks plugin loading entirely in any environment where that
         * artifact isn't actually published (e.g. a throwaway dev registry).
         */
        val hidden: Boolean = false,
    )

    const val GROUP_ID = "yv.yvtils"

    val KNOWN_MODULES = mapOf(
        "discord" to ModuleArtifact(
            "discord", "26.08.01", "yv.tils.discord.DiscordYVtils",
            "Discord integration: chat bridging, webhooks and linked accounts."
        ),
        "regions" to ModuleArtifact(
            "regions", "26.08.01", "yv.tils.regions.RegionsYVtils",
            "Land claiming and protected regions."
        ),
        "multiMine" to ModuleArtifact(
            "multiMine", "26.08.01", "yv.tils.multiMine.MultiMineYVtils",
            "Lets multiple players break the same block together."
        ),
        "essentials" to ModuleArtifact(
            "essentials", "26.08.01", "yv.tils.essentials.EssentialYVtils",
            "Core quality-of-life commands (home, spawn, gamemode, etc.)."
        ),
        "sit" to ModuleArtifact(
            "sit", "26.08.01", "yv.tils.sit.SitYVtils",
            "Lets players sit on stairs and slabs."
        ),
        "status" to ModuleArtifact(
            "status", "26.08.01", "yv.tils.status.StatusYVtils",
            "Player status effects and vanity status displays."
        ),
        "server" to ModuleArtifact(
            "server", "26.08.01", "yv.tils.server.ServerYVtils",
            "Server utility and administration commands."
        ),
        "message" to ModuleArtifact(
            "message", "26.08.01", "yv.tils.message.MessageYVtils",
            "Private messaging between players (/msg, /reply)."
        ),
        "moderation" to ModuleArtifact(
            "moderation", "26.08.01", "yv.tils.moderation.ModerationYVtils",
            "Moderation tools such as mutes and punishment logging."
        ),
        "gui-v2" to ModuleArtifact(
            "gui-v2", "26.08.01", "yv.tils.gui.GUIYVtils",
            "GUI module for YVtils, powered by InvUI.",
            hidden = true,
        ),
        "migration" to ModuleArtifact(
            "migration", "26.08.01", "yv.tils.migration.MigrationYVtils",
            "Migration tools for moving data between YVtils versions.",
            hidden = true,
        ),
        "stats" to ModuleArtifact(
            "stats", "26.08.01", "yv.tils.stats.StatsYVtils",
            "Player and server statistics tracking."
        ),
    )
}
