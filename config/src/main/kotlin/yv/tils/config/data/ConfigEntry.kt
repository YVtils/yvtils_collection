package yv.tils.config.data

data class ConfigEntry(
    val key: String,
    val type: EntryType,
    var value: Any?,
    val defaultValue: Any?,
    val description: String?,
)