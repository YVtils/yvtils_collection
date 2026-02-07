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

package yv.tils.moderation.utils

import com.destroystokyo.paper.profile.PlayerProfile
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import yv.tils.config.language.LanguageHandler
import yv.tils.moderation.configs.saveFile.MuteSave
import yv.tils.moderation.configs.saveFile.MuteSaveFile
import yv.tils.moderation.data.Exceptions.Companion.TargetToOfflinePlayerParseException
import yv.tils.utils.logger.DEBUGLEVEL
import yv.tils.utils.logger.Logger
import yv.tils.utils.message.MessageUtils
import yv.tils.utils.time.TimeUtils

class TargetUtils {
    companion object {
        /**
         * @param target The target to try to parse to an offline player
         * @throws TargetToOfflinePlayerParseException
         * @return [OfflinePlayer]
         */
        fun parseTargetToOfflinePlayer(target: PlayerProfile): OfflinePlayer? {
            try {
                if (target.id == null) {
                    return null
                }

                val offlinePlayer = Bukkit.getOfflinePlayer(target.id!!)

                return offlinePlayer
            } catch (e: Exception) {
                Logger.debug("An error occurred while trying to parse the target to offline player. ${e.message}",DEBUGLEVEL.DETAILED)
                throw TargetToOfflinePlayerParseException
            }
        }

        fun getMuteData(target: OfflinePlayer): MuteSave? {
            return MuteSaveFile().getMuteInfo(target.uniqueId)
        }

        fun loopThroughTargets(targets: List<PlayerProfile>, logic: (PlayerProfile, Map<String, Any>) -> Unit, params: Map<String, Any>) {
            for (target in targets) {
                logic(target, params)
            }
        }

        fun isTargetOnline(target: OfflinePlayer): Boolean {
            return target.isOnline
        }

        fun isTargetBanned(target: OfflinePlayer): Boolean {
            return target.isBanned
        }

        fun isTargetMuted(target: OfflinePlayer): Boolean {
            val muteData = getMuteData(target) ?: return false

            if (muteData.expires == "null") {
                return true
            }

            val durationLong = muteData.expires.toLong()
            val currentTime = System.currentTimeMillis()
            if (currentTime > durationLong) {
                MuteSaveFile().unmutePlayer(target.uniqueId)
                return false
            }

            return true
        }
    }

    fun sendMutedMessage(player: Player, muteData: MuteSave, message: Component) {
        val reason = muteData.reason
        val expires = if (muteData.expires == "null") {
            "Never"
        } else {
            val expiresLong = muteData.expires.toLong()
            TimeUtils().formatDuration(expiresLong-System.currentTimeMillis())
        }
        val sMessage = MessageUtils.stripChatMessage(message)

        player.sendMessage(LanguageHandler.getMessage(
            "moderation.target.muted.chat.player",
            player.uniqueId,
            mapOf(
                "reason" to reason,
                "duration" to expires
            )
        ))

        Logger.info(
            LanguageHandler.getMessage(
                "moderation.target.muted.chat.console",
                player.uniqueId,
                mapOf(
                    "reason" to reason,
                    "duration" to expires,
                    "player" to player.name,
                    "message" to sMessage
                )
            ))
    }

    fun cleanupMutedPlayers() {
        val iterator = MuteSaveFile.saves.entries.iterator()
        val currentTime = System.currentTimeMillis()

        while (iterator.hasNext()) {
            val entry = iterator.next()
            val muteSave = entry.value

            if (muteSave.expires != "null") {
                val expiresLong = muteSave.expires.toLong()
                if (currentTime > expiresLong) {
                    iterator.remove()
                    MuteSaveFile().unmutePlayer(entry.key)
                    Logger.debug("Unmuted player with UUID: ${entry.key} as the mute duration has expired.", DEBUGLEVEL.BASIC)
                }
            }
        }
    }
}