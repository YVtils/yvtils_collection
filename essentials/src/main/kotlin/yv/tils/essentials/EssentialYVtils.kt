package yv.tils.essentials

import data.Data
import yv.tils.essentials.commands.register.FlyCMD
import yv.tils.essentials.language.RegisterStrings

class EssentialYVtils {
    companion object {
        const val MODULENAME = "essentials"
        const val MODULEVERSION = "1.0.0"
    }

    init {
        RegisterStrings().registerStrings()
    }

    fun enablePlugin() {
        Data.loadedModules.add("$MODULENAME v$MODULEVERSION")

        registerCommands()
    }

    fun disablePlugin() {

    }

    private fun registerCommands() {
        FlyCMD()
    }
}