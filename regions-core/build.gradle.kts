dependencies {
    implementation(project(":common"))
    implementation(project(":utils"))
    implementation(project(":config"))
    implementation(project(":regions"))
}

val version = "1.0.0-beta.2"

tasks {
    runServer {
        minecraftVersion("1.21.5")
    }

    shadowJar {
        archiveBaseName.set("YVtils-RG")
        archiveVersion.set(version)
        archiveClassifier.set("")
        archiveFileName.set("YVtils-RG_v${version}.jar")

        manifest {
            attributes["Main-Class"] = "yv.tils.rg.YVtils"
        }
    }
}