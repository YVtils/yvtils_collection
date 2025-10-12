package yv.tils.multiMine.configs

import org.bukkit.Material
import org.bukkit.Tag
import yv.tils.config.data.ConfigEntry
import yv.tils.config.data.EntryType
import yv.tils.config.files.FileUtils
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.Logger

// TODO: Think about splitting block list and config file into separate files
class ConfigFile {
    companion object {
        val config: MutableMap<String, Any> = mutableMapOf()
        var blockList: MutableList<Material> = mutableListOf()
    }

    fun updateBlockList(blocks: MutableList<Material>) {
        blockList = blocks

        config["blocks"] = blocks.map { it.name }

        CoroutineHandler.launchTask(
            suspend { registerStrings(config) },
            null,
            isOnce = true,
        )
    }

    fun loadConfig() {
        val file = FileUtils.loadYAMLFile("/multiMine/config.yml")

        for (key in file.content.getKeys(true)) {
            val value = file.content.get(key)

            Logger.debug("Loading config key: $key -> $value")
            config[key] = value as Any
        }

        loadBlockList(file)
    }

    private fun loadBlockList(file: FileUtils.Companion.YAMLFile) {
        val blocks = file.content.getStringList("blocks")
        blocks.forEach {
            try {
                blockList.add(Material.getMaterial(it)!!)
            } catch (e: NullPointerException) {
                Logger.error("Trying to load a block that does not exist: $it")
            }
        }
    }

    fun registerStrings(content: MutableMap<String, Any> = mutableMapOf()) {
        if (content.isEmpty()) {
            content["documentation"] = "https://docs.yvtils.net/multiMine/config.yml"
            content["defaultState"] = true
            content["animationTime"] = 3
            content["cooldownTime"] = 3
            content["breakLimit"] = 250
            content["leaveDecay"] = true
            content["matchBlockTypeOnly"] = true // TODO: Test if this also works in deactivated state
            content["blocks"] = createTemplateBlocks()
        }

        val ymlFile = FileUtils.makeYAMLFile("/multiMine/config.yml", content)
        FileUtils.saveFile("/multiMine/config.yml", ymlFile)
    }

    // TODO: Implement this as new config logic
    fun registerStrings() {
        val content = mutableListOf<ConfigEntry>()

        content.add(ConfigEntry(
            "documentation",
            EntryType.STRING,
            null,
            "https://docs.yvtils.net/multiMine/config.yml",
            "Link to the documentation of the config file"
        ))
        content.add(ConfigEntry(
            "defaultState",
            EntryType.BOOLEAN,
            null,
            true,
            "If MultiMine should be enabled by default for new players"
        ))
        content.add(ConfigEntry(
            "animationTime",
            EntryType.INT,
            mapOf("min" to 1, "max" to 20),
            3,
            "The time in ticks for the breaking animation (1 tick = 1/20 second)"
        ))
        content.add(ConfigEntry(
            "cooldownTime",
            EntryType.INT,
            mapOf("min" to 0, "max" to 20),
            3,
            "The cooldown time in ticks before the next block can be broken (1 tick = 1/20 second)"
        ))
        content.add(ConfigEntry(
            "breakLimit",
            EntryType.INT,
            mapOf("min" to 1, "max" to 10000),
            250,
            "The maximum number of blocks that can be broken in one MultiMine session"
        ))
        content.add(ConfigEntry(
            "leaveDecay",
            EntryType.BOOLEAN,
            null,
            true,
            "If the broken blocks should decay over time after the player leaves"
        ))
        content.add(ConfigEntry(
            "matchBlockTypeOnly",
            EntryType.BOOLEAN,
            null,
            true,
            "If only blocks of the same type as the initially broken block should be considered for breaking"
        ))
        content.add(ConfigEntry(
            "blocks",
            EntryType.LIST,
            null,
            createTemplateBlocks(),
            "The list of blocks that can be broken with MultiMine"
        ))

    }

    // TODO: Test if list gets updated with version updates
    private fun createTemplateBlocks(): List<String> {
        val blocks = Tag.LOGS.values.toMutableList()

        val ores = listOf(
            Material.COAL_ORE,
            Material.IRON_ORE,
            Material.GOLD_ORE,
            Material.DIAMOND_ORE,
            Material.EMERALD_ORE,
            Material.LAPIS_ORE,
            Material.REDSTONE_ORE,
            Material.COPPER_ORE,
            Material.DEEPSLATE_COAL_ORE,
            Material.DEEPSLATE_IRON_ORE,
            Material.DEEPSLATE_GOLD_ORE,
            Material.DEEPSLATE_DIAMOND_ORE,
            Material.DEEPSLATE_EMERALD_ORE,
            Material.DEEPSLATE_LAPIS_ORE,
            Material.DEEPSLATE_REDSTONE_ORE,
            Material.DEEPSLATE_COPPER_ORE,
            Material.NETHER_QUARTZ_ORE,
            Material.NETHER_GOLD_ORE,
            Material.ANCIENT_DEBRIS,
            Material.GLOWSTONE,
        )
        blocks.addAll(ores)

        return blocks.map { it.name }
    }
}
