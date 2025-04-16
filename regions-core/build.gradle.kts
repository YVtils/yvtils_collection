dependencies {
    implementation(project(":common"))
    implementation(project(":utils"))
    implementation(project(":config"))
    implementation(project(":regions"))
}

tasks {
    runServer {
        minecraftVersion("1.21.5")
    }
}