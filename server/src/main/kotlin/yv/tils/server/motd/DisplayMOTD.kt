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

package yv.tils.server.motd

import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import yv.tils.server.configs.ConfigFile
import yv.tils.utils.player.PlayerUtils

class DisplayMOTD {
    fun onServerPing(e: PaperServerListPingEvent) {
        if (ConfigFile.get("motd.enabled") == true) {
            e.maxPlayers = ConfigFile.get("info.maxPlayers") as Int
            e.numPlayers = PlayerUtils.onlinePlayersAsCount

            if (ConfigFile.get("hoverMOTD.enabled") == true) {
                e.setHidePlayers(false)
                e.listedPlayers.clear()
                e.listedPlayers.addAll(GenerateContent.hoverMOTD)
            }

            e.motd(GenerateContent.motd)
        }
    }
}
