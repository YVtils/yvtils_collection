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
         * toggle on/off directly - e.g. `migration` is an internal, one-off
         * data-migration tool, not a persistent feature toggle.
         *
         * (The `gui` module used to be listed here too, but it isn't a normal
         * [KNOWN_MODULES] entry at all anymore - see [GUI_ARTIFACTS].)
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

    /**
     * The `gui` module is deliberately NOT a normal [KNOWN_MODULES] entry.
     *
     * Every other module is published under a single, fixed artifactId - one
     * Maven coordinate always works, regardless of which Minecraft version
     * the server is actually running, because those modules only touch
     * stable Paper API. `gui` is the one exception: it wraps InvUI, which
     * dropped multi-version support starting with v2 (each InvUI release
     * only targets ONE specific Minecraft version - see
     * https://github.com/NichtStudioCode/InvUI's compatibility table).
     * Resolving the wrong `gui-<version>` artifact for the running server
     * would load an InvUI build that doesn't match its internals.
     *
     * This maps a Minecraft *minor* version (e.g. `"26.1"`, `"26.2"` - see
     * [minecraftMinorVersion]) to the `gui-<version>` artifact that was built
     * against a matching InvUI release. [DynamicModuleLoader] looks up the
     * running server's actual Minecraft version at plugin-load time (via
     * `io.papermc.paper.ServerBuildInfo`) and resolves only that one entry,
     * unconditionally (like `migration`/formerly `gui-v2`, it's never listed
     * in `modules.yml` or toggle-able - there's nothing to toggle, every
     * server needs exactly one matching `gui` build).
     *
     * To add support for a new Minecraft version once InvUI publishes a
     * matching release (e.g. `26.3`):
     * 1. Create a `gui-26.3` Gradle module (copy `gui-26.2/build.gradle.kts`,
     *    point its `sourceSets.main.kotlin` at `gui-26.1`'s sources, pin the
     *    new InvUI version).
     * 2. Add `"gui-26.3"` to `publishableModules` in the root
     *    `build.gradle.kts`, and a `"gui-26.3" to "26.3"` entry to
     *    `paperApiVersionOverrides` there too.
     * 3. Add a `"26.3" to ModuleArtifact("gui-26.3", ...)` entry below.
     * 4. Publish it (`./gradlew :gui-26.3:publish`).
     */
    val GUI_ARTIFACTS: Map<String, ModuleArtifact> = mapOf(
        "26.1" to ModuleArtifact(
            "gui-26.1", "26.08.01", "yv.tils.gui.GUIYVtils",
            "GUI module for YVtils (Minecraft 26.1.x, InvUI 2.1.x)."
        ),
        "26.2" to ModuleArtifact(
            "gui-26.2", "26.08.01", "yv.tils.gui.GUIYVtils",
            "GUI module for YVtils (Minecraft 26.2.x, InvUI 2.3.x)."
        ),
    )

    /** The `gui` entry used when the running server's Minecraft version has no exact match in
     * [GUI_ARTIFACTS] (e.g. a not-yet-added future version). Picking a mismatched InvUI build is
     * risky (see [GUI_ARTIFACTS]'s docs), but picking none at all means every module that opens a
     * GUI (config editors, `/yvtils modules`, ...) silently fails instead - falling back to the
     * newest known entry is the better default until that new version gets its own real entry. */
    val GUI_FALLBACK_MINECRAFT_VERSION = "26.2"

    /**
     * Extracts the `major.minor` part of a Minecraft version id as returned by
     * `io.papermc.paper.ServerBuildInfo.buildInfo().minecraftVersionId()`
     * (e.g. `"26.1.2"` -> `"26.1"`, `"26.2"` -> `"26.2"`), for looking up
     * [GUI_ARTIFACTS]. Patch releases within the same minor version (e.g. a
     * hypothetical `26.1.3`) are assumed to stay compatible with the same
     * InvUI release as `26.1.2`, matching InvUI's own compatibility table.
     */
    fun minecraftMinorVersion(minecraftVersionId: String): String {
        val parts = minecraftVersionId.split(".")
        return if (parts.size >= 2) "${parts[0]}.${parts[1]}" else minecraftVersionId
    }

    /**
     * Resolves the `gui` [ModuleArtifact] to use for a given Minecraft
     * version id, falling back to [GUI_FALLBACK_MINECRAFT_VERSION] (with the
     * caller expected to log a warning) if there's no exact match yet.
     */
    fun guiArtifactFor(minecraftVersionId: String): ModuleArtifact {
        val minorVersion = minecraftMinorVersion(minecraftVersionId)
        return GUI_ARTIFACTS[minorVersion]
            ?: GUI_ARTIFACTS.getValue(GUI_FALLBACK_MINECRAFT_VERSION)
    }
}
