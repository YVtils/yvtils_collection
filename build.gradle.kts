/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
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
    kotlin("jvm") version "2.2.21" apply false
    kotlin("plugin.serialization") version "2.2.21" apply false
    id("com.gradleup.shadow") version "9.3.0" apply false
    id("io.papermc.paperweight.userdev") version "2.0.0-SNAPSHOT" apply false
    id("xyz.jpenilla.run-paper") version "3.0.2" apply false
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

    val commandAPIVersion = "11.0.0"

    dependencies {
        // Paper API dependency
        add("paperweightDevelopmentBundle", "io.papermc.paper:dev-bundle:1.21.1-R0.1-SNAPSHOT")

        // CommandAPI dependencies
        add("implementation", "dev.jorel:commandapi-paper-shade:$commandAPIVersion")
        add("implementation", "dev.jorel:commandapi-kotlin-paper:$commandAPIVersion")

        // Other
        add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        add("implementation", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }

    tasks.withType(xyz.jpenilla.runtask.task.AbstractRun::class) {
        javaLauncher.set(project.extensions.getByType<JavaToolchainService>().launcherFor {
            languageVersion.set(JavaLanguageVersion.of(21))
        })
        jvmArgs("-XX:+AllowEnhancedClassRedefinition")
    }
}
