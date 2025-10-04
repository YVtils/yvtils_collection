val jdaVersion = "6.0.0"

dependencies {
    implementation(project(":config"))
    implementation(project(":utils"))
    implementation(project(":common"))

    implementation("net.dv8tion:JDA:${jdaVersion}")
}
