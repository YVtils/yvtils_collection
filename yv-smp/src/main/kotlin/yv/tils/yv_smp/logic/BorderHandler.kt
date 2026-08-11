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

package yv.tils.yv_smp.logic

import org.bukkit.Location
import org.bukkit.World
import yv.tils.utils.logger.DEBUG_LEVEL
import yv.tils.utils.logger.Logger
import yv.tils.utils.modules.Core

class BorderHandler {
    /**
     * Changes the world border size for all worlds.
     *
     * @param size The new size of the world border.
     * @param time The time in seconds for the border to transition to the new size.
     */
    fun changeBorderSize(size: Double, time: Long = 0) {
        Core.instance.server.worlds.forEach { world ->
            world.worldBorder.changeSize(size, time)
        }
        Logger.debug("Changed world border size to $size with a transition time of $time seconds.", DEBUG_LEVEL.BASIC)
    }

    /**
     * Changes the world border size for a single world.
     *
     * @param world The world to apply the change to.
     * @param size The new size of the world border.
     * @param time The time in seconds for the border to transition to the new size.
     */
    fun changeBorderSize(world: World, size: Double, time: Long = 0) {
        world.worldBorder.changeSize(size, time)
        Logger.debug(
            "Changed world border size for '${world.name}' to $size with a transition time of $time seconds.",
            DEBUG_LEVEL.BASIC
        )
    }

    /**
     * Centers the world border of every world on the given location's X/Z coordinates.
     *
     * @param location The location to center the border on.
     */
    fun setBorderCenter(location: Location) {
        Core.instance.server.worlds.forEach { world ->
            world.worldBorder.setCenter(location.x, location.z)
        }
        Logger.debug(
            "Set world border center to ${location.blockX}, ${location.blockZ} for all worlds.",
            DEBUG_LEVEL.BASIC
        )
    }

    /**
     * Centers a single world's border on the given location's X/Z coordinates.
     *
     * @param world The world to apply the change to.
     * @param location The location to center the border on.
     */
    fun setBorderCenter(world: World, location: Location) {
        world.worldBorder.setCenter(location.x, location.z)
        Logger.debug(
            "Set world border center for '${world.name}' to ${location.blockX}, ${location.blockZ}.",
            DEBUG_LEVEL.BASIC
        )
    }

    /**
     * Resets the world border of every world to a default "closed" size, centered on
     * each world's spawn location. Intended to be used during `/yvsmp setup` before
     * an SMP session begins, so the border can later be expanded via [changeBorderSize]
     * as part of the start sequence.
     *
     * @param size The initial (small) border size.
     */
    fun setupDefaultBorder(size: Double) {
        Core.instance.server.worlds.forEach { world ->
            world.worldBorder.setCenter(world.spawnLocation.x, world.spawnLocation.z)
            world.worldBorder.size = size
        }
        Logger.debug(
            "Initialized world border to size $size, centered on spawn, for all worlds.",
            DEBUG_LEVEL.BASIC
        )
    }

    /**
     * Gets a short human-readable status line for a world's current border.
     */
    fun getBorderStatus(world: World): String {
        val border = world.worldBorder
        val center = border.center
        return "size=${"%.1f".format(border.size)} center=${center.blockX},${center.blockZ}"
    }
}