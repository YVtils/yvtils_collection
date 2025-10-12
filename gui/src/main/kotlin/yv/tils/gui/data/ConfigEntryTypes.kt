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
}