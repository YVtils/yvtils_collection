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

package yv.tils.core

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.bukkit.entity.Player
import yv.tils.config.GeneralConfig
import yv.tils.config.GeneralConfigManager
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.DEBUG_LEVEL
import yv.tils.utils.logger.Logger
import yv.tils.utils.modules.Core
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI

class CheckVersion {
    companion object {
        var versionState: VersionState = VersionState.UNKNOWN
        var updateNotified: Boolean = false

        var cloudVersion: String = ""
        var serverVersion: String = ""
    }

    /**
     * Check if the core supports the used server version
     * @return `true` if no supportedVersions are defined or server version is supported or `false` otherwise
     */
    fun serverVersion(): Boolean {
        val supportedVersions = Core.core.supportedVersions
        if (supportedVersions.isEmpty()) {
            Logger.debug("No supported versions specified for ${Core.core.name}. Skipping version check.", DEBUG_LEVEL.BASIC)
            return true
        }

        val serverVersion = Core.instance.server.minecraftVersion

        Logger.debug("Checking server version: $serverVersion against supported versions: ${supportedVersions.joinToString(", ")}",
            DEBUG_LEVEL.BASIC)

        return supportedVersions.contains(serverVersion)
    }

    fun launchVersionCheck() {
        if (!GeneralConfigManager.config.updateCheck.enabled) {
            return
        }

        CoroutineHandler.launchTask(
            suspend {
                pluginVersion()
            },
            "yvtils-update-check",
            afterDelay = 60 * 1000 * 60
        )
    }

    private fun pluginVersion() {
        val latestVersion = getLatestVersion(Core.core.pluginShort)
        if (latestVersion == null) {
            Logger.warn("Failed to fetch latest plugin version from API.")
            return
        }

        val currentVersion = getPluginVersion()
        if (currentVersion == null) {
            Logger.warn("Failed to fetch current plugin version.")
            return
        }

        versionState = compareVersions(latestVersion, currentVersion)

        notifyUpdate()
    }

    private fun notifyUpdate() {
        if (updateNotified) return

        val coreURL = Core.core.url
        val formatedPluginURL = "<click:open_url:${coreURL}>${coreURL}</click>"

        updateNotified = true
    }

    fun notifyOnJoin(player: Player) {
        if (!GeneralConfigManager.config.updateCheck.notifyOnJoin) return
        if (!player.isOp) return

        val coreURL = Core.core.url
        val formatedPluginURL = "<click:open_url:${coreURL}>${coreURL}</click>"


    }

    private fun getLatestVersion(pluginName: String? = null): String? {
        if (pluginName == null) {
            return null
        }

        val url = "https://api.yvtils.net/versions/$pluginName"

        try {
            val connection = URI(url).toURL().openConnection() as HttpURLConnection

            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val reader = BufferedReader(InputStreamReader(connection.inputStream))
                val response = StringBuilder()
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    response.append(line)
                }
                reader.close()
                val json = Json { ignoreUnknownKeys = true }
                val versionData = json.decodeFromString<VersionData>(response.toString())
                cloudVersion = versionData.version
                return versionData.version
            } else {
                Logger.debug("Failed to fetch data from API (URL: $url). Response code: $responseCode", DEBUG_LEVEL.BASIC)
                return null
            }
        } catch (e: Exception) {
            Logger.debug("Error while fetching plugin version: ${e.message}", DEBUG_LEVEL.BASIC)
            return null
        }
    }

    private fun getPluginVersion(): String? {
        val version = Core.core.version

        if (version == "") {
            return null
        }

        serverVersion = version
        return version
    }

    private fun compareVersions(cloudVersion: String, serverVersion: String): VersionState {
        val cloud = SemVer.parse(cloudVersion) ?: return VersionState.UNKNOWN
        val server = SemVer.parse(serverVersion) ?: return VersionState.UNKNOWN

        if (cloud == server) return VersionState.UP_TO_DATE
        if (cloud < server) return VersionState.UP_TO_DATE

        return when {
            cloud.major > server.major -> VersionState.OUTDATED_MAJOR
            cloud.minor > server.minor -> VersionState.OUTDATED_MINOR
            cloud.patch > server.patch || cloud.preRelease == null && server.preRelease != null -> VersionState.OUTDATED_PATCH
            else -> VersionState.OUTDATED_PATCH
        }
    }
}

@Serializable
data class VersionData(
    val version: String
)

enum class VersionState {
    UP_TO_DATE,
    OUTDATED_PATCH,
    OUTDATED_MINOR,
    OUTDATED_MAJOR,
    UNKNOWN
}

data class SemVer(
    val major: Int,
    val minor: Int,
    val patch: Int,
    val preRelease: String? = null
) : Comparable<SemVer> {
    companion object {
        fun parse(version: String): SemVer? {
            val mainAndPre = version.split("-", limit = 2)
            val parts = mainAndPre[0].split(".")

            if (parts.size != 3) return null

            val major = parts[0].toIntOrNull() ?: return null
            val minor = parts[1].toIntOrNull() ?: return null
            val patch = parts[2].toIntOrNull() ?: return null

            return SemVer(major, minor, patch, mainAndPre.getOrNull(1))
        }
    }

    override fun compareTo(other: SemVer): Int {
        if (this.major != other.major) return this.major - other.major
        if (this.minor != other.minor) return this.minor - other.minor
        if (this.patch != other.patch) return this.patch - other.patch

        return comparePreRelease(this.preRelease, other.preRelease)
    }

    private fun comparePreRelease(a: String?, b: String?): Int {
        if (a == null && b == null) return 0
        if (a == null) return 1
        if (b == null) return -1

        return a.compareTo(b)
    }
}