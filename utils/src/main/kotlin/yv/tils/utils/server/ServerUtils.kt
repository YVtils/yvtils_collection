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

import yv.tils.utils.data.Data
import yv.tils.utils.message.MessageUtils
import net.kyori.adventure.text.Component

class ServerUtils {
    companion object {
        var serverInMaintenance: Boolean = false
        var serverIP: String = ""
        var serverPort: Int = -1

        val difficulty: String
            get() = Data.instance.server.worlds[0].difficulty.name

        val isWhitelistActive: Boolean
            get() {
                return Data.instance.server.hasWhitelist()
            }

        fun setServerMaintenance(status: Boolean) {
            serverInMaintenance = status
        }

        val serverName: String
            get() {
                return "A Minecraft Server" // TODO: Logic for custom server names
            }

        val motd: Component
            get() {
                return Data.instance.server.motd()
            }

        val motdAsString: String
            get() {
                return MessageUtils.convert(motd)
            }
    }
}
