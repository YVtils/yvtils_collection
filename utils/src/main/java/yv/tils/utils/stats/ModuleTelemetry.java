/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */
package yv.tils.utils.stats;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Built-in collection: fixed aggregate event counters and current module metadata only.
 * Holds no event history; the optional consent-aware backend owns all counter values.
 * Lifecycle registration is independent of consent, just like the loaded-module registry.
 */
public final class ModuleTelemetry {
    private static final ConcurrentHashMap<String, Registration> MODULES = new ConcurrentHashMap<>();
    private record Registration(String version, ModuleStats publisher) {}

    private ModuleTelemetry() {}

    /** Fixed low-cardinality catalog: callers cannot accidentally pass user text as a metric. */
    public enum Event {
        CORE_INFO_VIEWS("core", "info_views_total", "Plugin information requests"),
        CORE_CONFIG_BROWSER_OPENS("core", "config_browser_opens_total", "Configuration browser opens"),
        CORE_MODULE_BROWSER_OPENS("core", "module_browser_opens_total", "Module browser opens"),
        COMMON_LOCALE_CHANGES("common", "locale_changes_total", "Player locale changes handled; no locale values"),
        CONFIG_LOADS("config", "file_loads_total", "Successful configuration file loads; no paths or contents"),
        CONFIG_SAVES("config", "file_saves_total", "Successful configuration file saves; no paths or contents"),
        UTILS_TASKS_SCHEDULED("utils", "tasks_scheduled_total", "Shared coroutine tasks scheduled; no task names"),
        GUI_CONFIG_EDITOR_OPENS("gui", "config_editor_opens_total", "Shared typed configuration editor opens"),
        VANISH_TOGGLES("vanish", "toggles_total", "Successful vanish state toggles"),
        WAYPOINT_NAVIGATIONS_STARTED("waypoint", "navigations_started_total", "Waypoint navigations started; no destinations"),
        FUSION_CRAFTS("fusion", "crafts_total", "Recipe repetitions successfully crafted; no recipe or item identifiers"),
        DISCORD_TO_MINECRAFT_MESSAGES("discord", "discord_to_minecraft_messages_total", "Discord chat messages forwarded to Minecraft; no content or identities"),
        MINECRAFT_TO_DISCORD_MESSAGES("discord", "minecraft_to_discord_messages_total", "Minecraft chat messages delivered to Discord; no content or identities"),
        MULTIMINE_RUNS_REQUESTED("multiMine", "mining_runs_requested_total", "Mining searches requested after feature eligibility checks"),
        ESSENTIALS_INVENTORY_VIEWS("essentials", "inventory_views_total", "Inventory inspection windows opened; no contents or identities"),
        SIT_SESSIONS_STARTED("sit", "sessions_started_total", "Successful player seating operations"),
        STATUS_UPDATES("status", "status_updates_total", "Successful status assignments; no status text"),
        SERVER_MAINTENANCE_CHANGES("server", "maintenance_changes_total", "Live maintenance state changes; excludes startup restoration"),
        MESSAGE_DELIVERIES("message", "message_deliveries_total", "Private messages delivered to another recipient; no content or identities"),
        MODERATION_ACTIONS("moderation", "actions_total", "Moderation actions reaching the completed-action notification; no targets or reasons"),
        REGIONS_CLAIMS_CREATED("regions", "claims_created_total", "Claims successfully created; no owners, names or geometry"),
        REGIONS_CLAIMS_DELETED("regions", "claims_deleted_total", "Claims successfully deleted; no claim identifiers"),
        DRAGON_ENCOUNTERS_STARTED("remade-ender-dragon", "encounters_started_total", "Custom dragon encounters started; excludes previews"),
        DRAGON_ENCOUNTERS_COMPLETED("remade-ender-dragon", "encounters_completed_total", "Custom dragon encounters ending in dragon death; excludes previews"),
        YV_SMP_AUDIO_LOADS("yv_smp", "audio_loads_total", "Successful audio loads; no source URLs or listener identities"),
        STATS_CONFIG_OPENS("stats", "config_opens_total", "Statistics administration menu opens"),
        STATS_PUSH_SUCCESSES("stats", "push_successes_total", "Successful consented telemetry pushes");

        private final String module;
        private final String key;
        private final String help;

        Event(String module, String key, String help) {
            this.module = module;
            this.key = key;
            this.help = help;
        }

        public String module() { return module; }
        public String key() { return key; }
        public String help() { return help; }
    }

    public static void register(String module, String version) {
        try {
            MODULES.compute(module, (name, previous) -> previous == null
                ? new Registration(version, StatsApi.forModule(name))
                : new Registration(version, previous.publisher()));
        } catch (IllegalArgumentException ignored) {
            // Third-party module display names need not be valid telemetry identifiers.
            return;
        }
        publishModule(module);
    }

    public static void unregister(String module) {
        Registration registration = MODULES.remove(module);
        if (registration != null) safely(registration.publisher()::close);
    }

    public static void clear() {
        MODULES.keySet().forEach(ModuleTelemetry::unregister);
    }

    /** Called after consent/load changes and before exports; never queries Bukkit. */
    public static void publishModules() {
        MODULES.keySet().forEach(ModuleTelemetry::publishModule);
    }

    private static void publishModule(String module) {
        Registration registration = MODULES.get(module);
        if (registration == null) return;
        safely(() -> {
            if (!registration.publisher().isEnabled()) return;
            registration.publisher().setGauge("enabled", "Module is currently loaded and enabled", 1L);
            registration.publisher().setString("version", "Loaded YVtils module version", registration.version());
        });
    }

    public static boolean record(Event event) { return record(event, 1L); }

    public static boolean record(Event event, long amount) {
        if (amount <= 0) return false;
        Registration registration = MODULES.get(event.module());
        if (registration == null) return false;
        try {
            return registration.publisher().incrementCounter(event.key(), event.help(), amount);
        } catch (RuntimeException ignored) {
            // Optional instrumentation must never roll back or interrupt a feature action.
            return false;
        }
    }

    private static void safely(Runnable action) {
        try { action.run(); } catch (RuntimeException ignored) { /* Telemetry is best effort. */ }
    }
}
