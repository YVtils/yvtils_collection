package yv.tils.multiMine.configs

import coroutine.CoroutineHandler
import files.FileUtils
import logger.Logger
import org.bukkit.Material

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
            content["blocks"] = createTemplateBlocks()
        }

        val ymlFile = FileUtils.makeYAMLFile("/multiMine/config.yml", content)
        FileUtils.saveFile("/multiMine/config.yml", ymlFile)
    }

    private fun createTemplateBlocks(): List<String> {
        val blocks = listOf(
            Material.OAK_LOG.name,
            Material.BIRCH_LOG.name,
            Material.SPRUCE_LOG.name,
            Material.JUNGLE_LOG.name,
            Material.ACACIA_LOG.name,
            Material.DARK_OAK_LOG.name,
            Material.CHERRY_LOG.name,
            Material.MANGROVE_LOG.name,
            Material.PALE_OAK_WOOD.name,
            Material.CRIMSON_STEM.name,
            Material.WARPED_STEM.name,
            Material.STRIPPED_OAK_LOG.name,
            Material.STRIPPED_BIRCH_LOG.name,
            Material.STRIPPED_SPRUCE_LOG.name,
            Material.STRIPPED_JUNGLE_LOG.name,
            Material.STRIPPED_ACACIA_LOG.name,
            Material.STRIPPED_DARK_OAK_LOG.name,
            Material.STRIPPED_CHERRY_LOG.name,
            Material.STRIPPED_MANGROVE_LOG.name,
            Material.STRIPPED_PALE_OAK_WOOD.name,
            Material.STRIPPED_CRIMSON_STEM.name,
            Material.STRIPPED_WARPED_STEM.name,

            Material.COAL_ORE.name,
            Material.IRON_ORE.name,
            Material.GOLD_ORE.name,
            Material.DIAMOND_ORE.name,
            Material.EMERALD_ORE.name,
            Material.LAPIS_ORE.name,
            Material.REDSTONE_ORE.name,
            Material.COPPER_ORE.name,
            Material.DEEPSLATE_COAL_ORE.name,
            Material.DEEPSLATE_IRON_ORE.name,
            Material.DEEPSLATE_GOLD_ORE.name,
            Material.DEEPSLATE_DIAMOND_ORE.name,
            Material.DEEPSLATE_EMERALD_ORE.name,
            Material.DEEPSLATE_LAPIS_ORE.name,
            Material.DEEPSLATE_REDSTONE_ORE.name,
            Material.DEEPSLATE_COPPER_ORE.name,
            Material.NETHER_QUARTZ_ORE.name,
            Material.NETHER_GOLD_ORE.name,
            Material.ANCIENT_DEBRIS.name,
            Material.GLOWSTONE.name,
        )

        return blocks
    }
}