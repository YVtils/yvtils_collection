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

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Minimal, dependency-free reader/writer for the dynamic-module config file
 * ({@code modules.yml}).
 *
 * <p>Written in Java (not Kotlin) for the same reason as
 * {@link DynamicModuleRegistry}: it is invoked from Paper's isolated
 * {@code PluginLoader} bootstrap classloader (via {@link DynamicModuleLoader}),
 * which only sees this plugin's own main jar and where the Kotlin stdlib must
 * not be required. It also intentionally avoids the {@code config} module's
 * YAML helpers, which aren't available that early.
 *
 * <p>File format is a minimal YAML-like {@code moduleName: true}/{@code false}
 * per line (blank lines and {@code #} comments ignored).
 *
 * <p>Module visibility has three states, driven by {@link DynamicModuleRegistry}:
 * normal (listed + honored), {@code hidden} (never listed nor honored, even if
 * hand-added), and {@code manual} (not listed, but honored when hand-added and
 * preserved on rewrites).
 */
public final class ModuleConfig {

    private ModuleConfig() {
    }

    private static final String FILE_NAME = "modules.yml";
    private static final String SHARED_DIRECTORY_NAME = "yvtils";

    /**
     * Computes the shared {@code plugins/yvtils} data directory used by every core (a sibling of the
     * given per-plugin data directory), so the runtime bundle and {@code modules.yml} are shared
     * across installed cores. Works without {@code Bukkit}/server access (not available when
     * {@link DynamicModuleLoader} runs).
     */
    public static Path sharedDataDirectory(Path perPluginDataDirectory) {
        Path parent = perPluginDataDirectory.getParent();
        Path base = (parent != null) ? parent : perPluginDataDirectory;
        return base.resolve(SHARED_DIRECTORY_NAME);
    }

    /** @see #readEnabledModules(Path, Collection, Set) */
    public static List<String> readEnabledModules(Path dataDirectory) {
        return readEnabledModules(dataDirectory, DynamicModuleRegistry.KNOWN_MODULES.keySet(), Set.of());
    }

    /**
     * Reads the set of enabled module names from {@code <dataDirectory>/modules.yml}, generating a
     * default file (listing every listable module, with {@code defaultEnabledModules} on) on first
     * boot. {@code hidden} modules are always excluded from the result even if hand-added;
     * {@code manual} modules are honored when present.
     */
    public static List<String> readEnabledModules(
            Path dataDirectory,
            Collection<String> knownModules,
            Set<String> defaultEnabledModules) {
        Path file = dataDirectory.resolve(FILE_NAME);
        try {
            if (!Files.exists(file)) {
                Files.createDirectories(dataDirectory);
                Files.writeString(file, buildDefaultFile(listableModules(knownModules), defaultEnabledModules));
            }

            List<String> result = new ArrayList<>();
            for (String[] entry : parse(Files.readAllLines(file))) {
                String name = entry[0];
                boolean enabled = Boolean.parseBoolean(entry[1]);
                if (enabled && !isHidden(name)) {
                    result.add(name);
                }
            }
            return result;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** @see #writeEnabledModules(Path, Set, Collection) */
    public static void writeEnabledModules(Path dataDirectory, Set<String> enabledModules) {
        writeEnabledModules(dataDirectory, enabledModules, DynamicModuleRegistry.KNOWN_MODULES.keySet());
    }

    /**
     * Regenerates {@code <dataDirectory>/modules.yml} listing every listable module with
     * {@code true}/{@code false} from {@code enabledModules}. Enabled {@code manual} modules are
     * preserved (appended) so a rewrite doesn't silently drop them; {@code hidden} modules are never
     * written.
     *
     * <p>Toggling only takes effect after a server restart, since modules are resolved/instantiated
     * once at boot.
     */
    public static void writeEnabledModules(Path dataDirectory, Set<String> enabledModules, Collection<String> knownModules) {
        try {
            Files.createDirectories(dataDirectory);
            Files.writeString(
                    dataDirectory.resolve(FILE_NAME),
                    buildDefaultFile(listableModules(knownModules), enabledModules));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /**
     * Flips a single module's enabled state, leaving every other module's state untouched. A no-op
     * for {@code hidden} modules.
     */
    public static void setModuleEnabled(Path dataDirectory, String moduleName, boolean enabled) {
        if (isHidden(moduleName)) {
            return;
        }

        Set<String> current = new LinkedHashSet<>(readEnabledModules(dataDirectory));
        if (enabled) {
            current.add(moduleName);
        } else {
            current.remove(moduleName);
        }

        writeEnabledModules(dataDirectory, current);
    }

    private static boolean isHidden(String moduleName) {
        DynamicModuleRegistry.ModuleArtifact artifact = DynamicModuleRegistry.KNOWN_MODULES.get(moduleName);
        return artifact != null && artifact.isHidden();
    }

    private static boolean isManual(String moduleName) {
        DynamicModuleRegistry.ModuleArtifact artifact = DynamicModuleRegistry.KNOWN_MODULES.get(moduleName);
        return artifact != null && artifact.isManual();
    }

    /**
     * Modules that appear in the auto-generated {@code modules.yml} and the in-game GUI: everything
     * except {@code hidden} ones (never listed nor honored) and {@code manual} ones (not listed, but
     * honored when hand-added).
     */
    private static List<String> listableModules(Collection<String> knownModules) {
        List<String> result = new ArrayList<>();
        for (String name : knownModules) {
            if (!isHidden(name) && !isManual(name)) {
                result.add(name);
            }
        }
        return result;
    }

    private static String buildDefaultFile(Collection<String> listableModules, Set<String> enabledModules) {
        StringBuilder sb = new StringBuilder();
        sb.append("# YVtils dynamic module configuration.\n");
        sb.append("# Set each module to true or false, then restart the server for changes to take effect.\n");
        sb.append("# Note: some internal modules (e.g. migration) are intentionally not\n");
        sb.append("# listed here and cannot be enabled through this file. The `gui` module isn't\n");
        sb.append("# listed either, but for a different reason - it isn't a togglable feature at\n");
        sb.append("# all; `core` always resolves exactly one `gui-<version>` build matching the\n");
        sb.append("# server's Minecraft version (see DynamicModuleRegistry.GUI_ARTIFACTS).\n");
        sb.append("#\n");
        sb.append("# Some optional modules are not listed here automatically - you can still enable\n");
        sb.append("# one by adding its name below by hand with `true` (see the YVtils docs for the\n");
        sb.append("# list of available optional modules).\n");
        sb.append("\n");

        // Manual (opt-in) modules aren't listed by default, but once an admin has enabled one by hand
        // we keep it in the file so a GUI-driven rewrite doesn't silently drop it.
        List<String> names = new ArrayList<>(listableModules);
        for (String name : enabledModules) {
            if (isManual(name) && !isHidden(name) && !names.contains(name)) {
                names.add(name);
            }
        }

        for (String name : names) {
            sb.append(name).append(": ").append(enabledModules.contains(name)).append("\n");
        }

        return sb.toString();
    }

    /**
     * Parses {@code moduleName: true}/{@code false} lines into {@code [name, "true"|"false"]} pairs.
     * Unknown/malformed values are treated as {@code false}.
     */
    private static List<String[]> parse(List<String> lines) {
        List<String[]> result = new ArrayList<>();
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }

            int separatorIndex = line.indexOf(':');
            if (separatorIndex == -1) {
                continue;
            }

            String name = line.substring(0, separatorIndex).trim();
            String value = line.substring(separatorIndex + 1).trim();
            if (name.isEmpty()) {
                continue;
            }

            result.add(new String[] {name, String.valueOf(value.equalsIgnoreCase("true"))});
        }
        return result;
    }
}
