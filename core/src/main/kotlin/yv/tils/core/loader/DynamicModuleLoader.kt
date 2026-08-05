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
import yv.tils.core.loader.DynamicModuleLoader.Companion.THIRD_PARTY_REPOSITORIES
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
 * 2. Reads `modules.yml` from the shared `plugins/yvtils` data directory
 *    (see [ModuleConfig.sharedDataDirectory] - shared across every installed
 *    core, not this specific plugin's own data folder) and fetches each
 *    enabled feature module from the configured Maven repository (Reposilite)
 *    via [MavenLibraryResolver]. Each module's resolver is also given a small,
 *    fixed set of well-known third-party repositories ([THIRD_PARTY_REPOSITORIES])
 *    as fallbacks, so transitive third-party dependencies (e.g. `discord`'s JDA,
 *    `gui-v2`'s InvUI) resolve directly instead of depending on Reposilite being
 *    configured as a proxy/mirror for every upstream host a module might need.
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

        /**
         * Well-known upstream repositories for *third-party* (non-YVtils) transitive
         * dependencies that feature modules may declare (e.g. `discord`'s JDA, or
         * `gui-v2`'s InvUI). These are added as fallbacks alongside the Reposilite
         * registry on every module's resolver, so resolution does NOT depend on
         * Reposilite being configured as a proxy/mirror for every possible upstream
         * host a module might need - each resolver can reach these repositories
         * directly instead.
         *
         * Add an entry here whenever a new feature module introduces a dependency
         * hosted somewhere that isn't already covered (Maven Central covers the vast
         * majority of the Java/Kotlin ecosystem, including JDA).
         *
         * NOTE: Maven Central itself is intentionally resolved through Paper's
         * [MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR] (a Google-hosted CDN
         * mirror), not the raw `repo.maven.apache.org` URL - hitting Central
         * directly like that is against its Terms of Service and Paper explicitly
         * warns/guards against it (see `MavenLibraryResolver.addRepository`).
         */
        private val THIRD_PARTY_REPOSITORIES: List<Pair<String, String>> = listOf(
            "maven-central" to MavenLibraryResolver.MAVEN_CENTRAL_DEFAULT_MIRROR,
            "maven-snapshots" to "https://s01.oss.sonatype.org/content/repositories/snapshots/",
            "papermc" to "https://repo.papermc.io/repository/maven-public/",
            "xenondevs" to "https://repo.xenondevs.xyz/releases",
        )
    }

    override fun classloader(classpathBuilder: PluginClasspathBuilder) {
        val logger = classpathBuilder.context.logger
        val sharedDirectory = ModuleConfig.sharedDataDirectory(classpathBuilder.context.dataDirectory)
        val repositoryUrl = repositoryUrl()
        val skipChecksums = devSkipChecksums()

        Files.createDirectories(sharedDirectory)
        addEmbeddedRuntimeBundle(classpathBuilder, sharedDirectory)

        val enabledModules = ModuleConfig.readEnabledModules(sharedDirectory)
        logger.info("[DynamicModuleLoader] Shared data directory: $sharedDirectory")
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
                    RepositoryPolicy(
                        true,
                        RepositoryPolicy.UPDATE_POLICY_ALWAYS,
                        RepositoryPolicy.CHECKSUM_POLICY_IGNORE
                    )
                )
            }
            resolver.addRepository(repositoryBuilder.build())

            // Fallbacks for third-party transitive dependencies (JDA, InvUI, ...) -
            // see THIRD_PARTY_REPOSITORIES for why this is needed in addition to
            // the registry above.
            THIRD_PARTY_REPOSITORIES.forEach { (id, url) ->
                resolver.addRepository(RemoteRepository.Builder(id, "default", url).build())
            }

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
            logger.error(
                "[DynamicModuleLoader] Embedded runtime bundle resource '$EMBEDDED_RUNTIME_RESOURCE' not found! " +
                        "This plugin jar was likely not built correctly (missing embedRuntime task output)."
            )
            return
        }

        resourceStream.use { input ->
            Files.copy(input, extractedPath, StandardCopyOption.REPLACE_EXISTING)
        }

        classpathBuilder.addLibrary(JarLibrary(extractedPath))
        logger.info("[DynamicModuleLoader] Added embedded runtime bundle from $extractedPath")
    }
}
