package yv.tils.server.motd

import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import player.PlayerUtils
import yv.tils.server.configs.ConfigFile

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