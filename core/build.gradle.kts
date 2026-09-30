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
    // so its direct WorldGuard API calls resolve through the main plugin classloader's
    // `paper-plugin.yml` dependency with `join-classpath: true`, rather than the
    // isolated library tier used by dynamically fetched modules. Still togglable via
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
 * Generates `GeneratedModuleVersions.java` straight from the repo-wide
 * `gradle/module-versions.properties` (see that file and the root
 * `build.gradle.kts`'s `moduleVersions`), so `DynamicModuleRegistry`'s
 * `KNOWN_MODULES`/`GUI_ARTIFACTS` entries don't need their own hand-typed
 * (and easily stale - see git history) version string literals: every
 * module's version, in one place, feeds both this registry AND that
 * module's own generated `module-version-<name>.properties` resource
 * (`Module.readVersion(...)` in `utils`) from the exact same source file.
 *
 * Emitted as Java (not Kotlin) because it is consumed by the Java loader
 * classes (`DynamicModuleRegistry`/`ModuleConfig`/`DynamicModuleLoader`), which
 * must stay free of any Kotlin dependency - see those classes' Javadoc and the
 * shadowJar Kotlin exclusion below.
 */
val moduleVersionsFile = rootProject.file("gradle/module-versions.properties")
val generatedModuleVersionsDir = layout.buildDirectory.dir("generated/moduleVersions/java")

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
            .joinToString("\n") { (name, version) -> "        modules.put(\"$name\", \"$version\");" }

        val packageDir = generatedModuleVersionsDir.get().asFile.resolve("yv/tils/core/loader")
        packageDir.mkdirs()

        packageDir.resolve("GeneratedModuleVersions.java").writeText(
            """
            |/*
            | * GENERATED FILE - do not edit by hand.
            | *
            | * Regenerated from `gradle/module-versions.properties` by the
            | * `generateModuleVersionsKotlin` task (see `core/build.gradle.kts`) - edit
            | * that properties file instead, then rebuild.
            | */
            |
            |package yv.tils.core.loader;
            |
            |import java.util.Collections;
            |import java.util.LinkedHashMap;
            |import java.util.Map;
            |
            |final class GeneratedModuleVersions {
            |    private GeneratedModuleVersions() {
            |    }
            |
            |    static final Map<String, String> VERSIONS = build();
            |
            |    private static Map<String, String> build() {
            |        LinkedHashMap<String, String> modules = new LinkedHashMap<>();
            |$entries
            |        return Collections.unmodifiableMap(modules);
            |    }
            |}
            |
            """.trimMargin()
        )
    }
}

sourceSets {
    main {
        resources.srcDir(embeddedResourcesDir)
        java.srcDir(generatedModuleVersionsDir)
    }
}

tasks.named("processResources") {
    dependsOn(embedRuntime)
}

tasks.named("compileJava") {
    dependsOn(generateModuleVersionsKotlin)
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

        // Do NOT bundle the Kotlin stdlib into the main jar: it's provided at runtime by the shared
        // runtime bundle (the library tier - see DynamicModuleLoader). A second copy here makes
        // main-jar Kotlin classes (e.g. the statically-bundled regions-v2's config data classes)
        // collide with the library tier when reflected by library-tier code (Configurate /
        // kotlin-reflect) -> LinkageError: loader constraint violation on
        // kotlin.jvm.internal.DefaultConstructorMarker. This is only safe because the PluginLoader
        // bootstrap classes (DynamicModuleLoader/ModuleConfig/DynamicModuleRegistry/
        // GeneratedModuleVersions) are pure Java and need no Kotlin at that stage.
        dependencies {
            exclude(dependency("org.jetbrains.kotlin:.*:.*"))
        }

        manifest {
            attributes["Main-Class"] = "yv.tils.core.YVtils"
        }
    }
}
