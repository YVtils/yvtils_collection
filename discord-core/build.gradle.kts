dependencies {
    implementation(project(":common"))
    implementation(project(":utils"))
    implementation(project(":config"))
    implementation(project(":discord"))
    implementation(project(":migration"))
}

val version = "4.0.0-beta.2"

tasks {
    runServer {
        minecraftVersion("1.21.5")
    }

    shadowJar {
        archiveBaseName.set("YVtils-DC")
        archiveVersion.set(version)
        archiveClassifier.set("")
        archiveFileName.set("YVtils-DC_v${version}.jar")

        manifest {
            attributes["Main-Class"] = "yv.tils.discord.YVtils"
        }
    }
}
