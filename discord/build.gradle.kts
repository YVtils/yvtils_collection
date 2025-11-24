val jdaVersion = "6.1.2"

dependencies {
    implementation(project(":config"))
    implementation(project(":utils"))
    implementation(project(":common"))

    implementation("net.dv8tion:JDA:${jdaVersion}")
}
