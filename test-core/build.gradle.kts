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

dependencies {
    // Provided at compile time only - these are NOT shaded into this jar.
    // Instead, `common`'s own shadowJar (which already bundles config-v2+
    // utils+common+CommandAPI+coroutines+serialization together) is embedded
    // as a resource and added to the classpath at runtime via a local
    // JarLibrary in DynamicModuleLoader. This keeps everything that needs to
    // interoperate with dynamically-fetched feature modules in the same
    // classloader tier - see DynamicModuleLoader's docs for the full
    // explanation.
    compileOnly(project(":common"))
    compileOnly(project(":config-v2"))
    compileOnly(project(":utils"))
    // gui-v2 (and its InvUI dependency) is merged into the same embedded runtime bundle
    // below for the same reason - see core/build.gradle.kts's comment on this same line.
    compileOnly(project(":gui-v2"))
}

val moduleVersion = project.version.toString()

// Embeds `common`'s + `gui-v2`'s shadowJar outputs (utils+config-v2+common+CommandAPI+
// coroutines+serialization+InvUI, all merged together) as a plugin resource, so
// DynamicModuleLoader can extract it and add it via JarLibrary at runtime.
val embeddedResourcesDir = layout.buildDirectory.dir("generated/embeddedResources")

val embedRuntime by tasks.registering(Jar::class) {
    dependsOn(":common:shadowJar", ":gui-v2:shadowJar")
    from(zipTree(project(":common").tasks.named("shadowJar", Jar::class).flatMap { it.archiveFile }))
    from(zipTree(project(":gui-v2").tasks.named("shadowJar", Jar::class).flatMap { it.archiveFile }))
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    destinationDirectory.set(embeddedResourcesDir.map { it.dir("embedded") })
    archiveFileName.set("yvtils-runtime.jar")
}

sourceSets {
    main {
        resources.srcDir(embeddedResourcesDir)
    }
}

tasks.named("processResources") {
    dependsOn(embedRuntime)
}

tasks {
    runServer {
        minecraftVersion("26.1.2")
    }

    shadowJar {
        archiveBaseName.set("TEST-YVTILS-CORE")
        archiveVersion.set(moduleVersion)
        archiveClassifier.set("")
        archiveFileName.set("TEST-YVTILS-CORE_v${moduleVersion}.jar")

        manifest {
            attributes["Main-Class"] = "yv.tils.core.YVtils"
        }
    }
}
