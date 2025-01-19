package yv.tils.core

import org.bukkit.plugin.java.JavaPlugin
import yv.tils.common.CommonYVtils

class YVtils : JavaPlugin() {
    companion object {
        val yvtilsVersion = YVtils().pluginMeta.version
    }

    override fun onEnable() {
        val loadedModules = mutableListOf<String>()
        loadedModules.add("EXAMPLE 1")
        loadedModules.add("EXAMPLE 2")
        loadedModules.add("EXAMPLE 3")

        val data = mutableMapOf(
            1 to listOf("plugin.action.start"),
            2 to loadedModules,
        )
        val log = printLogWithSplits(data)
        log.forEach { logger.info(it) }

        CommonYVtils().test()
    }

    override fun onDisable() {
        val data = mutableMapOf(
            1 to listOf("plugin.action.stop"),
        )

        val log = printLogWithSplits(data)
        log.forEach { logger.info(it) }
    }

    private fun printLogWithSplits(data: MutableMap<Int, List<String>>): MutableList<String> {
        val lineLength = 40
        val log = mutableListOf<String>()
        val border = "+".repeat(lineLength)

        log.add(border)

        val message = "YVtils Collection v$yvtilsVersion"
        val secondMessage = "https://yvtils.net"
        val prefix = "|"
        val suffix = "|"

        fun getPadding(message: String): String {
            val padding = (lineLength - message.length) / 2
            return " ".repeat(padding + (if ((lineLength - message.length) % 2 != 0) 1 else 0))
        }

        log.add("$prefix${getPadding(message)}$message${" ".repeat(lineLength - message.length - getPadding(message).length)}$suffix")
        log.add("$prefix${getPadding(secondMessage)}$secondMessage${" ".repeat(lineLength - secondMessage.length - getPadding(secondMessage).length)}$suffix")

        log.add(border)

        var lastKey: Int? = null
        data.toSortedMap().forEach { (key, messages) ->
            if (lastKey != null && key != lastKey) {
                log.add("|----------------------------------------|")
            }

            messages.forEach { message ->
                if (message.length > lineLength) {
                    log.add("$prefix${message.substring(0, lineLength - 2)}$suffix")
                    log.add("$prefix${message.substring(lineLength - 2)}${" ".repeat(lineLength - message.length - 2)}$suffix")
                    return@forEach
                }

                log.add("$prefix${getPadding(message)}$message${" ".repeat(lineLength - message.length - getPadding(message).length)}$suffix")
            }

            lastKey = key
        }

        log.add(border)

        return log
    }
}