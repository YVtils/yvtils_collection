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

import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinMonorepoVersion = "2.3.20"

    kotlin("jvm") version kotlinMonorepoVersion apply false
    kotlin("plugin.serialization") version kotlinMonorepoVersion apply false
    id("com.gradleup.shadow") version "9.4.1" apply false
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.21" apply false
    id("xyz.jpenilla.run-paper") version "3.1.0" apply false
    id("org.cyclonedx.bom") version "2.3.1"
}

/*
 * Aggregate CycloneDX SBOM (Software Bill of Materials) generation for the
 * whole monorepo - produces a single bom.json covering every subproject's
 * resolved dependencies, used by the `sbom.yml` workflow to upload to a
 * self-hosted OWASP Dependency-Track instance on every merge to `main`.
 *
 * Per-module SBOMs are intentionally NOT generated: Dependency-Track projects
 * are tracked at the monorepo level (one "project" per push), matching how
 * modules are versioned/released together rather than independently audited.
 */
tasks.cyclonedxBom {
    setIncludeConfigs(listOf("runtimeClasspath", "compileClasspath"))
    setProjectType("application")
    setSchemaVersion("1.5")
    setDestination(project.file("build/reports"))
    setOutputName("bom")
    setOutputFormat("json")
}

allprojects {
    group = "yv.tils"
    version = "1.2.0"

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.serialization")
        plugin("com.gradleup.shadow")
        plugin("io.papermc.paperweight.userdev")
        plugin("xyz.jpenilla.run-paper")
    }

    val commandAPIVersion = "11.2.0"

    dependencies {
        // Paper API dependency
        add("paperweightDevelopmentBundle", "io.papermc.paper:dev-bundle:1.21.1-R0.1-SNAPSHOT")

        // CommandAPI dependencies
        add("implementation", "dev.jorel:commandapi-paper-shade:$commandAPIVersion")
        add("implementation", "dev.jorel:commandapi-kotlin-paper:$commandAPIVersion")

        // Other
        add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        add("implementation", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.10.0")
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
        javaLauncher.set(project.extensions.getByType<JavaToolchainService>().launcherFor {
            languageVersion.set(JavaLanguageVersion.of(25))
        })
//        jvmArgs("-XX:+AllowEnhancedClassRedefinition")
    }
}
