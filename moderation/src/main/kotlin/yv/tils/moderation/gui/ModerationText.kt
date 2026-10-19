package yv.tils.moderation.gui

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.standard.StandardTags
import yv.tils.utils.colors.Colors
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/** Reasons may contain formatting, but never interactive tags or arbitrary placeholder expansion. */
object ModerationText {
    private val formatting = MiniMessage.builder().tags(
        TagResolver.builder()
            .resolver(StandardTags.color()).resolver(StandardTags.decorations())
            .resolver(StandardTags.newline()).build()
    ).build()

    fun reason(value: String): Component = formatting.deserialize(value)

    fun lore(value: Component): Component = Component.empty()
        .color(TextColor.fromHexString(Colors.TERTIARY.color))
        .decoration(TextDecoration.ITALIC, false).append(value)
}
