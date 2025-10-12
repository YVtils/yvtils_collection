package yv.tils.config.data

import org.bukkit.Material

data class ConfigEntry(
    val key: String,
    val type: EntryType,
    var value: Any?,
    val defaultValue: Any?,
    val description: String?,
    val invItem: Material? = null,
    val dynamicInvItem: ((ConfigEntry) -> Material)? = null,
)