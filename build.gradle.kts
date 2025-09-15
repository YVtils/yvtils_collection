import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "2.2.20" apply false
    kotlin("plugin.serialization") version "2.2.0" apply false
    id("com.gradleup.shadow") version "9.0.0-rc2" apply false
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18" apply false
    id("xyz.jpenilla.run-paper") version "2.3.1" apply false
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

    val commandAPIVersion = "10.1.2"

    dependencies {
        // Paper API dependency
        add("paperweightDevelopmentBundle", "io.papermc.paper:dev-bundle:1.21.1-R0.1-SNAPSHOT")

        // CommandAPI dependencies
        add("implementation", "dev.jorel:commandapi-bukkit-shade-mojang-mapped:$commandAPIVersion")
        add("implementation", "dev.jorel:commandapi-bukkit-kotlin:$commandAPIVersion")

        // Other
        add("implementation", "org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
        add("implementation", "org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    }

    tasks.withType<KotlinCompile> {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
}
