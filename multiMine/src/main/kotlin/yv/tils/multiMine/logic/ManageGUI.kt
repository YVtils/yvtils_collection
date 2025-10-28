package yv.tils.multiMine.logic

import org.bukkit.entity.Player
import yv.tils.gui.logic.ConfigGUI
import yv.tils.multiMine.configs.ConfigFile
import yv.tils.utils.logger.Logger

class ManageGUI {
    fun openGUI(sender: Player) {
        ConfigGUI.createGUI(
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
                    Logger.debug("ManageGUI.saver: entries count = ${entries.size}", 2)
                    Logger.debug("ManageGUI.saver: saving map keys = ${map.keys}", 2)
                    val blocksValue = map["blocks"]
                    Logger.debug("ManageGUI.saver: blocks value type = ${blocksValue?.javaClass?.name}", 2)
                    Logger.debug("ManageGUI.saver: blocks value = $blocksValue", 2)
                    if (blocksValue is List<*>) {
                        Logger.debug("ManageGUI.saver: blocks list size = ${blocksValue.size}", 2)
                    }
                    ConfigFile().registerStrings(map)
                    Logger.debug("ManageGUI.saver: save complete", 2)
                    Logger.debug("ManageGUI.saver: File should be at: ${yv.tils.utils.data.Data.pluginFolder.absolutePath}/multiMine/config.yml", 2)
                } catch (ex: Exception) {
                    Logger.error("ManageGUI.saver: save failed: ${ex.message}")
                    ex.printStackTrace()
                }
            }
        )
    }
}