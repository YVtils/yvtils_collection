package yv.tils.regions.listeners

import language.LanguageHandler
import logger.Logger
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import yv.tils.regions.data.Flag
import yv.tils.regions.language.LangStrings
import yv.tils.regions.listeners.custom.flags.BlockFlagTriggerEvent
import yv.tils.regions.logic.FlagLogic
import yv.tils.regions.logic.PlayerChecks

class BlockFlagTrigger: Listener {
    @EventHandler(priority = EventPriority.LOW, ignoreCancelled = true)
    fun onEvent(e: BlockFlagTriggerEvent) {
        Logger.debug("Flag trigger event: ${e.flag} for player ${e.player.name} in region ${e.region.name}")
        val player = e.player
        val region = e.region
        val flagType = e.flag

        val loc = e.block.location

        val playerRole = PlayerChecks.regionRole(player, region)

        if (FlagLogic.flagCheck(region, flagType, playerRole)) {
            Logger.debug("Flag trigger event: ${e.flag} for player ${e.player.name} in region ${e.region.name} is allowed")
            return
        } else {
            Logger.debug("Flag trigger event: ${e.flag} for player ${e.player.name} in region ${e.region.name} is denied")
            e.isCancelled = true
            displayFeedback(player, loc, flagType)
            return
        }
    }

    private fun displayFeedback(player: Player, loc: Location, flag: Flag) {
        val visualFeedbackFlags = mutableListOf(
            Flag.DESTROY,
            Flag.PLACE,
            Flag.INTERACT,
        )

        if (flag in visualFeedbackFlags) {
            val particle = Particle.DUST
            val dustOptions = Particle.DustOptions(Color.fromRGB(145, 150, 145), 1.0f)

            val particleLoc = loc.clone().add(0.5, 1.25, 0.5)

            loc.world?.spawnParticle(
                particle,
                particleLoc,
                15,
                0.2, 0.1, 0.2,
                0.01,
                dustOptions
            )
        }

        player.sendMessage(
            LanguageHandler.getMessage(
                LangStrings.FLAG_TRIGGER_DENIED.key,
                player.uniqueId,
                mapOf(
                    "flag" to flag.name,
                )
            )
        )
    }
}