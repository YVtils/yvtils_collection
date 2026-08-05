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

package yv.tils.utils.server

import yv.tils.utils.modules.Core
import yv.tils.utils.logger.Logger


class VersionUtils {
    companion object {
        var serverVersion = "x.x.x"
        var isViaVersion = false
    }

    fun loadServerVersion() {
        serverVersion = Core.instance.server.minecraftVersion
        isViaVersion = Core.instance.server.pluginManager.getPlugin("ViaVersion") != null

        Logger.debug("Server is running on version: $serverVersion${if (isViaVersion) " with ViaVersion" else ""}")
    }

    fun isServerVersionAtLeast(version: String): Boolean {
        val serverParts = serverVersion.split(".").map { it.toIntOrNull() ?: 0 }
        val versionParts = version.split(".").map { it.toIntOrNull() ?: 0 }

        val maxLength = maxOf(serverParts.size, versionParts.size)
        val paddedServerParts = serverParts + List(maxLength - serverParts.size) { 0 }
        val paddedVersionParts = versionParts + List(maxLength - versionParts.size) { 0 }

        for (i in 0 until maxLength) {
            if (paddedServerParts[i] > paddedVersionParts[i]) return true
            if (paddedServerParts[i] < paddedVersionParts[i]) return false
        }
        return true
    }
}
