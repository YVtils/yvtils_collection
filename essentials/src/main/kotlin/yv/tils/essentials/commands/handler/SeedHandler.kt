/*
 * Part of the YVtils Project.
 * Copyright (c) 2025 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.essentials.commands.handler

import yv.tils.utils.data.Data
import yv.tils.config.language.LanguageHandler
import yv.tils.utils.message.MessageUtils
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender

class SeedHandler {
    fun seed(sender: CommandSender) {
        val seedMap: MutableMap<Long, String> = mutableMapOf()

        for (world in Bukkit.getWorlds()) {
            if (world.seed !in seedMap.keys) {
                seedMap[world.seed] = world.name
            } else {
                seedMap[world.seed] = seedMap[world.seed] + ", " + world.name
            }
        }

        if (seedMap.keys.size == 1) {
            sender.sendMessage(
                MessageUtils.convert(
                    Data.prefix + " Seed: <gray>[<green><click:copy_to_clipboard:${seedMap.keys.first()}><hover:show_text:'${
                        LanguageHandler.getRawMessage(
                            "text.action.copy",
                            sender,
                        )
                    }'>${seedMap.keys.first()}</click><gray>]"
                )
            )
        } else {
            val seedList: MutableList<String> = mutableListOf()

            for (seed in seedMap.keys) {
                seedList.add(
                    "<white>${seedMap[seed]}: <gray>[<green><click:copy_to_clipboard:$seed><hover:show_text:'${
                        LanguageHandler.getRawMessage(
                            "text.action.copy",
                            sender,
                        )
                    }'>$seed</click><gray>]"
                )
            }

            sender.sendMessage(
                MessageUtils.convert(
                    Data.prefix + " Seeds:<newline>"
                ).append(
                    MessageUtils.convert(
                        seedList.joinToString("<newline>")
                    )
                )
            )
        }
    }
}
