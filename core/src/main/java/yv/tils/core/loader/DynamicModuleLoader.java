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

import io.papermc.paper.ServerBuildInfo;
import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.JarLibrary;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.repository.RepositoryPolicy;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Paper {@link PluginLoader} that builds this plugin's runtime classpath dynamically:
 *
 * <ol>
 *   <li>Always adds the embedded "runtime bundle" ({@code utils}+{@code config}+{@code common}+
 *       CommandAPI+coroutines+serialization+Kotlin stdlib, built from {@code common}'s own
 *       shadowJar) via a local {@link JarLibrary}. No network required.</li>
 *   <li>Always resolves exactly one {@code gui-<version>} artifact matching the running server's
 *       Minecraft version (see {@link DynamicModuleRegistry#GUI_ARTIFACTS}).</li>
 *   <li>Reads {@code modules.yml} and fetches each enabled, non-static feature module from the
 *       configured Maven registry (Reposilite), with a small set of well-known third-party
 *       repositories as fallbacks.</li>
 * </ol>
 *
 * <p>Written in Java (not Kotlin): Paper loads this class through an isolated bootstrap classloader
 * that only sees this plugin's own main jar, before the runtime bundle above is added. Because the
 * main jar deliberately does NOT bundle its own Kotlin stdlib (see {@code core/build.gradle.kts}),
 * this class and everything it touches at that stage ({@link ModuleConfig},
 * {@link DynamicModuleRegistry}, {@code GeneratedModuleVersions}) must be free of any Kotlin
 * dependency.
 *
 * <p>ARCHITECTURE NOTE: classes added via {@code PluginClasspathBuilder.addLibrary(...)} (the
 * "library tier") CANNOT see classes bundled directly into the plugin's own main jar; visibility
 * only works main jar -&gt; library tier. This is why the runtime bundle (and thus the single shared
 * Kotlin stdlib) is added through this same mechanism instead of being shaded into the main jar -
 * anything with global/static init state, or that must interoperate across the two tiers, has to
 * resolve to exactly one copy.
 */
public final class DynamicModuleLoader implements PluginLoader {

    private static final String EMBEDDED_RUNTIME_RESOURCE = "embedded/yvtils-runtime.jar";
    private static final String EXTRACTED_RUNTIME_FILE_NAME = "yvtils-runtime.jar";

    /**
     * Well-known upstream repositories (id, url) for third-party transitive dependencies feature
     * modules may declare (e.g. {@code discord}'s JDA, {@code gui}'s InvUI, {@code regions}'
     * WorldGuard), added as fallbacks alongside the Reposilite registry on every module's resolver.
     * Maven Central is intentionally the Paper-provided CDN mirror (hitting Central directly is
     * against its ToS and Paper guards against it).
     */
    private static final List<String[]> THIRD_PARTY_REPOSITORIES = List.of(
            new String[] {"maven-central", MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR},
            new String[] {"maven-snapshots", "https://s01.oss.sonatype.org/content/repositories/snapshots/"},
            new String[] {"papermc", "https://repo.papermc.io/repository/maven-public/"},
            new String[] {"xenondevs", "https://repo.xenondevs.xyz/releases"},
            new String[] {"maxhenkel", "https://maven.maxhenkel.de/repository/public"},
            new String[] {"lavalink", "https://maven.lavalink.dev/releases"},
            new String[] {"enginehub", "https://maven.enginehub.org/repo"});

    /** Reposilite registry URL; overridable via {@code REPOSILITE_URL} for local development. */
    private static String repositoryUrl() {
        String env = System.getenv("REPOSILITE_URL");
        return (env != null) ? env : "https://reposilite-registry.yvtils.net/releases";
    }

    /**
     * Opt-in local-dev escape hatch ({@code YVTILS_DEV_SKIP_CHECKSUMS=true}) that skips checksum
     * validation and always re-checks for updates. Never enabled for the real registry.
     */
    private static boolean devSkipChecksums() {
        String value = System.getenv("YVTILS_DEV_SKIP_CHECKSUMS");
        return value != null && value.equalsIgnoreCase("true");
    }

    @Override
    public void classloader(@NotNull PluginClasspathBuilder classpathBuilder) {
        var logger = classpathBuilder.getContext().getLogger();
        Path sharedDirectory = ModuleConfig.sharedDataDirectory(classpathBuilder.getContext().getDataDirectory());
        String repositoryUrl = repositoryUrl();
        boolean skipChecksums = devSkipChecksums();

        try {
            Files.createDirectories(sharedDirectory);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        addEmbeddedRuntimeBundle(classpathBuilder, sharedDirectory);
        addGuiModule(classpathBuilder, repositoryUrl, skipChecksums);

        List<String> enabledModules = ModuleConfig.readEnabledModules(sharedDirectory);
        logger.info("[DynamicModuleLoader] Shared data directory: " + sharedDirectory);
        logger.info("[DynamicModuleLoader] Enabled modules from config: " + enabledModules);
        logger.info("[DynamicModuleLoader] Resolving against repository: " + repositoryUrl);

        if (skipChecksums) {
            logger.warn("[DynamicModuleLoader] YVTILS_DEV_SKIP_CHECKSUMS is enabled - checksum validation is "
                    + "DISABLED for module resolution. This must only ever be used for local development.");
        }

        for (String moduleName : enabledModules) {
            DynamicModuleRegistry.ModuleArtifact artifact = DynamicModuleRegistry.KNOWN_MODULES.get(moduleName);

            if (artifact == null) {
                logger.warn("[DynamicModuleLoader] Unknown module '" + moduleName + "', skipping.");
                continue;
            }

            if (artifact.isStatic()) {
                // Shaded into this launcher's own main jar - there's nothing to resolve over the
                // network. DynamicModuleDriver still instantiates it from the main jar. See
                // DynamicModuleRegistry.ModuleArtifact#isStatic.
                logger.info("[DynamicModuleLoader] Module '" + moduleName
                        + "' is statically bundled - not resolved from the registry.");
                continue;
            }

            MavenLibraryResolver resolver = newModuleResolver(repositoryUrl, skipChecksums);
            resolver.addDependency(new Dependency(
                    new DefaultArtifact(DynamicModuleRegistry.GROUP_ID + ":" + artifact.getArtifactId()
                            + ":" + artifact.getVersion()),
                    null));

            classpathBuilder.addLibrary(resolver);
            logger.info("[DynamicModuleLoader] Queued module '" + moduleName + "' ("
                    + DynamicModuleRegistry.GROUP_ID + ":" + artifact.getArtifactId() + ":" + artifact.getVersion()
                    + ") for resolution.");
        }
    }

    /**
     * Resolves and adds exactly one {@code gui-<version>} artifact matching the running server's
     * Minecraft version, unconditionally (not driven by {@code modules.yml}).
     */
    private void addGuiModule(PluginClasspathBuilder classpathBuilder, String repositoryUrl, boolean skipChecksums) {
        var logger = classpathBuilder.getContext().getLogger();
        String minecraftVersionId = ServerBuildInfo.buildInfo().minecraftVersionId();
        String minorVersion = DynamicModuleRegistry.minecraftMinorVersion(minecraftVersionId);

        if (!DynamicModuleRegistry.GUI_ARTIFACTS.containsKey(minorVersion)) {
            logger.warn("[DynamicModuleLoader] No 'gui' artifact registered for Minecraft " + minecraftVersionId
                    + " (minor version '" + minorVersion + "') - falling back to the "
                    + DynamicModuleRegistry.GUI_FALLBACK_MINECRAFT_VERSION + " build. GUI-dependent features may not "
                    + "work correctly. Add a 'gui-" + minorVersion + "' entry to DynamicModuleRegistry.GUI_ARTIFACTS "
                    + "once InvUI publishes support for this version.");
        }

        DynamicModuleRegistry.ModuleArtifact artifact = DynamicModuleRegistry.guiArtifactFor(minecraftVersionId);
        MavenLibraryResolver resolver = newModuleResolver(repositoryUrl, skipChecksums);
        resolver.addDependency(new Dependency(
                new DefaultArtifact(DynamicModuleRegistry.GROUP_ID + ":" + artifact.getArtifactId()
                        + ":" + artifact.getVersion()),
                null));

        classpathBuilder.addLibrary(resolver);
        logger.info("[DynamicModuleLoader] Queued gui module for Minecraft " + minecraftVersionId + " ("
                + DynamicModuleRegistry.GROUP_ID + ":" + artifact.getArtifactId() + ":" + artifact.getVersion()
                + ") for resolution.");
    }

    /**
     * Builds a fresh {@link MavenLibraryResolver} pointed at the configured Reposilite registry plus
     * {@link #THIRD_PARTY_REPOSITORIES}.
     */
    private MavenLibraryResolver newModuleResolver(String repositoryUrl, boolean skipChecksums) {
        MavenLibraryResolver resolver = new MavenLibraryResolver();

        RemoteRepository.Builder repositoryBuilder =
                new RemoteRepository.Builder("yvtils-registry", "default", repositoryUrl);
        if (skipChecksums) {
            repositoryBuilder.setPolicy(new RepositoryPolicy(
                    true,
                    RepositoryPolicy.UPDATE_POLICY_ALWAYS,
                    RepositoryPolicy.CHECKSUM_POLICY_IGNORE));
        }
        resolver.addRepository(repositoryBuilder.build());

        for (String[] repository : THIRD_PARTY_REPOSITORIES) {
            resolver.addRepository(new RemoteRepository.Builder(repository[0], "default", repository[1]).build());
        }

        return resolver;
    }

    /**
     * Extracts the embedded runtime bundle to disk and adds it via a local {@link JarLibrary}, so it
     * lands in the same classloader tier as the Maven-resolved feature modules. No network required.
     */
    private void addEmbeddedRuntimeBundle(PluginClasspathBuilder classpathBuilder, Path dataDirectory) {
        var logger = classpathBuilder.getContext().getLogger();
        Path extractedPath = dataDirectory.resolve(EXTRACTED_RUNTIME_FILE_NAME);

        try (InputStream resourceStream =
                     DynamicModuleLoader.class.getClassLoader().getResourceAsStream(EMBEDDED_RUNTIME_RESOURCE)) {
            if (resourceStream == null) {
                logger.error("[DynamicModuleLoader] Embedded runtime bundle resource '" + EMBEDDED_RUNTIME_RESOURCE
                        + "' not found! This plugin jar was likely not built correctly (missing embedRuntime task "
                        + "output).");
                return;
            }
            Files.copy(resourceStream, extractedPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        classpathBuilder.addLibrary(new JarLibrary(extractedPath));
        logger.info("[DynamicModuleLoader] Added embedded runtime bundle from " + extractedPath);
    }
}
