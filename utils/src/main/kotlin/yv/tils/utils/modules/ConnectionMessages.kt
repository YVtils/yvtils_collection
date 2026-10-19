package yv.tils.utils.modules

import org.bukkit.entity.Player
import java.util.function.BiConsumer
import java.util.function.Predicate
import java.util.function.Function
import net.kyori.adventure.text.Component

/** Optional feature hooks in the shared runtime; neither Server nor Vanish requires the other. */
object ConnectionMessages {
    var displayName: Function<Player, Component> =
        Function { Component.text(it.name) }
    var announcer: BiConsumer<Player, Boolean>? = null
    var hidden: Predicate<Player> = Predicate { false }
}
