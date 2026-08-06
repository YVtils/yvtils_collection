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
}

val moduleVersion = project.version.toString()
val embeddedResourcesDir = layout.buildDirectory.dir("generated/embeddedResources")

val embedRuntime = tasks.register<Copy>("embedRuntime") {
    description = "Embed plugin runtime"
    dependsOn(":common:shadowJar")
    from(project(":common").tasks.named("shadowJar"))
    into(embeddedResourcesDir.map { it.dir("embedded") })
    rename { "yvtils-runtime.jar" }
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