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

import io.papermc.paper.plugin.loader.PluginClasspathBuilder
import io.papermc.paper.plugin.loader.PluginLoader
import io.papermc.paper.plugin.loader.library.impl.JarLibrary
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.repository.RepositoryPolicy
import java.nio.file.Files
import java.nio.file.StandardCopyOption

/**
 * Paper `PluginLoader` that builds this plugin's runtime classpath dynamically:
 *
 * 1. Always adds the embedded "runtime bundle" (`utils`+`config`+`common`+
 *    CommandAPI+coroutines+serialization, built from `common`'s own shadowJar
 *    and embedded as a plugin resource - see `test-core/build.gradle.kts`) via
 *    a local [JarLibrary]. No network access is required for this; it's always
 *    bundled inside this plugin's own jar.
 * 2. Reads `modules.yml` from the plugin's data directory and fetches each
 *    enabled feature module from the configured Maven repository (Reposilite)
 *    via [MavenLibraryResolver].
 *
 * Each feature module is registered as its OWN `MavenLibraryResolver` instance
 * (rather than one shared resolver with multiple dependencies added to it), so
 * that a failure resolving one module does not prevent the others from being
 * added to the classpath.
 *
 * IMPORTANT (see Paper's `PluginLoader` docs): this class is loaded through a
 * separate/isolated classloader from the actual plugin instance. Any static
 * state set here will NOT be visible from `YVtils.onLoad()`. Do not rely on
 * shared mutable state between this class and the rest of the plugin - only
 * stateless, file-based communication (like [ModuleConfig]) works reliably.
 *
 * ARCHITECTURE NOTE (validated via a dedicated spike - see git history):
 * classes/libraries added via `PluginClasspathBuilder.addLibrary(...)` (this
 * "library tier") CANNOT see classes bundled directly into the plugin's own
 * main jar (via a normal Gradle `implementation` dependency) - visibility only
 * works in the other direction (main jar -> library tier). This is exactly why
 * the runtime bundle below is added through this same mechanism instead of
 * being a plain `implementation` dependency of this module: dynamically-fetched
 * feature modules need to resolve `Module.YVtilsModule` (and everything else in
 * utils/config/common), and the only way for that to work is for both to live
 * in the same shared classloader tier. The same reasoning applies to anything
 * with global/static init state (e.g. CommandAPI's `.onLoad()`) - it must be
 * resolved in exactly one place, not once in the main jar and once more
 * transitively via a fetched module's own POM.
 */
class DynamicModuleLoader : PluginLoader {
    companion object {
        private const val EMBEDDED_RUNTIME_RESOURCE = "embedded/yvtils-runtime.jar"
        private const val EXTRACTED_RUNTIME_FILE_NAME = "yvtils-runtime.jar"

        /**
         * Repository used to resolve feature module artifacts.
         *
         * Defaults to the real, self-hosted Reposilite registry. Set the
         * `REPOSILITE_URL` environment variable to override this (e.g. for
         * local development against a throwaway repository).
         */
        private fun repositoryUrl(): String =
            System.getenv("REPOSILITE_URL")
                ?: "https://reposilite-registry.yvtils.net/releases"

        /**
         * Explicit, opt-in escape hatch for local development: when set to
         * `true`, skips checksum validation and always re-checks for updates
         * on the configured repository. Never enabled unless a developer
         * deliberately sets this - the real registry always has valid
         * checksums and this must never be relaxed for it.
         *
         * This exists because `./gradlew publishToMavenLocal` doesn't produce
         * `.sha1`/`.md5` files the way a real Maven repository does; prefer
         * running `./gradlew publishAllModulesLocally` (which generates them)
         * over relying on this flag, but this is a faster escape hatch while
         * iterating. See docs/migrating-to-dynamic-modules.md.
         */
        private fun devSkipChecksums(): Boolean =
            System.getenv("YVTILS_DEV_SKIP_CHECKSUMS")?.equals("true", ignoreCase = true) == true
    }

    override fun classloader(classpathBuilder: PluginClasspathBuilder) {
        val logger = classpathBuilder.context.logger
        val dataDirectory = classpathBuilder.context.dataDirectory
        val repositoryUrl = repositoryUrl()
        val skipChecksums = devSkipChecksums()

        Files.createDirectories(dataDirectory)
        addEmbeddedRuntimeBundle(classpathBuilder, dataDirectory)

        val enabledModules = ModuleConfig.readEnabledModules(dataDirectory)
        logger.info("[DynamicModuleLoader] Enabled modules from config: $enabledModules")
        logger.info("[DynamicModuleLoader] Resolving against repository: $repositoryUrl")

        if (skipChecksums) {
            logger.warn(
                "[DynamicModuleLoader] YVTILS_DEV_SKIP_CHECKSUMS is enabled - checksum validation is " +
                    "DISABLED for module resolution. This must only ever be used for local development."
            )
        }

        for (moduleName in enabledModules) {
            val artifact = DynamicModuleRegistry.KNOWN_MODULES[moduleName]

            if (artifact == null) {
                logger.warn("[DynamicModuleLoader] Unknown module '$moduleName', skipping.")
                continue
            }

            val resolver = MavenLibraryResolver()

            val repositoryBuilder = RemoteRepository.Builder("yvtils-registry", "default", repositoryUrl)
            if (skipChecksums) {
                repositoryBuilder.setPolicy(
                    RepositoryPolicy(true, RepositoryPolicy.UPDATE_POLICY_ALWAYS, RepositoryPolicy.CHECKSUM_POLICY_IGNORE)
                )
            }
            resolver.addRepository(repositoryBuilder.build())

            resolver.addDependency(
                Dependency(
                    DefaultArtifact("${DynamicModuleRegistry.GROUP_ID}:${artifact.artifactId}:${artifact.version}"),
                    null
                )
            )

            classpathBuilder.addLibrary(resolver)
            logger.info(
                "[DynamicModuleLoader] Queued module '$moduleName' " +
                    "(${DynamicModuleRegistry.GROUP_ID}:${artifact.artifactId}:${artifact.version}) for resolution."
            )
        }
    }

    /**
     * Extracts the embedded runtime bundle (utils+config+common+CommandAPI+
     * coroutines+serialization) to disk and adds it via a local [JarLibrary],
     * so it lands in the same classloader tier as the Maven-resolved feature
     * modules above. No network access required - this is always bundled
     * inside this plugin's own jar.
     */
    private fun addEmbeddedRuntimeBundle(classpathBuilder: PluginClasspathBuilder, dataDirectory: java.nio.file.Path) {
        val logger = classpathBuilder.context.logger
        val extractedPath = dataDirectory.resolve(EXTRACTED_RUNTIME_FILE_NAME)

        val resourceStream = javaClass.classLoader.getResourceAsStream(EMBEDDED_RUNTIME_RESOURCE)
        if (resourceStream == null) {
            logger.error("[DynamicModuleLoader] Embedded runtime bundle resource '$EMBEDDED_RUNTIME_RESOURCE' not found! " +
                "This plugin jar was likely not built correctly (missing embedRuntime task output).")
            return
        }

        resourceStream.use { input ->
            Files.copy(input, extractedPath, StandardCopyOption.REPLACE_EXISTING)
        }

        classpathBuilder.addLibrary(JarLibrary(extractedPath))
        logger.info("[DynamicModuleLoader] Added embedded runtime bundle from $extractedPath")
    }
}
