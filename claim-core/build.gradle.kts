dependencies {
    implementation(project(":common"))
    implementation(project(":utils"))
    implementation(project(":config"))
    implementation(project(":claim"))
}

tasks {
    runServer {
        minecraftVersion("1.21.5")
    }
}