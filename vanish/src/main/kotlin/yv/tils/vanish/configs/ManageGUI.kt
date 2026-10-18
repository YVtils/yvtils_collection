package yv.tils.vanish.configs

import org.bukkit.entity.Player
import yv.tils.gui.logic.DataClassConfigGui
import yv.tils.utils.message.MessageUtils
import yv.tils.vanish.language.VanishText
import yv.tils.vanish.permissions.VanishPermissions

object ManageGUI {
    fun open(player: Player) {
        if (!player.hasPermission(VanishPermissions.CONFIG)) {
            VanishText.send(player, "denied")
            return
        }
        DataClassConfigGui.open(
            player,
            MessageUtils.strip(VanishText.text(player, "config")),
            ConfigFile.state.copy()
        ) {
            check(player.hasPermission(VanishPermissions.CONFIG))
            ConfigFile.apply(it)
        }
    }
}
