/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.gui.utils

import com.destroystokyo.paper.profile.PlayerProfile
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.server.PluginDisableEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import xyz.xenondevs.invui.item.Item
import yv.tils.utils.modules.Core
import java.lang.ref.WeakReference
import java.util.Locale
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/**
 * Shared player-skin cache for inventory icons and player-head selection.
 * Call on the server thread. Paper performs profile.update() asynchronously;
 * item creation, cache access and InvUI refreshes stay on the server thread.
 */
object PlayerHeads : Listener {
    private data class Key(val uuid: UUID?, val name: String?)

    private val profiles = HeadProfileCache<Key, PlayerProfile>(
        usable = { it.hasTextures() },
        load = { Bukkit.createProfile(it.uuid, it.name).update().orTimeout(15, TimeUnit.SECONDS) }
    )
    private var registered = false
    private var generation = 0

    private fun prepare() {
        check(Bukkit.isPrimaryThread()) { "Player heads must be accessed on the server thread" }
        if (!registered) {
            Bukkit.getPluginManager().registerEvents(this, Core.instance)
            registered = true
        }
    }

    private fun online(uuid: UUID?, name: String?): PlayerProfile? =
        (if (uuid != null) Bukkit.getPlayer(uuid) else name?.let(Bukkit::getPlayerExact))
            ?.playerProfile?.takeIf { it.hasTextures() }

    private fun key(uuid: UUID?, name: String?): Key {
        require(uuid != null || !name.isNullOrBlank()) { "A player UUID or name is required" }
        // A stored name must never change which UUID is resolved.
        return Key(uuid, if (uuid == null) name?.lowercase(Locale.ROOT) else null)
    }

    /** Returns a fresh stack; unresolved players use a neutral head until [load] refreshes it. */
    fun head(uuid: UUID, name: String? = null): ItemStack {
        prepare()
        val profile = online(uuid, name) ?: profiles.peek(key(uuid, name))
        return ItemStack(Material.PLAYER_HEAD).apply {
            if (profile != null) editMeta { (it as SkullMeta).playerProfile = profile }
        }
    }

    /** Shares in-flight UUID lookups across all inventories. The returned future is a defensive copy. */
    fun resolve(uuid: UUID? = null, name: String? = null): CompletableFuture<PlayerProfile> {
        prepare()
        val identity = key(uuid, name)
        return online(uuid, name)?.let { CompletableFuture.completedFuture(it.clone()) }
            ?: profiles.resolve(identity).thenApply { it.clone() }
    }

    /** Refreshes an item's providers after resolution, without retaining closed inventories. */
    fun load(uuid: UUID, item: Item, name: String? = null) {
        prepare()
        if (online(uuid, name) != null || profiles.peek(key(uuid, name)) != null) return
        val reference = WeakReference(item)
        val expected = generation
        val plugin = Core.instance
        resolve(uuid, name).whenComplete { profile, error ->
            if (error != null || profile?.hasTextures() != true || !plugin.isEnabled) return@whenComplete
            try {
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    if (generation == expected && plugin.isEnabled) reference.get()?.notifyWindows()
                })
            } catch (_: org.bukkit.plugin.IllegalPluginAccessException) {
                // Plugin shutdown can race with completion of a remote lookup.
            }
        }
    }

    @EventHandler
    fun onPluginDisable(event: PluginDisableEvent) {
        if (event.plugin !== Core.instance) return
        generation++
        profiles.clear()
        registered = false
    }
}
