/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.yv_smp.logic.music

import org.bukkit.Registry
import org.bukkit.Sound
import java.net.URI
import java.net.URL

/**
 * Represents different types of audio sources that can be played.
 * - File: Audio from a file on the server
 * - Url: Audio from a remote URL
 * - BuiltIn: Minecraft's built-in sounds
 */
sealed class AudioSource {
    /**
     * Audio source from a local file on the server.
     * The file must exist and be in a supported format (e.g., .ogg, .wav).
     */
    data class File(val file: java.io.File) : AudioSource() {
        init {
            require(file.exists()) { "Audio file does not exist: ${file.absolutePath}" }
            require(file.isFile) { "Path is not a file: ${file.absolutePath}" }
        }
    }

    /**
     * Audio source from a remote URL.
     * The URL must be accessible and point to a supported audio format.
     */
    data class Url(val url: URL) : AudioSource() {
        constructor(urlString: String) : this(URI(urlString).toURL())
    }

    /**
     * Built-in Minecraft sound that works without any mods.
     * This is the fallback option when Simple Voice Chat is not available.
     */
    data class BuiltIn(val sound: Sound?) : AudioSource()

    /**
     * Checks if this audio source is a built-in Minecraft sound.
     */
    fun isBuiltIn(): Boolean = this is BuiltIn

    /**
     * Converts this audio source to a Minecraft Sound if it's a BuiltIn source.
     * Returns null for File or Url sources.
     */
    fun toMinecraftSound(): Sound? = when (this) {
        is BuiltIn -> sound
        else -> null
    }

    override fun toString(): String = when (this) {
        is File -> "File(${file.name})"
        is Url -> "Url(${url.host}${url.path})"
        is BuiltIn -> {
            if (sound == null) {
                "BuiltIn(None)"
            }
            val key = Registry.SOUNDS.getKey(sound!!)
            "BuiltIn(${key?.asString()})"
        }
    }
}

/**
 * Configuration class for audio playback settings.
 */
data class AudioConfig(
    val source: AudioSource,
    val volume: Float = 1.0f,
    val pitch: Float = 1.0f,
    val loop: Boolean = false,
) {
    init {
        require(volume in 0.0f..1.0f) { "Volume must be between 0.0 and 1.0" }
        require(pitch in 0.5f..2.0f) { "Pitch must be between 0.5 and 2.0" }
    }
}
