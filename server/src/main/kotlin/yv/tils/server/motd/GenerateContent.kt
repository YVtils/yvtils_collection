package yv.tils.server.motd

import com.destroystokyo.paper.event.server.PaperServerListPingEvent
import message.MessageUtils
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import player.PlayerUtils
import server.VersionUtils
import yv.tils.server.configs.ConfigFile
import java.text.SimpleDateFormat
import java.util.*


class GenerateContent {
    companion object {
        val motd: Component
            get() {
                val motdLinesTop = ConfigFile.get("motd.entries.top") as List<*>
                val motdLinesBottom = ConfigFile.get("motd.entries.bottom") as List<*>
                val motd = Component.text()

                val randomTopLine = motdLinesTop.random()
                if (randomTopLine is String) {
                    val message = MessageUtils.replacer(randomTopLine, placeholders)
                    motd.append(message)
                }

                motd.append(Component.newline())

                val randomBottomLine = motdLinesBottom.random()
                if (randomBottomLine is String) {
                    val message = MessageUtils.replacer(randomBottomLine, placeholders)
                    motd.append(message)
                }

                return motd.build()
            }

        val hoverMOTD: Collection<PaperServerListPingEvent.ListedPlayerInfo>
            get() {
                val legacy: LegacyComponentSerializer = LegacyComponentSerializer.legacySection()
                val profiles = mutableListOf<PaperServerListPingEvent.ListedPlayerInfo>()

                val hoverLines = ConfigFile.get("hoverMOTD.entries") as List<*>

                for (line in hoverLines) {
                    if (line is String) {
                        val message = MessageUtils.replacer(line, placeholders)
                        val profile: PaperServerListPingEvent.ListedPlayerInfo = PaperServerListPingEvent.ListedPlayerInfo(
                            legacy.serialize(message),
                            UUID.randomUUID()
                        )

                        profiles.add(profile)
                    }
                }

                return profiles
            }

        private val placeholders: Map<String, Any>
            get() {
                val map = mutableMapOf<String, Any>()

                map["serverName"] = "" // TODO
                map["version"] = VersionUtils.serverVersion + if (VersionUtils.isViaVersion) "+" else ""
                map["onlinePlayers"] = PlayerUtils.onlinePlayersAsNames
                map["playerCount"] = PlayerUtils.onlinePlayersAsCount
                map["maxPlayers"] = ConfigFile.get("info.maxPlayers") as Int
                map["date"] = run {
                    val date = Date()
                    val formatter = SimpleDateFormat("dd/MM/yyyy")
                    formatter.format(date)
                }

                return map
            }
    }
}