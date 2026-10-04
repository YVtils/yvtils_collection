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

package yv.tils.core.loader;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps a module name (as used in {@code modules.yml}) to its published Maven
 * artifactId, version, and lifecycle entry-point class (implementing
 * {@code yv.tils.utils.modules.Module.YVtilsModule}).
 *
 * <p>Written in Java (not Kotlin) on purpose: this class - together with
 * {@link ModuleConfig}, {@link DynamicModuleLoader} and
 * {@code GeneratedModuleVersions} - is loaded by Paper's isolated
 * {@code PluginLoader} bootstrap classloader, which only sees this plugin's own
 * main jar and has no access to the runtime bundle yet. Keeping these classes
 * free of any Kotlin dependency lets the main jar avoid bundling its own copy of
 * the Kotlin stdlib (which would otherwise clash with the runtime bundle's copy
 * when a statically-bundled module's Kotlin classes are reflected by
 * library-tier code - see {@code core/build.gradle.kts}'s shadowJar exclusion).
 */
public final class DynamicModuleRegistry {

    private DynamicModuleRegistry() {
    }

    /** A single module's Maven coordinate + lifecycle entry point + toggle metadata. */
    public static final class ModuleArtifact {
        private final String artifactId;
        private final String version;
        private final String entryPointClass;
        private final String description;
        private final boolean hidden;
        private final boolean staticallyBundled;
        private final boolean manual;

        public ModuleArtifact(
                String artifactId,
                String version,
                String entryPointClass,
                String description,
                boolean hidden,
                boolean staticallyBundled,
                boolean manual) {
            this.artifactId = artifactId;
            this.version = version;
            this.entryPointClass = entryPointClass;
            this.description = description;
            this.hidden = hidden;
            this.staticallyBundled = staticallyBundled;
            this.manual = manual;
        }

        public ModuleArtifact(String artifactId, String version, String entryPointClass, String description) {
            this(artifactId, version, entryPointClass, description, false, false, false);
        }

        public String getArtifactId() {
            return artifactId;
        }

        public String getVersion() {
            return version;
        }

        public String getEntryPointClass() {
            return entryPointClass;
        }

        /**
         * A short, human-readable description shown in {@code /yvtils modules}. Available even for
         * modules that are currently disabled (their entry-point class was never instantiated).
         */
        public String getDescription() {
            return description;
        }

        /**
         * Whether this module is excluded from the dynamic-module system entirely (never listed in
         * {@code modules.yml}/the GUI, and any hand-added {@code true} line is ignored). Used for
         * internal, non-togglable modules (e.g. {@code migration}).
         */
        public boolean isHidden() {
            return hidden;
        }

        /**
         * Whether this module is shaded directly into the launcher's own main jar instead of being
         * fetched dynamically. Static modules stay togglable via {@code modules.yml} but are skipped
         * by {@link DynamicModuleLoader} (nothing to resolve) - their classes are already present in
         * the main jar. Used for modules that must run in the main plugin classloader (e.g.
         * {@code regions}, which references WorldGuard's API directly).
         */
        public boolean isStatic() {
            return staticallyBundled;
        }

        /**
         * Whether this module is a manual, opt-in module: excluded from the auto-generated
         * {@code modules.yml} and the GUI (like {@link #isHidden()}), but still honored if an admin
         * hand-adds it with {@code true}. {@link #isHidden()} takes precedence.
         */
        public boolean isManual() {
            return manual;
        }
    }

    public static final String GROUP_ID = "yv.yvtils";

    /**
     * Looks up a module's current version out of {@code GeneratedModuleVersions} (generated from the
     * repo-wide {@code gradle/module-versions.properties} by {@code core/build.gradle.kts}). Fails
     * fast if a module is missing an entry there.
     */
    private static String versionOf(String moduleName) {
        String version = GeneratedModuleVersions.VERSIONS.get(moduleName);
        if (version == null) {
            throw new IllegalStateException(
                    "No version found for module '" + moduleName + "' in gradle/module-versions.properties");
        }
        return version;
    }

    public static final Map<String, ModuleArtifact> KNOWN_MODULES = buildKnownModules();

    private static Map<String, ModuleArtifact> buildKnownModules() {
        LinkedHashMap<String, ModuleArtifact> modules = new LinkedHashMap<>();
        modules.put("discord", new ModuleArtifact(
                "discord", versionOf("discord"), "yv.tils.discord.DiscordYVtils",
                "Discord integration: chat bridging, webhooks and linked accounts."));
        modules.put("regions", new ModuleArtifact(
                "regions", versionOf("regions"), "yv.tils.regions.RegionsYVtils",
                "Survival claims and area protection backed by WorldGuard.",
                false, true, false));
        modules.put("multiMine", new ModuleArtifact(
                "multiMine", versionOf("multiMine"), "yv.tils.multiMine.MultiMineYVtils",
                "Lets multiple players break the same block together."));
        modules.put("essentials", new ModuleArtifact(
                "essentials", versionOf("essentials"), "yv.tils.essentials.EssentialYVtils",
                "Core quality-of-life commands (home, spawn, gamemode, etc.)."));
        modules.put("sit", new ModuleArtifact(
                "sit", versionOf("sit"), "yv.tils.sit.SitYVtils",
                "Lets players sit on stairs and slabs."));
        modules.put("status", new ModuleArtifact(
                "status", versionOf("status"), "yv.tils.status.StatusYVtils",
                "Player status effects and vanity status displays."));
        modules.put("server", new ModuleArtifact(
                "server", versionOf("server"), "yv.tils.server.ServerYVtils",
                "Server utility and administration commands."));
        modules.put("message", new ModuleArtifact(
                "message", versionOf("message"), "yv.tils.message.MessageYVtils",
                "Private messaging between players (/msg, /reply)."));
        modules.put("moderation", new ModuleArtifact(
                "moderation", versionOf("moderation"), "yv.tils.moderation.ModerationYVtils",
                "Moderation tools such as mutes and punishment logging."));
        modules.put("migration", new ModuleArtifact(
                "migration", versionOf("migration"), "yv.tils.migration.MigrationYVtils",
                "Migration tools for moving data between YVtils versions.",
                true, false, false));
        modules.put("stats", new ModuleArtifact(
                "stats", versionOf("stats"), "yv.tils.stats.StatsYVtils",
                "Player and server statistics tracking."));
        modules.put("yv-smp", new ModuleArtifact(
                "yv-smp", versionOf("yv-smp"), "yv.tils.yv_smp.YV_SMPYVtils",
                "YV SMP module for YVtils",
                false, false, true));
        modules.put("remade-ender-dragon", new ModuleArtifact(
                "remade-ender-dragon", versionOf("remade-ender-dragon"),
                "yv.tils.remadeEnderDragon.RemadeEnderDragonYVtils",
                "Remade Ender Dragon: player-scaled encounters, attacks and team support."));
        return Collections.unmodifiableMap(modules);
    }

    /**
     * The {@code gui} module is deliberately NOT a normal {@link #KNOWN_MODULES} entry: it wraps
     * InvUI, which dropped multi-version support in v2 (each release targets ONE Minecraft version).
     * This maps a Minecraft minor version (e.g. {@code "26.1"}) to the matching {@code gui-<version>}
     * artifact; {@link DynamicModuleLoader} resolves exactly one, matching the running server.
     */
    public static final Map<String, ModuleArtifact> GUI_ARTIFACTS = buildGuiArtifacts();

    private static Map<String, ModuleArtifact> buildGuiArtifacts() {
        LinkedHashMap<String, ModuleArtifact> modules = new LinkedHashMap<>();
        modules.put("26.1", new ModuleArtifact(
                "gui-26.1", versionOf("gui-26.1"), "yv.tils.gui.GUIYVtils",
                "GUI module for YVtils (Minecraft 26.1.x, InvUI 2.1.x)."));
        modules.put("26.2", new ModuleArtifact(
                "gui-26.2", versionOf("gui-26.2"), "yv.tils.gui.GUIYVtils",
                "GUI module for YVtils (Minecraft 26.2.x, InvUI 2.3.x)."));
        modules.put("26.3", new ModuleArtifact(
                "gui-26.3", versionOf("gui-26.3"), "yv.tils.gui.GUIYVtils",
                "GUI module for YVtils (Minecraft 26.3.x, InvUI 2.5.x)."));
        return Collections.unmodifiableMap(modules);
    }

    /** The {@code gui} entry used when the running server's Minecraft version has no exact match. */
    public static final String GUI_FALLBACK_MINECRAFT_VERSION = "26.3";

    /**
     * Extracts the {@code major.minor} part of a Minecraft version id (e.g. {@code "26.1.2"} ->
     * {@code "26.1"}), for looking up {@link #GUI_ARTIFACTS}.
     */
    public static String minecraftMinorVersion(String minecraftVersionId) {
        String[] parts = minecraftVersionId.split("\\.");
        if (parts.length >= 2) {
            return parts[0] + "." + parts[1];
        }
        return minecraftVersionId;
    }

    /**
     * Resolves the {@code gui} {@link ModuleArtifact} for a Minecraft version id, falling back to
     * {@link #GUI_FALLBACK_MINECRAFT_VERSION} (caller expected to log a warning) if there's no exact
     * match yet.
     */
    public static ModuleArtifact guiArtifactFor(String minecraftVersionId) {
        String minorVersion = minecraftMinorVersion(minecraftVersionId);
        ModuleArtifact exact = GUI_ARTIFACTS.get(minorVersion);
        if (exact != null) {
            return exact;
        }
        ModuleArtifact fallback = GUI_ARTIFACTS.get(GUI_FALLBACK_MINECRAFT_VERSION);
        if (fallback == null) {
            throw new IllegalStateException(
                    "No gui artifact registered for fallback Minecraft version " + GUI_FALLBACK_MINECRAFT_VERSION);
        }
        return fallback;
    }
}
