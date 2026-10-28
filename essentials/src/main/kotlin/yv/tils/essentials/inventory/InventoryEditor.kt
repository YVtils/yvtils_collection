package yv.tils.essentials.inventory

import org.bukkit.Bukkit
import org.bukkit.GameMode
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import xyz.xenondevs.invui.Click
import xyz.xenondevs.invui.inventory.event.ItemPreUpdateEvent
import xyz.xenondevs.invui.inventory.event.PlayerUpdateReason
import xyz.xenondevs.invui.inventory.event.UpdateReason
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import yv.tils.essentials.language.LangStrings
import yv.tils.utils.logger.Logger
import yv.tils.utils.modules.Core
import yv.tils.utils.stats.ModuleTelemetry
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

/** Server-thread, slot-by-slot transactions: never copy an old GUI snapshot over live player data. */
object InventoryEditor : Listener {
    private val sessions = ConcurrentHashMap<UUID, InventorySession>()
    private val joining = ConcurrentHashMap<UUID, Long>()
    private var ticker: BukkitTask? = null

    fun start() {
        Bukkit.getPluginManager().registerEvents(this, Core.instance)
        ticker = Bukkit.getScheduler().runTaskTimer(Core.instance, Runnable {
            sessions.values.toList().forEach { session ->
                if (!session.offline) {
                    val target = Bukkit.getPlayer(session.target)
                    if (target == null) close(session)
                    else {
                        refresh(session, snapshot(target, session.kind))
                    }
                }
            }
        }, 1L, 1L)
    }

    @Synchronized
    fun open(viewer: Player, input: String, kind: InventoryKind) {
        sessions.values.filter { it.viewer == viewer.uniqueId }.toList().forEach(::close)
        val uuid = runCatching { UUID.fromString(input) }.getOrNull()
        val target = uuid?.let(Bukkit::getOfflinePlayer) ?: Bukkit.getOfflinePlayerIfCached(input)
        if (target == null || !target.hasPlayedBefore() && !target.isOnline) {
            viewer.sendMessage(InventoryPresentation.message(LangStrings.INVENTORY_NOT_FOUND, viewer))
            return
        }
        if (target.uniqueId == viewer.uniqueId) {
            viewer.sendMessage(InventoryPresentation.message(LangStrings.INVENTORY_SELF, viewer))
            return
        }
        if (sessions.containsKey(target.uniqueId)) {
            viewer.sendMessage(InventoryPresentation.message(LangStrings.INVENTORY_BUSY, viewer))
            return
        }
        if ((joining[target.uniqueId] ?: 0L) > System.currentTimeMillis() - 60_000L) {
            viewer.sendMessage(reopen(viewer, LangStrings.INVENTORY_CONNECTING, kind, target.uniqueId))
            return
        }
        try {
            val online = target.player
            val loaded = if (online == null) OfflineInventoryStorage.load(target.uniqueId, kind) else null
            val session = InventorySession(
                target.uniqueId, viewer.uniqueId, kind, online == null,
                loaded?.items ?: snapshot(online!!, kind), loaded?.fingerprint
            )
            val targetName = target.name ?: target.uniqueId.toString()
            session.window = InventoryPresentation.window(
                session, viewer, targetName,
                exchange = { slot, click, admin -> exchange(session, slot, click, admin) },
                preUpdate = { event -> preUpdate(session, event) },
                closed = {
                    session.locked = true
                    sessions.remove(session.target, session)
                }
            )
            sessions[target.uniqueId] = session
            session.window.open()
            if (!session.window.isOpen) {
                sessions.remove(target.uniqueId, session)
                return
            }
            ModuleTelemetry.record(ModuleTelemetry.Event.ESSENTIALS_INVENTORY_VIEWS)
            viewer.sendMessage(
                InventoryPresentation.message(
                    if (viewer.hasPermission(kind.editPermission)) LangStrings.INVENTORY_OPEN_EDIT
                    else LangStrings.INVENTORY_OPEN_READ_ONLY,
                    viewer, mapOf("player" to targetName)
                )
            )
        } catch (error: Exception) {
            sessions[target.uniqueId]?.takeIf { it.viewer == viewer.uniqueId }?.let(::close)
            Logger.error("Could not open inventory for ${target.uniqueId}", error)
            viewer.sendMessage(InventoryPresentation.message(LangStrings.INVENTORY_LOAD_FAILED, viewer))
        }
    }

    private fun snapshot(player: Player, kind: InventoryKind): Array<ItemStack?> =
        (if (kind == InventoryKind.PLAYER) player.inventory.contents else player.enderChest.contents)
            .map { it?.clone()?.takeUnless { item -> item.type.isAir } }.toTypedArray().copyOf(kind.slots)

    private fun refresh(session: InventorySession, next: Array<ItemStack?>) {
        val changed = next.indices.filter { !InventoryEditPolicy.same(session.expected[it], next[it]) }
        session.expected = next
        changed.forEach { session.inventory.setItem(UpdateReason.SUPPRESSED, it, next[it]) }
    }

    private fun allowed(session: InventorySession, viewer: Player) =
        viewer.uniqueId == session.viewer && !session.locked && !session.dragPending && sessions[session.target] === session &&
            viewer.hasPermission(session.kind.permission) && viewer.hasPermission(session.kind.editPermission)

    private fun copy(items: Array<ItemStack?>) = items.map { it?.clone()?.takeUnless { item -> item.type.isAir } }.toTypedArray()

    /** Writes only changed slots, after checking each against the displayed snapshot. */
    private fun commit(session: InventorySession, next: Array<ItemStack?>) {
        val changed = next.indices.filter { !InventoryEditPolicy.same(session.expected[it], next[it]) }
        if (changed.isEmpty()) return
        if (session.offline) OfflineInventoryStorage.save(session, next)
        else {
            val live = checkNotNull(Bukkit.getPlayer(session.target)) { "Target disconnected; reopen the editor." }
            val current = snapshot(live, session.kind)
            check(changed.all { InventoryEditPolicy.same(session.expected[it], current[it]) }) { "Slot changed; retry after refresh." }
            val inventory = if (session.kind == InventoryKind.PLAYER) live.inventory else live.enderChest
            changed.forEach { inventory.setItem(it, next[it]?.clone()); current[it] = next[it]?.clone() }
            live.updateInventory()
            refresh(session, current)
            return
        }
        refresh(session, next)
    }

    private fun failed(session: InventorySession, viewer: Player, error: Exception) {
        viewer.sendMessage(reopen(viewer, LangStrings.INVENTORY_EDIT_FAILED, session.kind, session.target))
        Logger.warn("Inventory edit failed for ${session.target}: ${error.message}")
    }

    /** All target clicks are cancelled by InvUI and applied through an explicit transaction. */
    @Synchronized
    private fun exchange(session: InventorySession, slot: Int, click: Click, admin: Boolean) {
        val viewer = click.player()
        if (!allowed(session, viewer)) return
        try {
            val next = copy(session.expected)
            if (click.clickType().isShiftClick) {
                val own = copy(viewer.inventory.storageContents)
                if (admin) {
                    val order = if (session.kind == InventoryKind.PLAYER) (9..35).toList() + (0..8).toList() else (0..26).toList()
                    InventoryTransfers.move(own, slot, next, order)
                } else InventoryTransfers.move(next, slot, own, (0..35).toList())
                commit(session, next)
                viewer.inventory.storageContents = own
                session.adminInventory.notifyWindows()
            } else if (!admin && click.clickType() == ClickType.LEFT) {
                val previous = next[slot]?.clone()
                next[slot] = viewer.itemOnCursor.clone().takeUnless { it.type.isAir }
                commit(session, next)
                viewer.setItemOnCursor(previous)
            } else if (!admin && click.clickType() == ClickType.RIGHT) {
                val change = InventoryTransfers.rightClick(next[slot], viewer.itemOnCursor.takeUnless { it.type.isAir })
                next[slot] = change.slot
                commit(session, next)
                viewer.setItemOnCursor(change.cursor)
            } else if (!admin && click.clickType() == ClickType.MIDDLE && viewer.gameMode == GameMode.CREATIVE) {
                if (!session.offline) {
                    val live = checkNotNull(Bukkit.getPlayer(session.target)) { "Target disconnected; reopen the editor." }
                    check(InventoryEditPolicy.same(next[slot], snapshot(live, session.kind)[slot])) { "Slot changed; retry after refresh." }
                }
                viewer.setItemOnCursor(InventoryTransfers.middleClick(
                    next[slot], viewer.itemOnCursor.takeUnless { it.type.isAir }, true
                ))
            }
        } catch (error: Exception) {
            failed(session, viewer, error)
        }
    }

    /** InvUI's drag callbacks may be previews; cancel them and plan the whole drag once. */
    @Synchronized
    private fun preUpdate(session: InventorySession, event: ItemPreUpdateEvent) {
        val reason = event.updateReason
        if (reason !is PlayerUpdateReason.Drag) {
            if (event.inventory === session.inventory || session.dragPending) event.isCancelled = true
            return
        }
        event.isCancelled = true
        if (session.lastDrag === reason) return
        session.lastDrag = reason
        val viewer = reason.player()
        if (!allowed(session, viewer) || reason.clickType() !in setOf(ClickType.LEFT, ClickType.RIGHT)) return
        val cursor = viewer.itemOnCursor.clone().takeUnless { it.type.isAir } ?: return
        val ownBefore = copy(viewer.inventory.storageContents)
        val expectedBefore = copy(session.expected)
        val own = copy(ownBefore)
        val next = copy(expectedBefore)
        val slots = reason.slots().mapNotNull { link ->
            when (link.inventory()) {
                session.inventory -> next to link.slot()
                session.adminInventory -> own to ((link.slot() + 9) % 36)
                else -> null
            }
        }
        val remaining = InventoryTransfers.distribute(cursor, slots, reason.clickType() == ClickType.RIGHT)
        session.dragPending = true
        Bukkit.getScheduler().runTask(Core.instance, Runnable {
            synchronized(this) {
                session.dragPending = false
                if (!allowed(session, viewer)) return@synchronized
                try {
                    check(InventoryEditPolicy.same(cursor, viewer.itemOnCursor)) { "Cursor changed during drag." }
                    check(ownBefore.indices.all { InventoryEditPolicy.same(ownBefore[it], viewer.inventory.getItem(it)) }) {
                        "Admin inventory changed during drag."
                    }
                    check(expectedBefore.indices.all { InventoryEditPolicy.same(expectedBefore[it], session.expected[it]) }) {
                        "Target inventory changed during drag."
                    }
                    commit(session, next)
                    viewer.inventory.storageContents = own
                    viewer.setItemOnCursor(remaining)
                    session.adminInventory.notifyWindows()
                } catch (error: Exception) {
                    failed(session, viewer, error)
                }
            }
        })
    }

    private fun reopen(viewer: Player, key: LangStrings, kind: InventoryKind, target: UUID, name: String = target.toString()): Component =
        InventoryPresentation.message(key, viewer, mapOf("player" to name))
            .append(Component.space())
            .append(InventoryPresentation.message(LangStrings.INVENTORY_REOPEN, viewer)
                .clickEvent(ClickEvent.runCommand("/${kind.command} $target"))
                .hoverEvent(InventoryPresentation.message(LangStrings.INVENTORY_REOPEN_HINT, viewer)))

    @EventHandler(priority = EventPriority.LOWEST)
    fun join(event: PlayerJoinEvent) {
        joining.remove(event.player.uniqueId)
        // Offline editing was locked during pre-login, before player data could be loaded.
        sessions[event.player.uniqueId]?.let(::close)
    }

    @Synchronized
    @EventHandler(priority = EventPriority.MONITOR)
    fun preLogin(event: AsyncPlayerPreLoginEvent) {
        if (event.loginResult != AsyncPlayerPreLoginEvent.Result.ALLOWED) return
        joining[event.uniqueId] = System.currentTimeMillis()
        val session = sessions[event.uniqueId]?.takeIf { it.offline && !it.locked } ?: return
        // Shares the editor monitor with open/exchange: any in-flight save and cursor exchange
        // finish before pre-login returns, and no further offline write can start afterward.
        session.locked = true
        val targetName = event.name
        Bukkit.getScheduler().runTask(Core.instance, Runnable {
            close(session)
            Bukkit.getPlayer(session.viewer)?.let { viewer ->
                viewer.sendMessage(
                    reopen(viewer, LangStrings.INVENTORY_LOGIN_HANDOFF, session.kind, session.target, targetName)
                )
            }
        })
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun quit(event: PlayerQuitEvent) {
        joining.remove(event.player.uniqueId)
        sessions.values.filter { it.target == event.player.uniqueId || it.viewer == event.player.uniqueId }
            .toList().forEach { session ->
                close(session)
                if (session.target == event.player.uniqueId) Bukkit.getPlayer(session.viewer)?.let { viewer ->
                    viewer.sendMessage(reopen(viewer, LangStrings.INVENTORY_LOGOUT_HANDOFF, session.kind, session.target, event.player.name))
                }
            }
    }

    private fun close(session: InventorySession) {
        session.locked = true
        sessions.remove(session.target, session)
        session.window.close()
    }

    fun closeAll() {
        ticker?.cancel()
        sessions.values.toList().forEach(::close)
        joining.clear()
    }
}
