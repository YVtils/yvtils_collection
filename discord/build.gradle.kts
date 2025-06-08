val jdaVersion = "5.6.1"

dependencies {
    implementation(project(":config"))
    implementation(project(":utils"))
    implementation(project(":common"))

    implementation("net.dv8tion:JDA:${jdaVersion}")
}
