package yv.tils.discord.actions.buttons.handler

import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import yv.tils.discord.logic.whitelist.WhitelistEmbeds
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
        val footerText = e.message.embeds.firstOrNull()?.footer?.text ?: return 1

        val splitText = footerText.split("•")
        for (part in splitText) {
            if (part.contains("Site")) {
                val sitePart = part.trim().split(" ")
                if (sitePart.size >= 3) {
                    val siteIndex = sitePart.indexOf("Site")
                    if (siteIndex != - 1 && siteIndex + 1 < sitePart.size) {
                        return sitePart[siteIndex + 1].toIntOrNull() ?: 1
                    }
                }
            }
        }

        return e.message.embeds.firstOrNull()?.footer?.text?.split(" / ")?.getOrNull(0)?.toIntOrNull() ?: 1
    }

    private fun switchPage(e: ButtonInteractionEvent, page: Int) {
        e.deferEdit().queue()
        val embed = WhitelistEmbeds().forceRemoveEmbed(page)

        val entries = WhitelistLogic.getEntriesBySite(page)

        val hook = e.hook
        hook.editOriginalEmbeds(embed.build()).setComponents(
            ActionRow.of(WhitelistEmbeds().forceRemoveActionRowDropdown(entries).build()),
            ActionRow.of(WhitelistEmbeds().forceRemoveActionRowButtons(page))
        ).queue()
    }
}
