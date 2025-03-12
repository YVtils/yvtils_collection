package yv.tils.common.updateChecker

import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URI

class PluginVersion {
    companion object {
        var cloudVersion = "x.x.x"
        var serverVersion = "x.x.x"
        var versionState = VersionState.UNKNOWN
    }

    private val scope = CoroutineScope(Dispatchers.Default)

    fun launchVersionCheck() {
        if (false) { // TODO
            return
        }

        scope.launch {
            asyncVersionCheck()
        }
    }

    private suspend fun asyncVersionCheck() {
        while (true) {

            delay(60 * 60 * 1000) // 1 hour
        }
    }

    private fun getNewestVersion(pluginName: String? = null): String {
        if (pluginName == null) {
            return "x.x.x"
        }

        val url = "https://api.yvtils.net/plugins/version?plugin=$pluginName"

        val connection = URI(url).toURL().openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        val response = connection.inputStream.bufferedReader().use { it.readText() }
        val responseCode = connection.responseCode
        connection.disconnect()

        if (responseCode != 200) {
            return "x.x.x"
        }

        val json = Json { ignoreUnknownKeys = true }
        val parsedResponse = json.decodeFromString<VersionData>(response)

        return "x.x.x"
    }

    private fun getPluginVersion(): String {
        return "x.x.x"
    }

    private fun compareVersions(cloudVersion: String, serverVersion: String): VersionState {
        val splitCloud = cloudVersion.split(".")
        val splitServer = serverVersion.split(".")

        for (bit in splitCloud + splitServer) {
            if (bit == "x") {
                return VersionState.UNKNOWN
            }
        }

        if (splitCloud[0] > splitServer[0]) {
            return VersionState.OUTDATED_MAJOR
        }

        if (splitCloud[1] > splitServer[1]) {
            return VersionState.OUTDATED_MINOR
        }

        if (splitCloud[2] > splitServer[2]) {
            return VersionState.OUTDATED_PATCH
        }

        return VersionState.UP_TO_DATE
    }
}

enum class VersionState {
    UP_TO_DATE,
    OUTDATED_PATCH,
    OUTDATED_MINOR,
    OUTDATED_MAJOR,
    UNKNOWN
}

data class VersionData(
    val version: String
)