package yv.tils.discord.gui

import com.destroystokyo.paper.profile.PlayerProfile
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import xyz.xenondevs.invui.item.Item
import yv.tils.discord.logic.whitelist.WhitelistEntry
import yv.tils.utils.modules.Core
import yv.tils.utils.logger.Logger
import java.util.UUID
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit

/** Server-thread cache; Paper's update() performs remote profile resolution asynchronously. */
object AccountHeads {
    private data class Cached(val future: CompletableFuture<PlayerProfile>, val created: Long)

    private val profiles = mutableMapOf<UUID, Cached>()

    fun head(entry: WhitelistEntry): ItemStack = ItemStack(Material.PLAYER_HEAD).apply {
        val uuid = runCatching { UUID.fromString(entry.minecraftUUID) }.getOrNull() ?: return@apply
        val online = Bukkit.getPlayer(uuid)?.playerProfile
        val cached = profiles[uuid]?.future?.let { if (it.isCompletedExceptionally) null else it.getNow(null) }
        val meta = itemMeta as SkullMeta
        meta.playerProfile = online ?: cached ?: Bukkit.createProfile(uuid, entry.minecraftName)
        itemMeta = meta
    }

    fun load(entry: WhitelistEntry, item: Item) {
        val uuid = runCatching { UUID.fromString(entry.minecraftUUID) }.getOrNull() ?: return
        if (Bukkit.getPlayer(uuid)?.playerProfile?.hasTextures() == true) return
        val now = System.currentTimeMillis()
        profiles.entries.removeIf { now - it.value.created > 600000 }
        val cached = profiles.getOrPut(uuid) {
            Cached(Bukkit.createProfile(uuid).update().orTimeout(15, TimeUnit.SECONDS), now)
        }
        cached.future.whenComplete { profile, error ->
            if (!Core.instance.isEnabled) return@whenComplete
            Bukkit.getScheduler().runTask(Core.instance, Runnable {
                if (error == null && profile?.hasTextures() == true) item.notifyWindows()
                else Logger.debug("Account head texture unavailable for $uuid: ${error?.message ?: "no textures"}")
            })
        }
    }
}
