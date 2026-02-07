/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.discord.actions.buttons.handler

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import yv.tils.discord.logic.whitelist.WhitelistComponents
import yv.tils.discord.logic.whitelist.WhitelistLogic

class JDAPageSwitch {
    fun executeSitePrevious(e: ButtonInteractionEvent) {
        val currentPage = getCurrentPage(e)
        val maxPage = WhitelistLogic.getTotalPagesCount()

        if (currentPage <= 1) {
            switchPage(e, 1)
            return
        }

        if (currentPage > maxPage) {
            switchPage(e, maxPage)
            return
        }

        val newPage = currentPage - 1
        switchPage(e, newPage)
    }

    fun executeSiteNext(e: ButtonInteractionEvent) {
        val currentPage = getCurrentPage(e)
        val maxPage = WhitelistLogic.getTotalPagesCount()

        if (currentPage < 1) {
            switchPage(e, 1)
            return
        }

        if (currentPage >= maxPage) {
            switchPage(e, maxPage)
            return
        }

        val newPage = currentPage + 1
        switchPage(e, newPage)
    }

    private fun getCurrentPage(e: ButtonInteractionEvent): Int {
        return e.message.components
            .asSequence()
            .mapNotNull { component ->
                try {
                    val container = component.asContainer()
                    container.components.find { it.uniqueId == 999 }?.asTextDisplay()?.content
                } catch (_: Exception) {
                    null
                }
            }
            .firstNotNullOfOrNull { footerText ->
                footerText.split("•")
                    .find { it.contains("Site") }
                    ?.trim()
                    ?.split(" ")
                    ?.let { sitePart ->
                        val siteIndex = sitePart.indexOf("Site")
                        if (siteIndex != - 1 && siteIndex + 1 < sitePart.size) {
                            sitePart[siteIndex + 1].toIntOrNull()
                        } else null
                    }
            } ?: - 1
    }

    private fun switchPage(e: ButtonInteractionEvent, page: Int) {
        e.deferEdit().queue()

        val hook = e.hook

        hook.editOriginalComponents(
            WhitelistComponents().forceRemoveContainer(page)
        ).useComponentsV2().queue()
    }
}
