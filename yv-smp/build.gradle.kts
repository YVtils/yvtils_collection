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

val opus4jVersion = "2.1.3"
val lavaplayerVersion = "2.2.7"
val lavalinkYoutubeVersion = "1.17.0"

dependencies {
    compileOnly(project(":utils"))
    compileOnly(project(":config-v2"))
    compileOnly(project(":common"))
    compileOnly(project(":gui-26.1"))

    // Deliberately NOT a dependency (not even compileOnly): this module is
    // fetched dynamically at runtime and lives in a classloader tier that
    // cannot see other plugins' classes (Simple Voice Chat's, in this case) -
    // see `logic/music/svc/VoicechatBridge.kt` for the full explanation and
    // the reflection-based bridge used instead. Do NOT add
    // `de.maxhenkel.voicechat:voicechat-api` back here; any code that
    // statically references its types will throw `NoClassDefFoundError` at
    // runtime no matter what.
    implementation("de.maxhenkel.opus4j:opus4j:${opus4jVersion}")
    implementation("dev.arbjerg:lavaplayer:${lavaplayerVersion}")
    implementation("dev.lavalink.youtube:v2:${lavalinkYoutubeVersion}")
}
