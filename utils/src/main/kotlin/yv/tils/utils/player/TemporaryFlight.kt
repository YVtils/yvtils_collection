/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms. https://yvtils.net/license
 */

package yv.tils.utils.player

import org.bukkit.entity.Player
import java.util.function.Consumer

/** Server-thread hook for releasing borrowed flight before another feature snapshots player abilities. */
object TemporaryFlight {
    var release: Consumer<Player> = Consumer { }
}
