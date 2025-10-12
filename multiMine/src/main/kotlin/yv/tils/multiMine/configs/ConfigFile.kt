package yv.tils.multiMine.configs

import org.bukkit.Material
import org.bukkit.Tag
import yv.tils.config.data.ConfigEntry
import yv.tils.config.data.EntryType
import yv.tils.config.files.YMLFileUtils
import yv.tils.utils.coroutine.CoroutineHandler
import yv.tils.utils.logger.Logger

// TODO: Think about splitting block list and config file into separate files
class ConfigFile {
    companion object {
        val config: MutableMap<String, Any> = mutableMapOf()
        var blockList: MutableList<Material> = mutableListOf()
        val configNew: MutableList<ConfigEntry> = mutableListOf()
        private val configIndex: MutableMap<String, ConfigEntry> = mutableMapOf()

        fun getConfigEntry(key: String): ConfigEntry? = configIndex[key]

        fun get(key: String): Any? {
            val e = getConfigEntry(key)
            return e?.value ?: e?.defaultValue ?: config[key]
        }

        fun getString(key: String): String? = get(key)?.toString()
        fun getInt(key: String): Int? = (get(key) as? Number)?.toInt()
        fun getBoolean(key: String): Boolean? = when (val v = get(key)) {
            is Boolean -> v
            is String -> v.toBoolean()
            else -> null
        }
    }

    fun updateBlockList(blocks: MutableList<Material>) {
        blockList = blocks

        config["blocks"] = blocks.map { it.name }
        // keep ConfigEntry list in sync
        val existing = getConfigEntry("blocks")
        if (existing != null) {
            existing.value = blocks.map { it.name }
        } else {
            val entry = ConfigEntry("blocks", EntryType.LIST, blocks.map { it.name }, createTemplateBlocks(), "Block list")
            configNew.add(entry)
            configIndex[entry.key] = entry
        }

        CoroutineHandler.launchTask(
            suspend { registerStrings(config) },
            null,
            isOnce = true,
        )
    }

    fun loadConfig() {
    val file = YMLFileUtils.loadYAMLFile("/multiMine/config.yml")
        // populate legacy config map
        for (key in file.content.getKeys(true)) {
            val value = file.content.get(key)

            Logger.debug("Loading config key: $key -> $value")
            if (value != null) config[key] = value
        }

        // ensure configNew contains base entries and then load values into them
        ensureBaseEntries()
        // load values into entries and populate index
        for (entry in configNew) {
            val v = file.content.get(entry.key)
            if (v != null) entry.value = v
            configIndex[entry.key] = entry
            val vv = entry.value ?: entry.defaultValue
            if (vv != null) config[entry.key] = vv
        }

        loadBlockList(file)
    }

    private fun loadBlockList(file: yv.tils.config.files.YMLFileUtils.Companion.YAMLFile) {
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
        // Always start from base default entries
        ensureBaseEntries()

        // If a map is provided, set entry.value from it
        if (content.isNotEmpty()) {
            for (entry in configNew) {
                if (content.containsKey(entry.key)) entry.value = content[entry.key]
            }
        }

        // sync index and legacy map
        syncEntriesToMap()

        val ymlFile = YMLFileUtils.makeYAMLFileFromEntries("/multiMine/config.yml", configNew)
        yv.tils.config.files.FileUtils.saveFile("/multiMine/config.yml", ymlFile)
    }

    private fun syncEntriesToMap() {
        configIndex.clear()
        for (entry in configNew) {
            configIndex[entry.key] = entry
            val vv = entry.value ?: entry.defaultValue
            if (vv != null) config[entry.key] = vv
        }
    }

    private fun ensureBaseEntries() {
        if (configNew.isNotEmpty()) return

        configNew.add(ConfigEntry(
            "documentation",
            EntryType.STRING,
            null,
            "https://docs.yvtils.net/multiMine/config.yml",
            "Documentation URL"
        ))
        configNew.add(ConfigEntry(
            "defaultState",
            EntryType.BOOLEAN,
            null,
            true,
            "Default enabled state",
            dynamicInvItem = { if (it.value as? Boolean == true) Material.LIME_DYE else Material.RED_DYE }
        ))
        configNew.add(ConfigEntry(
            "animationTime",
            EntryType.INT,
            null,
            3,
            "Animation time in ticks",
            Material.CLOCK
        ))
        configNew.add(ConfigEntry(
            "cooldownTime",
            EntryType.INT,
            null,
            3,
            "Cooldown time in ticks",
            Material.SNOWBALL
        ))
        configNew.add(ConfigEntry(
            "breakLimit",
            EntryType.INT,
            null,
            250,
            "Break limit",
            Material.DIAMOND_PICKAXE
        ))
        configNew.add(ConfigEntry(
            "leaveDecay",
            EntryType.BOOLEAN,
            null,
            true,
            "Leave decay",
            dynamicInvItem = { if (it.value as? Boolean == true) Material.OAK_LEAVES else Material.NETHER_WART_BLOCK }
        ))
        configNew.add(ConfigEntry(
            "matchBlockTypeOnly",
            EntryType.BOOLEAN,
            null,
            true,
            "Match block type only",
            dynamicInvItem = { if (it.value as? Boolean == true) Material.HOPPER else Material.RED_DYE }
        ))
        configNew.add(ConfigEntry(
            "blocks",
            EntryType.LIST,
            null,
            createTemplateBlocks(),
            "Block list",
            Material.BUNDLE
        ))
        // populate index for fast lookups
        for (entry in configNew) configIndex[entry.key] = entry
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
