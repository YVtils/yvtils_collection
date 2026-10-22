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
import kotlin.random.Random
import org.bukkit.block.data.BlockData
import yv.tils.yv_smp.configs.ConfigFile
import yv.tils.yv_smp.logic.music.LocationalAudioStream
import yv.tils.yv_smp.logic.music.SimpleVoiceChat
import yv.tils.yv_smp.logic.music.svc.VoicechatBridge
import java.io.File
import java.net.URI
import java.util.logging.Level

/** All access, including commands, runs on the server thread. No entities or chunks are retained. */
object AmbientSounds {
    private const val PATH = "/yv_smp/ambient-sources.yml"
    private val sources = linkedMapOf<String, AmbientSource>()
    private val playback = mutableMapOf<String, AmbientPlayback>()
    private val streams = mutableMapOf<String, LocationalAudioStream>()
    private val audioRequested = mutableSetOf<String>()
    private val audioAttempted = mutableSetOf<String>()
    private var streamApi: Any? = null
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
                if (source.looping && source.audio != null) audioRequested.add(source.name)
            } else {
                Core.instance.logger.warning("Ignoring invalid/duplicate ambient source '${source.name}' in $PATH")
            }
        }
        tick = 0
        task = Bukkit.getScheduler().runTaskTimer(Core.instance, Runnable { update() }, 1L, 1L)
    }

    fun stop() {
        task?.cancel()
        task = null
        sources.values.forEach { silence(it) }
        playback.clear()
        audioRequested.clear()
        audioAttempted.clear()
        streamApi = null
    }

    fun put(source: AmbientSource) {
        require(valid(source))
        val updated = sources.toMutableMap().apply { put(source.name, source) }
        // Save before committing runtime changes, so failed writes cannot report success.
        ObjectMapperFileUtils.saveList(PATH, updated.values.toList(), "sources", ConfigFormat.YAML)
        sources[source.name]?.let { silence(it) }
        sources[source.name] = source
        if (source.looping && source.audio != null) audioRequested.add(source.name)
    }

    fun remove(name: String) {
        val source = sources.getValue(name)
        ObjectMapperFileUtils.saveList(PATH, sources.values.filter { it.name != name }, "sources", ConfigFormat.YAML)
        silence(source)
        sources.remove(name)
    }

    fun play(source: AmbientSource) {
        if (source.audio != null) {
            require(SimpleVoiceChat.serverAPI != null) { "svc" }
            refreshStreamApi()
            silence(source)
            audioRequested.add(source.name)
            startAudio(source)
            return
        }
        val center = center(source) ?: return
        val recipients = center.world.players.filter { inside(it, center, source.radius) }
        recipients.forEach { playFor(it, source, center) }
        playback.getOrPut(source.name) { AmbientPlayback() }
            .record(tick, recipients.mapTo(mutableSetOf()) { it.uniqueId }, source.intervalTicks)
    }

    private fun playFor(player: Player, source: AmbientSource, center: Location) {
        // A repeat replaces the previous instance rather than stacking long sounds.
        player.stopSound(source.sound, SoundCategory.AMBIENT)
        // Vanilla attenuation is 16 * volume. Explicit filtering enforces the configured sphere.
        player.playSound(
            center,
            source.sound,
            SoundCategory.AMBIENT,
            maxOf(1.0, source.radius / 16.0).toFloat(),
            1f
        )
    }

    private fun update() {
        tick++
        refreshStreamApi()
        for (source in sources.values) {
            val center = center(source)
            if (source.audio != null) {
                if (center == null) {
                    streams.remove(source.name)?.close()
                    audioAttempted.remove(source.name)
                } else {
                    if (source.name in audioRequested && source.name !in audioAttempted) startAudio(source)
                    if (tick % 20L == 0L && source.particle != null) particles(source, center)
                }
                continue
            }
            val eligible = center?.world?.players?.filter { inside(it, center, source.radius) }.orEmpty()
                .associateBy { it.uniqueId }
            val changes = playback.getOrPut(source.name) { AmbientPlayback() }
                .update(tick, eligible.keys, source.looping, source.intervalTicks)
            changes.stop.forEach { uuid ->
                Bukkit.getPlayer(uuid)?.stopSound(source.sound, SoundCategory.AMBIENT)
            }
            if (center != null) {
                changes.play.forEach { uuid -> playFor(eligible.getValue(uuid), source, center) }
            }
            if (tick % 20L == 0L && center != null && source.particle != null) {
                particles(source, center)
            }
        }
    }

    private fun silence(source: AmbientSource) {
        streams.remove(source.name)?.close()
        audioRequested.remove(source.name)
        audioAttempted.remove(source.name)
        playback.remove(source.name)?.clear()?.forEach { uuid ->
            Bukkit.getPlayer(uuid)?.stopSound(source.sound, SoundCategory.AMBIENT)
        }
    }

    private fun refreshStreamApi() {
        val api = SimpleVoiceChat.serverAPI
        if (streamApi !== api) {
            streams.values.forEach { it.close() }
            streams.clear()
            audioAttempted.clear()
            streamApi = api
        }
    }

    private fun startAudio(source: AmbientSource) {
        val api = SimpleVoiceChat.serverAPI ?: return
        val center = center(source) ?: return
        val identifier = source.audio ?: return
        audioAttempted.add(source.name)
        try {
            val channel = VoicechatBridge.createLocationalAudioChannel(
                api, center.world, source.x, source.y, source.z, source.radius.toFloat()
            )
            VoicechatBridge.setCategory(channel, SimpleVoiceChat.AUDIO_CATEGORY)
            val stream = LocationalAudioStream(source.name, channel, source.looping, ConfigFile.getMasterVolume())
            streams[source.name] = stream
            stream.load(audioIdentifier(identifier))
            if (!source.looping) audioRequested.remove(source.name)
        } catch (exception: Exception) {
            streams.remove(source.name)?.close()
            Core.instance.logger.log(Level.WARNING, "Could not start ambient audio '${source.name}'", exception)
        }
    }

    /** File paths are relative to the server working directory; URLs must be HTTP(S). */
    fun audioIdentifier(input: String): String {
        require(input.isNotBlank()) { "audio" }
        if (input.startsWith("http://", true) || input.startsWith("https://", true)) {
            val uri = runCatching { URI(input) }.getOrNull()
            require(uri != null && !uri.host.isNullOrBlank()) { "audio" }
            return input
        }
        val file = File(input)
        require(file.isFile && file.canRead()) { "audio" }
        return file.absolutePath
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
                (source.audio?.isNotBlank() ?: (NamespacedKey.fromString(source.sound) != null)) && source.radius.isFinite() &&
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
                BlockData::class.java -> Material.STONE.createBlockData()
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
