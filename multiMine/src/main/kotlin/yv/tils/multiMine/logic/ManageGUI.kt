package yv.tils.multiMine.logic

import org.bukkit.entity.Player
import yv.tils.gui.logic.ConfigGUI
import yv.tils.multiMine.configs.ConfigFile

class ManageGUI {
    fun openGUI(sender: Player) {
        ConfigGUI().createGUI(
            sender,
            "MultiMine Config",
            ConfigFile.configNew,
            saver = { entries ->
                // convert entries to map and persist via ConfigFile.registerStrings
                val map = mutableMapOf<String, Any>()
                for (e in entries) {
                    val v = e.value ?: e.defaultValue
                    if (v != null) map[e.key] = v
                }
                try {
                    yv.tils.utils.logger.Logger.debug("ManageGUI.saver: saving map ${map}", 2)
                    ConfigFile().registerStrings(map)
                    yv.tils.utils.logger.Logger.debug("ManageGUI.saver: save complete", 2)
                } catch (ex: Exception) {
                    yv.tils.utils.logger.Logger.error("ManageGUI.saver: save failed: ${ex.message}")
                }
            }
        )
    }
}