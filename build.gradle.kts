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

import io.papermc.paperweight.userdev.PaperweightUserDependenciesExtension
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinMonorepoVersion = "2.4.10"

    kotlin("jvm") version kotlinMonorepoVersion apply false
    kotlin("plugin.serialization") version kotlinMonorepoVersion apply false
    id("com.gradleup.shadow") version "9.6.1" apply false
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" apply false
    id("xyz.jpenilla.run-paper") version "3.0.2" apply false
}

allprojects {
    group = "yv.tils"
    version = "26.08.01"

    repositories {
        mavenCentral()

        maven {
            name = "maven-snapshots"
            url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
        }

        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }

        maven {
            name = "xenondevs"
            url = uri("https://repo.xenondevs.xyz/releases")
        }

        maven("https://maven.maxhenkel.de/repository/public")
        maven("https://maven.lavalink.dev/releases")
        maven("https://jitpack.io")
    }
}

/*
 * Feature modules that are published as standalone Maven artifacts to the
 * self-hosted Reposilite registry (https://reposilite-registry.yvtils.net/releases).
 *
 * This allows a "core" launcher plugin to fetch only the modules it needs at
 * runtime (via Paper's PluginLoader + MavenLibraryResolver), instead of every
 * product shading a private copy of every module into its own jar.
 *
 * `utils`, `config` and `common` are intentionally NOT published here: they are
 * always bundled directly into the launcher/core plugin (as a locally embedded
 * `JarLibrary`, built from `common`'s own shadowJar output - see
 * `test-core/build.gradle.kts`), so feature modules only ever depend on them via
 * `compileOnly` and must not declare them as a fetchable Maven dependency.
 *
 * Versioning is independent per module (NOT tied to the root project version) -
 * set `version` at the top of each published module's own build.gradle.kts once
 * it's ready to be versioned/published on its own.
 */
val publishableModules = setOf(
    "discord",
    "regions",
    "multiMine",
    "essentials",
    "sit",
    "status",
    "server",
    "message",
    "moderation",
    "stats",
)

/*
 * "core"/launcher modules that use the new dynamic PluginLoader pattern
 * (fetching feature modules at runtime instead of shading them all in).
 *
 * Like the modules in `publishableModules`, these must NOT shade CommandAPI/
 * coroutines/serialization/utils/config/common directly into their own jar -
 * they get all of that from the same embedded-JarLibrary + MavenLibraryResolver
 * mechanism as the feature modules, so everything lands in one shared
 * classloader tier. See `DynamicModuleLoader` for the full explanation.
 */
val dynamicCoreModules = setOf(
    "test-core",
    "core"
)

// Modules that must rely on the shared runtime tier (embedded JarLibrary +
// MavenLibraryResolver) instead of shading CommandAPI/coroutines/serialization
// directly into their own jar.
val usesSharedRuntimeTier = publishableModules + dynamicCoreModules

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.serialization")
        plugin("com.gradleup.shadow")
        plugin("io.papermc.paperweight.userdev")
        plugin("xyz.jpenilla.run-paper")
    }

    val commandAPIVersion = "12.0.0"

    // Modules going through the shared runtime tier get CommandAPI/coroutines/
    // serialization as `compileOnly` (available at compile time, but NOT shaded
    // into their own jar / declared as a Maven dependency) since those are
    // provided at runtime by the embedded runtime bundle instead. Everything
    // else (standalone `*-core` launchers, utils/config/common themselves)
    // keeps shading them directly via `implementation`, unchanged.
    val runtimeDependencyScope = if (name in usesSharedRuntimeTier) "compileOnly" else "implementation"

    dependencies {
        // Paper API dependency
        the<PaperweightUserDependenciesExtension>().paperDevBundle("26.1.2.build.+")

        // CommandAPI dependencies
        add(runtimeDependencyScope, "dev.jorel:commandapi-paper-shade:$commandAPIVersion")
        add(runtimeDependencyScope, "dev.jorel:commandapi-kotlin-paper:$commandAPIVersion")

        // Other
        add(runtimeDependencyScope, "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
        add(runtimeDependencyScope, "org.jetbrains.kotlinx:kotlinx-serialization-json:1.11.0")
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_25)
        }
    }

    tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
        javaLauncher.set(project.extensions.getByType<JavaToolchainService>().launcherFor {
            languageVersion.set(JavaLanguageVersion.of(25))
        })
    }
}

subprojects {
    if (name in publishableModules) {
        apply(plugin = "maven-publish")

        configure<PublishingExtension> {
            repositories {
                maven {
                    name = "reposilite"
                    url = uri(
                        (System.getenv("REPOSILITE_URL")
                            ?: findProperty("reposiliteUrl") as String?
                            ?: "https://reposilite-registry.yvtils.net/releases")
                    )

                    credentials {
                        username = System.getenv("REPOSILITE_USERNAME")
                            ?: findProperty("reposiliteUsername") as String?
                        password = System.getenv("REPOSILITE_TOKEN")
                            ?: findProperty("reposilitePassword") as String?
                    }

                    authentication {
                        create<BasicAuthentication>("basic")
                    }
                }
            }

            publications {
                create<MavenPublication>("maven") {
                    groupId = "yv.yvtils"
                    artifactId = project.name
                    version = project.version.toString()

                    from(components["java"])
                }
            }
        }
    }
}

/*
 * Local development helpers.
 *
 * Reposilite (and any real Maven repository) generates .sha1/.md5 checksum
 * files alongside every artifact, and Paper's MavenLibraryResolver/Aether
 * refuses to resolve an artifact without them. `publishToMavenLocal` does NOT
 * generate these, so testing a not-yet-published module against a throwaway
 * local repository needs them created by hand.
 *
 * `publishAllModulesLocally` does the whole "make a module resolvable from a
 * local HTTP-served repo" dance in one command:
 *   ./gradlew publishAllModulesLocally
 *   jwebserver -p 8095 -d "$HOME/.m2/repository"
 *   REPOSILITE_URL="http://127.0.0.1:8095/" ./gradlew :test-core:runServer
 *
 * See docs/migrating-to-dynamic-modules.md for the full local testing guide.
 */
fun sha1Hex(file: File): String = digestHex(file, "SHA-1")
fun md5Hex(file: File): String = digestHex(file, "MD5")

fun digestHex(file: File, algorithm: String): String {
    val digest = java.security.MessageDigest.getInstance(algorithm)
    file.inputStream().use { input ->
        val buffer = ByteArray(8192)
        var read: Int
        while (input.read(buffer).also { read = it } != -1) {
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString("") { "%02x".format(it) }
}

val generateLocalMavenChecksums by tasks.registering {
    group = "yvtils dev"
    description = "(Re)generates .sha1/.md5 files for locally-published yv.yvtils artifacts " +
            "(publishToMavenLocal doesn't produce them, but Aether requires them for resolution). " +
            "Always overwrites, since a republish under the same version changes the artifact's " +
            "content/hash but does not remove a previously-generated checksum file."

    doLast {
        val repoDir = File(System.getProperty("user.home"), ".m2/repository/yv/yvtils")

        if (!repoDir.exists()) {
            logger.lifecycle("No local yv.yvtils artifacts found at $repoDir - publish something first.")
            return@doLast
        }

        var generated = 0

        repoDir.walkTopDown()
            .filter { it.isFile && !it.name.endsWith(".sha1") && !it.name.endsWith(".md5") }
            .forEach { file ->
                val sha1File = File(file.parentFile, "${file.name}.sha1")
                val md5File = File(file.parentFile, "${file.name}.md5")

                // Always (re)write: a republish under the same version overwrites `file`'s
                // content, so a pre-existing checksum here is not necessarily still correct.
                sha1File.writeText(sha1Hex(file))
                md5File.writeText(md5Hex(file))
                generated += 2
            }

        logger.lifecycle("(Re)generated $generated checksum file(s) under $repoDir")
    }
}

val publishAllModulesLocally by tasks.registering {
    group = "yvtils dev"
    description = "Publishes every feature module to the local Maven cache (~/.m2/repository) and " +
            "generates the checksum files Aether needs, for local development against a temporary " +
            "HTTP-served repository (see docs/migrating-to-dynamic-modules.md)."

    publishableModules.forEach { moduleName ->
        dependsOn(":$moduleName:publishToMavenLocal")
    }

    finalizedBy(generateLocalMavenChecksums)
}
