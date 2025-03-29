dependencies {
    implementation(project(":common"))
    implementation(project(":utils"))
    implementation(project(":essentials"))
    implementation(project(":config"))
    implementation(project(":sit"))
    implementation(project(":message"))
    implementation(project(":multiMine"))
    implementation(project(":status"))
}

tasks {
    runServer {
        minecraftVersion("1.21.4")
    }
}