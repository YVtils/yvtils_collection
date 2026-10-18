/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.yv_smp.logic.ambient

import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.SoundCategory
import org.bukkit.Vibration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitTask
import yv.tils.config.files.ConfigFormat
import yv.tils.config.files.ObjectMapperFileUtils
import yv.tils.utils.modules.Core
import java.util.UUID
import kotlin.random.Random

/** All access, including commands, runs on the server thread. No entities or chunks are retained. */
object AmbientSounds {
    private const val PATH = "/yv_smp/ambient-sources.yml"
    private val sources = linkedMapOf<String, AmbientSource>()
    private val nextPlay = mutableMapOf<String, Long>()
    private val listeners = mutableMapOf<String, MutableSet<UUID>>()
    private var task: BukkitTask? = null
    private var tick = 0L

    fun names(): Array<String> = sources.keys.toTypedArray()
    fun get(name: String): AmbientSource? = sources[name]

    fun start() {
        stop()
        sources.clear()
        ObjectMapperFileUtils.loadList<AmbientSource>(PATH, "sources", ConfigFormat.YAML).forEach { source ->
            if (valid(source) && source.name !in sources) {
                sources[source.name] = source
            } else {
                Core.instance.logger.warning("Ignoring invalid/duplicate ambient source '${source.name}' in $PATH")
            }
        }
        tick = 0
        sources.values.filter { it.looping }.forEach { nextPlay[it.name] = 1 }
        task = Bukkit.getScheduler().runTaskTimer(Core.instance, Runnable { update() }, 1L, 1L)
    }

    fun stop() {
        task?.cancel()
        task = null
        sources.values.forEach { silence(it) }
        listeners.clear()
        nextPlay.clear()
    }

    fun put(source: AmbientSource) {
        require(valid(source))
        val updated = sources.toMutableMap().apply { put(source.name, source) }
        // Save before committing runtime changes, so failed writes cannot report success.
        ObjectMapperFileUtils.saveList(PATH, updated.values.toList(), "sources", ConfigFormat.YAML)
        sources[source.name]?.let { silence(it) }
        sources[source.name] = source
        nextPlay.remove(source.name)
        if (source.looping) nextPlay[source.name] = tick + source.intervalTicks
    }

    fun remove(name: String) {
        val source = sources.getValue(name)
        ObjectMapperFileUtils.saveList(PATH, sources.values.filter { it.name != name }, "sources", ConfigFormat.YAML)
        silence(source)
        sources.remove(name)
        nextPlay.remove(name)
    }

    fun play(source: AmbientSource) {
        val center = center(source) ?: return
        val recipients = listeners.getOrPut(source.name) { mutableSetOf() }
        center.world.players.filter { inside(it, center, source.radius) }.forEach { player ->
            // Vanilla attenuation is 16 * volume. Explicit filtering enforces the configured sphere.
            player.playSound(
                center,
                source.sound,
                SoundCategory.AMBIENT,
                maxOf(1.0, source.radius / 16.0).toFloat(),
                1f
            )
            recipients.add(player.uniqueId)
        }
    }

    private fun update() {
        tick++
        for (source in sources.values) {
            val center = center(source)
            val recipients = listeners[source.name]
            recipients?.toList()?.forEach { uuid ->
                val player = Bukkit.getPlayer(uuid)
                if (player == null || center == null || !inside(player, center, source.radius)) {
                    recipients.remove(uuid)
                    player?.stopSound(source.sound, SoundCategory.AMBIENT)
                }
            }
            val due = nextPlay[source.name]
            if (due != null && tick >= due) {
                play(source)
                nextPlay[source.name] = tick + source.intervalTicks
            }
            if (tick % 20L == 0L && center != null && source.particle != null) {
                particles(source, center)
            }
        }
    }

    private fun silence(source: AmbientSource) {
        listeners.remove(source.name)?.forEach { uuid ->
            Bukkit.getPlayer(uuid)?.stopSound(source.sound, SoundCategory.AMBIENT)
        }
    }

    private fun center(source: AmbientSource): Location? = Bukkit.getWorld(source.world)?.let {
        Location(it, source.x, source.y, source.z)
    }

    private fun inside(player: Player, center: Location, radius: Double): Boolean =
        player.world == center.world && player.location.distanceSquared(center) <= radius * radius

    fun particle(input: String): Particle? = Particle.entries.firstOrNull {
        it.key.toString() == input.lowercase() || it.key.key == input.lowercase() || it.name.equals(input, true)
    }

    private fun valid(source: AmbientSource): Boolean =
        source.name.matches(Regex("[a-zA-Z0-9_-]{1,64}")) && source.world.isNotBlank() &&
                source.x.isFinite() && source.y.isFinite() && source.z.isFinite() &&
                NamespacedKey.fromString(source.sound) != null && source.radius.isFinite() &&
                source.radius > 0 && source.radius <= 512 && source.intervalTicks in 1..Long.MAX_VALUE / 2 &&
                (source.particle == null || particle(source.particle) != null)

    private fun particles(source: AmbientSource, center: Location) {
        val type = particle(source.particle ?: return) ?: return
        val viewers = center.world.players.filter { inside(it, center, source.radius) }
        if (viewers.isEmpty()) return
        // Uniform sampling inside the sphere; bounded particle count independent of its volume.
        repeat(8) {
            var dx: Double
            var dy: Double
            var dz: Double
            var length: Double
            do {
                dx = Random.nextDouble(-1.0, 1.0)
                dy = Random.nextDouble(-1.0, 1.0)
                dz = Random.nextDouble(-1.0, 1.0)
                length = dx * dx + dy * dy + dz * dz
            } while (length > 1.0 || length == 0.0)
            val point = center.clone().add(dx * source.radius, dy * source.radius, dz * source.radius)
            val data: Any? = when (type.dataType) {
                Void::class.java -> null
                Color::class.java -> Color.WHITE
                Particle.DustOptions::class.java -> Particle.DustOptions(Color.WHITE, 1f)
                Particle.DustTransition::class.java -> Particle.DustTransition(Color.WHITE, Color.AQUA, 1f)
                org.bukkit.block.data.BlockData::class.java -> Material.STONE.createBlockData()
                ItemStack::class.java -> ItemStack(Material.AMETHYST_SHARD)
                Float::class.javaObjectType -> 1f
                Int::class.javaObjectType -> 0
                Vibration::class.java -> Vibration(Vibration.Destination.BlockDestination(center), 20)
                Particle.Trail::class.java -> Particle.Trail(center, Color.WHITE, 20)
                Particle.Spell::class.java -> Particle.Spell(Color.WHITE, 1f)
                else -> return
            }
            viewers.forEach { it.spawnParticle(type, point, 1, 0.0, 0.0, 0.0, 0.0, data) }
        }
    }
}
