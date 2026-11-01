/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */

package yv.tils.yv_smp.logic.start

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Statistic
import org.bukkit.entity.Player
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Scoreboard
import yv.tils.config.files.ObjectMapperFileUtils

/** Persistent SMP death displays live on the real scoreboards, preserving status teams. */
object DeathCounter {
    data class State(var enabled: Boolean = false)

    private const val filePath = "/yv_smp/death-counter.json"
    private var state = State()

    fun load() {
        state = ObjectMapperFileUtils.load(filePath, State())
        refresh()
    }

    fun enable() {
        if (!state.enabled) {
            val enabled = State(enabled = true)
            ObjectMapperFileUtils.save(filePath, enabled)
            state = enabled
        }
        refresh()
    }

    fun refresh() {
        if (!state.enabled) return
        val players = Bukkit.getOnlinePlayers().toList()
        players.map { it.scoreboard }.distinct().forEach { configure(it, players) }
    }

    internal fun configure(scoreboard: Scoreboard, players: List<Player>) {
        val title = Component.text("☠", NamedTextColor.RED)
        val tab = scoreboard.getObjective("yvsmp_deaths")
            ?: scoreboard.registerNewObjective("yvsmp_deaths", Criteria.DEATH_COUNT, title)
        val belowName = scoreboard.getObjective("yvsmp_deaths_name")
            ?: scoreboard.registerNewObjective("yvsmp_deaths_name", Criteria.DEATH_COUNT, title)
        tab.displaySlot = DisplaySlot.PLAYER_LIST
        belowName.displaySlot = DisplaySlot.BELOW_NAME
        players.forEach { player ->
            val deaths = player.getStatistic(Statistic.DEATHS)
            tab.getScore(player.name).score = deaths
            belowName.getScore(player.name).score = deaths
        }
    }
}
