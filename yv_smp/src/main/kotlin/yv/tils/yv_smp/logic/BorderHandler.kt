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

import yv.tils.utils.data.Data
import yv.tils.utils.logger.DEBUGLEVEL
import yv.tils.utils.logger.Logger

class BorderHandler {
    /**
     * Changes the world border size for all worlds.
     *
     * @param size The new size of the world border.
     * @param time The time in seconds for the border to transition to the new size.
     */
    fun changeBorderSize(size: Double, time: Long = 0) {
        Data.instance.server.worlds.forEach { world ->
            world.worldBorder.setSize(size, time)
        }
        Logger.debug("Changed world border size to $size with a transition time of $time seconds.", DEBUGLEVEL.BASIC)
    }
}