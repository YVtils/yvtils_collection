/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.status.logic

import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.status.data.Permissions

/** Recheck action permissions at execution, including changes after command parsing. */
internal object StatusAuthorization {
    fun canChange(sender: CommandSender, target: Player, action: Permissions): Boolean {
        if (!sender.hasPermission(Permissions.COMMAND_STATUS.permission.name) ||
            !sender.hasPermission(action.permission.name)) return false
        if (sender is Player && sender.uniqueId == target.uniqueId) return true
        val others = if (action == Permissions.COMMAND_STATUS_CLEAR)
            Permissions.COMMAND_STATUS_CLEAR_OTHERS else Permissions.COMMAND_STATUS_MANAGE_OTHERS
        return sender.hasPermission(others.permission.name)
    }
}
