val jdaVersion = "5.5.1"

dependencies {
    implementation(project(":config"))
    implementation(project(":utils"))
    implementation(project(":common"))

    implementation("net.dv8tion:JDA:${jdaVersion}")
}
