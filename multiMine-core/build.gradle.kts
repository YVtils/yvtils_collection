dependencies {
    implementation(project(":common"))
    implementation(project(":utils"))
    implementation(project(":config"))
    implementation(project(":multiMine"))
    implementation(project(":migration"))
}

val version = "2.0.0-beta.1"

tasks {
    runServer {
        minecraftVersion("1.21.5")
    }

    shadowJar {
        archiveBaseName.set("YVtils-MM")
        archiveVersion.set(version)
        archiveClassifier.set("")
        archiveFileName.set("YVtils-MM_v${version}.jar")

        manifest {
            attributes["Main-Class"] = "yv.tils.discord.YVtils"
        }
    }
}
