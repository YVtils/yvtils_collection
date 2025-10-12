package yv.tils.multiMine.logic

import org.bukkit.entity.Player
import yv.tils.gui.logic.ConfigGUI
import yv.tils.multiMine.configs.ConfigFile

class ManageGUI {
    fun openGUI(sender: Player) {
        ConfigGUI().createGUI(
            sender,
            "MultiMine Config",
            ConfigFile.configNew
        )
    }
}