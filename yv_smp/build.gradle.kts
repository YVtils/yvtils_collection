/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

val voiceChatVersion = "2.6.0"
val opus4jVersion = "2.1.3"
val lavaplayerVersion = "2.2.6"
val lavalinkYoutubeVersion = "1.17.0"

dependencies {
    implementation(project(":utils"))
    implementation(project(":config"))
    implementation(project(":common"))

    compileOnly("de.maxhenkel.voicechat:voicechat-api:${voiceChatVersion}")
    implementation("de.maxhenkel.opus4j:opus4j:${opus4jVersion}")

    implementation("dev.arbjerg:lavaplayer:${lavaplayerVersion}")
    implementation("dev.lavalink.youtube:v2:${lavalinkYoutubeVersion}")
}