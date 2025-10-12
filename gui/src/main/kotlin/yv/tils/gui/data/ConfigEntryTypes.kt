package yv.tils.gui.data

enum class ConfigEntryTypes(val type: String, val clickActions: List<ClickActions>) {
    STRING(
        "String",
        listOf(ClickActions.MODIFY_TEXT),
    ),
    INT(
        "Int",
        listOf(ClickActions.INCREMENT_VALUE, ClickActions.DECREMENT_VALUE, ClickActions.INCREMENT_VALUE_SHIFT, ClickActions.DECREMENT_VALUE_SHIFT),
    ),
    DOUBLE(
        "Double",
        listOf(ClickActions.INCREMENT_VALUE, ClickActions.DECREMENT_VALUE, ClickActions.INCREMENT_VALUE_SHIFT, ClickActions.DECREMENT_VALUE_SHIFT),
    ),
    BOOLEAN(
        "Boolean",
        listOf(ClickActions.TOGGLE_OPTION),
    ),
    LIST(
        "List",
        listOf(ClickActions.OPEN_SETTING),
    ),
    MAP(
        "Map",
        listOf(ClickActions.OPEN_SETTING),
    ),
    UNKNOWN(
        "Unknown",
        listOf(),
    );

    companion object {
        fun fromEntryType(t: yv.tils.config.data.EntryType): ConfigEntryTypes {
            return when (t) {
                yv.tils.config.data.EntryType.STRING -> STRING
                yv.tils.config.data.EntryType.INT -> INT
                yv.tils.config.data.EntryType.DOUBLE -> DOUBLE
                yv.tils.config.data.EntryType.BOOLEAN -> BOOLEAN
                yv.tils.config.data.EntryType.LIST -> LIST
                yv.tils.config.data.EntryType.MAP -> MAP
                yv.tils.config.data.EntryType.ANY -> UNKNOWN
                yv.tils.config.data.EntryType.UNKNOWN -> UNKNOWN
            }
        }
    }
}