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
    compileOnly(project(":common"))
    compileOnly(project(":config-v2"))
    compileOnly(project(":utils"))
    // Provided at compile time only - NOT shaded into this jar. `gui-v2` (and its InvUI
    // dependency) is merged into the same embedded runtime bundle as common/config-v2/utils
    // below, for the exact same reason CommandAPI/coroutines/serialization are: InvUI keeps
    // global, classloader-sensitive static state (its `PacketListener` singleton registers a
    // fixed-named Netty channel handler). If every dynamically-loaded feature module that
    // depends on gui-v2 (multiMine, discord, status, ...) independently re-resolved it via
    // its own MavenLibraryResolver, each would load its own separate copy of that singleton,
    // and every one of them would try to register the same-named handler on the same
    // connection - causing `IllegalArgumentException: Duplicate handler name` (or, for plain
    // Kotlin types crossing the same boundary, a `LinkageError: loader constraint violation`
    // - see Module.YVtilsModuleData.configGuiOpener's KDoc). Resolving it exactly once, here,
    // is what avoids that.
    compileOnly(project(":gui-v2"))
}

val moduleVersion = project.version.toString()
val embeddedResourcesDir = layout.buildDirectory.dir("generated/embeddedResources")

val embedRuntime = tasks.register<Jar>("embedRuntime") {
    description = "Embed plugin runtime (common + gui-v2, merged into one shaded jar)"
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
        archiveBaseName.set("YVtils")
        archiveVersion.set(moduleVersion)
        archiveClassifier.set("")
        archiveFileName.set("YVtils_v${moduleVersion}.jar")

        manifest {
            attributes["Main-Class"] = "yv.tils.core.YVtils"
        }
    }
}