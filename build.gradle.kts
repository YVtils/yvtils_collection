import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.1.21" apply false
    kotlin("plugin.serialization") version "2.1.21" apply false
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16" apply false
    id("xyz.jpenilla.run-paper") version "2.3.1" apply false
}

allprojects {
    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }

    group = "yv.tils"
    version = "1.2.0" // Set the version for the whole project
}

subprojects {
    apply {
        plugin("org.jetbrains.kotlin.jvm")
        plugin("org.jetbrains.kotlin.plugin.serialization")
        plugin("com.github.johnrengelman.shadow")
        plugin("io.papermc.paperweight.userdev")
        plugin("xyz.jpenilla.run-paper")
        plugin("java")
    }

    repositories {
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }

    val compileKotlin: KotlinCompile by tasks
    compileKotlin.compilerOptions {
        languageVersion.set(KotlinVersion.KOTLIN_2_2)
    }

    group = "yv.tils"
    version = "1.2.0"

    val commandAPIVersion = "10.0.1"

    dependencies {
        // Paper API dependency
        add("paperweightDevelopmentBundle", "io.papermc.paper:dev-bundle:1.21.1-R0.1-SNAPSHOT")

        // CommandAPI dependencies
        add("implementation", "dev.jorel:commandapi-bukkit-shade-mojang-mapped:$commandAPIVersion")
        add("implementation", "dev.jorel:commandapi-bukkit-kotlin:$commandAPIVersion")

        // Other
        add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        add("implementation", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.8.1")
    }

    tasks.withType<KotlinCompile> {
        compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
    }
}