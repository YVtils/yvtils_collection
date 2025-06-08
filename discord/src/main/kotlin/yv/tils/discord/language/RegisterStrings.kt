package yv.tils.discord.language

import language.FileTypes
import language.LanguageProvider
import language.LanguageProvider.Companion.registerNewString

class RegisterStrings : LanguageProvider.RegisterStrings {
    override fun registerStrings() {
        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Send a whitelist request to the Minecraft server.",
                FileTypes.DE to "Sendet eine Whitelist-Anfrage an den Minecraft-Server.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEADD_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Force-add a player to the whitelist and optionally link their Discord account.",
                FileTypes.DE to "Fügt einen Spieler zwangsweise zur Whitelist hinzu und verknüpft optional Discord.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEADD_ARGS_MINECRAFTNAME_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Minecraft name of the player to force-add.",
                FileTypes.DE to "Minecraft-Name des Spielers, der zwangsweise hinzugefügt werden soll.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEADD_ARGS_DISCORDUSER_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Discord user to link with the Minecraft player.",
                FileTypes.DE to "Discord-Benutzer, der mit dem Spieler verknüpft werden soll.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Force-remove a player from the whitelist. Dropdown shown if no args are given.",
                FileTypes.DE to "Entfernt Spieler von der Whitelist. Auswahlmenü bei fehlenden Argumenten.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_ARGS_SITE_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Enter whitelist page (25 per page). Leave blank for page one.",
                FileTypes.DE to "Whitelist-Seite angeben (25 pro Seite). Leer lassen für Seite 1.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_ARGS_DISCORDUSER_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Discord user to remove from the whitelist.",
                FileTypes.DE to "Discord-Benutzer, der von der Whitelist entfernt werden soll.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_ARGS_MINECRAFTNAME_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Minecraft name of the player to remove.",
                FileTypes.DE to "Minecraft-Name des Spielers, der entfernt werden soll.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_CHECK_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Check if a player is whitelisted.",
                FileTypes.DE to "Prüft, ob ein Spieler auf der Whitelist ist.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_CHECK_ARGS_MINECRAFTNAME_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Minecraft name of the player to check.",
                FileTypes.DE to "Minecraft-Name des Spielers, der geprüft werden soll.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_CHECK_ARGS_DISCORDUSER_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Discord user to check.",
                FileTypes.DE to "Discord-Benutzer, der geprüft werden soll.",
            )
        )

        registerNewString(
            LangStrings.SLASHCOMMANDS_MCINFO_DESCRIPTION,
            mapOf(
                FileTypes.EN to "Get information about the Minecraft server.",
                FileTypes.DE to "Erhalte Informationen über den Minecraft-Server.",
            )
        )
    }

    enum class LangStrings(override val key: String) : LanguageProvider.LangStrings {
        SLASHCOMMANDS_WHITELIST_DESCRIPTION("discord.slashcommands.whitelist.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEADD_DESCRIPTION("discord.slashcommands.whitelist.subcommands.forceAdd.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEADD_ARGS_MINECRAFTNAME_DESCRIPTION("discord.slashcommands.whitelist.subcommands.forceAdd.args.minecraftName.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEADD_ARGS_DISCORDUSER_DESCRIPTION("discord.slashcommands.whitelist.subcommands.forceAdd.args.discordUser.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_DESCRIPTION("discord.slashcommands.whitelist.subcommands.forceRemove.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_ARGS_SITE_DESCRIPTION("discord.slashcommands.whitelist.subcommands.forceRemove.args.site.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_ARGS_DISCORDUSER_DESCRIPTION("discord.slashcommands.whitelist.subcommands.forceRemove.args.discordUser.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_FORCEREMOVE_ARGS_MINECRAFTNAME_DESCRIPTION("discord.slashcommands.whitelist.subcommands.forceRemove.args.minecraftName.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_CHECK_DESCRIPTION("discord.slashcommands.whitelist.subcommands.check.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_CHECK_ARGS_MINECRAFTNAME_DESCRIPTION("discord.slashcommands.whitelist.subcommands.check.args.minecraftName.description"),
        SLASHCOMMANDS_WHITELIST_SUBCOMMANDS_CHECK_ARGS_DISCORDUSER_DESCRIPTION("discord.slashcommands.whitelist.subcommands.check.args.discordUser.description"),
        SLASHCOMMANDS_MCINFO_DESCRIPTION("discord.slashcommands.mcinfo.description"),

        ;
    }
}
