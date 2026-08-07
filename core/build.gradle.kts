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
    // Compile-time only, to reference GUI types (`Gui`, `Item`, `Window`, ...) and
    // `GUIYVtils`/`DynamicModuleRegistry.GUI_ARTIFACTS`'s entry-point class name.
    // NOT shaded/embedded into this jar - unlike `common`, the actual `gui-<version>`
    // build used at runtime is resolved dynamically by `DynamicModuleLoader`, matching
    // whichever Minecraft version the server is actually running (InvUI, which `gui`
    // wraps, dropped multi-version support in v2 - see `DynamicModuleRegistry.GUI_ARTIFACTS`
    // for the full explanation). `gui-26.1` is used here purely as the reference variant
    // for compile-time symbol resolution; its InvUI API is expected to stay
    // source-compatible with every other `gui-<version>` module.
    compileOnly(project(":gui-26.1"))
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