package yv.tils.multiMine.logic

import org.bukkit.entity.Player
import yv.tils.gui.logic.ConfigGUI

class ManageGUI {
    fun openGUI(sender: Player) {
        // TODO: Implement config value getter
        val configMap: MutableMap<String, Any> = mutableMapOf(
            "exampleInt" to 10,
            "exampleDouble" to 5.5,
            "exampleBoolean" to true,
            "exampleString" to "Hello, MultiMine!"
        )

        ConfigGUI().createGUI(
            sender,
            "MultiMine Config",
            configMap
        )
    }
}