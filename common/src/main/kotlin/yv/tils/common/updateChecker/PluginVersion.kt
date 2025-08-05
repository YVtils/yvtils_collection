package yv.tils.common.updateChecker

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import yv.tils.common.config.ConfigFile
import yv.tils.common.language.LangStrings
import yv.tils.config.language.LanguageHandler
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.data.Data
import yv.tils.utils.logger.Logger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URI

class PluginVersion {
    companion object {
        var cloudVersion: String? = null
        var serverVersion: String? = null
        var versionState = VersionState.UNKNOWN

        var moderatorMessageKeyOnJoin: LangStrings? = null
        var firstBroadcast = true
    }

    fun launchVersionCheck() {
        if (ConfigFile.getValueAsBoolean("updateCheck.enabled") != true) {
            return
        }

        CoroutineHandler.launchTask(
            suspend {
                checkForUpdates()
            },
            "yvtils-update-check",
            afterDelay = 60 * 1000 * 60
        )
    }

    private fun checkForUpdates() {
        val latestVersion = getLatestVersion(Data.pluginShortName)
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

        val formatedPluginURL = "<click:open_url:${Data.pluginURL}>${Data.pluginURL}</click>"

        when (versionState) {
            VersionState.UP_TO_DATE -> {
                if (!firstBroadcast) {
                    return
                }

                Logger.info(
                    LanguageHandler.getMessage(
                        LangStrings.PLUGIN_VERSION_UP_TO_DATE.key
                    )
                )

                firstBroadcast = false
                moderatorMessageKeyOnJoin = null
            }
            VersionState.OUTDATED_PATCH -> {
                Logger.warn(
                    LanguageHandler.getMessage(
                        LangStrings.PLUGIN_VERSION_OUTDATED_PATCH.key,
                        mapOf(
                            "oldVersion" to currentVersion,
                            "newVersion" to latestVersion,
                            "link" to formatedPluginURL,
                        )
                    )
                )

                moderatorMessageKeyOnJoin = LangStrings.PLUGIN_VERSION_OUTDATED_PATCH
            }
            VersionState.OUTDATED_MINOR -> {
                Logger.warn(
                    LanguageHandler.getMessage(
                        LangStrings.PLUGIN_VERSION_OUTDATED_MINOR.key,
                        mapOf(
                            "oldVersion" to currentVersion,
                            "newVersion" to latestVersion,
                            "link" to formatedPluginURL,
                        )
                    )
                )

                moderatorMessageKeyOnJoin = LangStrings.PLUGIN_VERSION_OUTDATED_MINOR
            }
            VersionState.OUTDATED_MAJOR -> {
                Logger.warn(
                    LanguageHandler.getMessage(
                        LangStrings.PLUGIN_VERSION_OUTDATED_MAJOR.key,
                        mapOf(
                            "oldVersion" to currentVersion,
                            "newVersion" to latestVersion,
                            "link" to formatedPluginURL,
                        )
                    )
                )

                moderatorMessageKeyOnJoin = LangStrings.PLUGIN_VERSION_OUTDATED_MAJOR
            }
            VersionState.UNKNOWN -> {
                Logger.error("Failed to compare plugin versions. One of the versions is unknown.")

                moderatorMessageKeyOnJoin = null
            }
        }
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
                Logger.debug("Failed to fetch data from API (URL: $url). Response code: $responseCode")
                return null
            }
        } catch (e: Exception) {
            Logger.debug("Error while fetching plugin version: ${e.message}")
            return null
        }
    }

    private fun getPluginVersion(): String? {
        val version = Data.yvtilsVersion

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

enum class VersionState {
    UP_TO_DATE,
    OUTDATED_PATCH,
    OUTDATED_MINOR,
    OUTDATED_MAJOR,
    UNKNOWN
}

@Serializable
data class VersionData(
    val version: String
)

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
