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

import java.util.Properties

dependencies {
    compileOnly(project(":common"))
    compileOnly(project(":config-v2"))
    compileOnly(project(":utils"))
    compileOnly(project(":gui-26.1"))

    // Statically bundled into this launcher's main jar (NOT dynamically fetched)
    // so it runs in the main plugin classloader and can see WorldGuard directly
    // via `paper-plugin.yml` `dependencies` - the isolated library tier every
    // dynamically-fetched module lives in cannot. Still togglable via
    // `modules.yml` (see DynamicModuleRegistry `static = true`). See
    // `staticBundledModules` in the root build.gradle.kts.
    implementation(project(":regions-v2"))
}

val moduleVersion = project.version.toString()
val embeddedResourcesDir = layout.buildDirectory.dir("generated/embeddedResources")

val embedRuntime = tasks.register<Jar>("embedRuntime") {
    description = "Embed plugin runtime (common, shaded standalone)"
    dependsOn(":common:shadowJar")
    from(zipTree(project(":common").tasks.named("shadowJar", Jar::class).flatMap { it.archiveFile }))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    destinationDirectory.set(embeddedResourcesDir.map { it.dir("embedded") })
    archiveFileName.set("yvtils-runtime.jar")
}

/*
 * Generates `GeneratedModuleVersions.kt` straight from the repo-wide
 * `gradle/module-versions.properties` (see that file and the root
 * `build.gradle.kts`'s `moduleVersions`), so `DynamicModuleRegistry`'s
 * `KNOWN_MODULES`/`GUI_ARTIFACTS` entries don't need their own hand-typed
 * (and easily stale - see git history) version string literals: every
 * module's version, in one place, feeds both this registry AND that
 * module's own generated `module-version-<name>.properties` resource
 * (`Module.readVersion(...)` in `utils`) from the exact same source file.
 */
val moduleVersionsFile = rootProject.file("gradle/module-versions.properties")
val generatedModuleVersionsDir = layout.buildDirectory.dir("generated/moduleVersions/kotlin")

val generateModuleVersionsKotlin = tasks.register("generateModuleVersionsKotlin") {
    inputs.file(moduleVersionsFile)
    outputs.dir(generatedModuleVersionsDir)

    doLast {
        val properties = Properties().apply {
            moduleVersionsFile.inputStream().use { load(it) }
        }

        val entries = properties.entries
            .map { (key, value) -> key.toString() to value.toString() }
            .sortedBy { it.first }
            .joinToString(",\n") { (name, version) -> "        \"$name\" to \"$version\"" }

        val packageDir = generatedModuleVersionsDir.get().asFile.resolve("yv/tils/core/loader")
        packageDir.mkdirs()

        packageDir.resolve("GeneratedModuleVersions.kt").writeText(
            """
            |/*
            | * GENERATED FILE - do not edit by hand.
            | *
            | * Regenerated from `gradle/module-versions.properties` by the
            | * `generateModuleVersionsKotlin` task (see `core/build.gradle.kts`) - edit
            | * that properties file instead, then rebuild.
            | */
            |
            |package yv.tils.core.loader
            |
            |internal object GeneratedModuleVersions {
            |    val VERSIONS: Map<String, String> = mapOf(
            |$entries
            |    )
            |}
            |
            """.trimMargin()
        )
    }
}

sourceSets {
    main {
        resources.srcDir(embeddedResourcesDir)
        kotlin.srcDir(generatedModuleVersionsDir)
    }
}

tasks.named("processResources") {
    dependsOn(embedRuntime)
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    dependsOn(generateModuleVersionsKotlin)
}

tasks {
    runServer {
        minecraftVersion("26.1.2")

        downloadPlugins {
            modrinth("simple-voice-chat", "bukkit-2.6.20")
            modrinth("worldguard", "7.0.18")
            modrinth("fastasyncworldedit", "2.15.3")
        }
    }

    shadowJar {
        archiveBaseName.set("YVtils")
        archiveVersion.set(moduleVersion)
        archiveClassifier.set("")
        archiveFileName.set("YVtils_v${moduleVersion}.jar")

        manifest {
            attributes["Main-Class"] = "yv.tils.core.YVtils"
        }
    }
}