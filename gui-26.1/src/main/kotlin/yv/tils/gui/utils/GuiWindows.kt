/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.gui.utils

import org.bukkit.entity.Player
import xyz.xenondevs.invui.window.WindowManager

/** Window lifecycle helpers shared by feature modules and all supported InvUI versions. */
@Suppress("unused") // Public API consumed by feature modules through the selected GUI runtime.
object GuiWindows {
    fun close(player: Player) {
        WindowManager.getInstance().getOpenWindow(player)?.close()
    }
}
