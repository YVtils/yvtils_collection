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
    )

    const val GROUP_ID = "yv.yvtils"

    val KNOWN_MODULES = mapOf(
        "discord" to ModuleArtifact("discord", "26.08.01", "yv.tils.discord.DiscordYVtils"),
        "regions" to ModuleArtifact("regions", "26.08.01", "yv.tils.regions.RegionsYVtils"),
        "multiMine" to ModuleArtifact("multiMine", "26.08.01", "yv.tils.multiMine.MultiMineYVtils"),
        "essentials" to ModuleArtifact("essentials", "26.08.01", "yv.tils.essentials.EssentialYVtils"),
        "sit" to ModuleArtifact("sit", "26.08.01", "yv.tils.sit.SitYVtils"),
        "status" to ModuleArtifact("status", "26.08.01", "yv.tils.status.StatusYVtils"),
        "server" to ModuleArtifact("server", "26.08.01", "yv.tils.server.ServerYVtils"),
        "message" to ModuleArtifact("message", "26.08.01", "yv.tils.message.MessageYVtils"),
        "moderation" to ModuleArtifact("moderation", "26.08.01", "yv.tils.moderation.ModerationYVtils"),
        "gui-v2" to ModuleArtifact("gui-v2", "26.08.01", "yv.tils.gui.GUIYVtils"),
        "migration" to ModuleArtifact("migration", "26.08.01", "yv.tils.migration.MigrationYVtils"),
        "stats" to ModuleArtifact("stats", "26.08.01", "yv.tils.stats.StatsYVtils"),
    )
}
