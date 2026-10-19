package yv.tils.essentials.inventory

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.security.MessageDigest
import java.util.UUID
import java.io.File
import java.io.InputStream
import java.io.OutputStream

/** Reflection is restricted to this adapter: dynamic library classes cannot link CraftBukkit/NMS. */
object OfflineInventoryStorage {
    class Loaded(val items: Array<ItemStack?>, val fingerprint: ByteArray)

    private val loader get() = Bukkit.getServer().javaClass.classLoader
    private fun type(name: String) = Class.forName("net.minecraft.$name", true, loader)
    private fun call(receiver: Any, name: String, vararg arguments: Any): Any? {
        val method = receiver.javaClass.methods.first { method ->
            method.name == name && method.parameterCount == arguments.size &&
                    method.parameterTypes.zip(arguments).all { (parameter, value) ->
                        parameter.isInstance(value) || parameter == Int::class.javaPrimitiveType && value is Int ||
                                parameter == Byte::class.javaPrimitiveType && value is Byte
                    }
        }
        return method.invoke(receiver, *arguments)
    }

    private fun file(uuid: UUID): Path {
        val server = Bukkit.getServer().javaClass.getMethod("getServer").invoke(Bukkit.getServer())
        val storage = server.javaClass.getField("playerDataStorage").get(server)
        val directory = call(storage, "getPlayerDir") as File
        return directory.toPath().resolve("$uuid.dat")
    }

    private fun read(bytes: ByteArray): Any {
        val accounter = type("nbt.NbtAccounter").getMethod("create", Long::class.javaPrimitiveType)
            .invoke(null, 64L * 1024 * 1024)
        return type("nbt.NbtIo").getMethod("readCompressed", InputStream::class.java, accounter.javaClass)
            .invoke(null, ByteArrayInputStream(bytes), accounter)
    }

    private fun bytes(tag: Any): ByteArray = ByteArrayOutputStream().use { output ->
        type("nbt.NbtIo").getMethod("writeCompressed", type("nbt.CompoundTag"), OutputStream::class.java)
            .invoke(null, tag, output)
        output.toByteArray()
    }

    private fun hash(bytes: ByteArray) = MessageDigest.getInstance("SHA-256").digest(bytes)
    private val serverDataVersion: Int by lazy {
        val tag = read(ItemStack(Material.STONE).serializeAsBytes())
        val version = call(tag, "getIntOr", "DataVersion", -1) as Int
        check(version >= 0) { "Serialized item is missing its data version." }
        version
    }
    private val equipment = mapOf(36 to "feet", 37 to "legs", 38 to "chest", 39 to "head", 40 to "offhand")

    fun load(uuid: UUID, kind: InventoryKind): Loaded {
        check(Bukkit.isPrimaryThread())
        check(Bukkit.getPlayer(uuid) == null) { "Target is online; reopen the editor." }
        val data = Files.readAllBytes(file(uuid))
        val root = read(data)
        val version = call(root, "getIntOr", "DataVersion", -1) as Int
        require(version == serverDataVersion) { "Offline player data must be saved by this server version first." }
        val items = arrayOfNulls<ItemStack>(kind.slots)
        val key = if (kind == InventoryKind.PLAYER) "Inventory" else "EnderItems"
        val rawList = call(root, "get", key)
        require(rawList == null || type("nbt.ListTag").isInstance(rawList)) { "Malformed $key list" }
        val list = call(root, "getListOrEmpty", key) as List<*>
        list.forEach { entry ->
            require(entry != null && type("nbt.CompoundTag").isInstance(entry)) { "Malformed inventory entry" }
            val slot = ((call(entry, "getByteOr", "Slot", 0.toByte()) as? Byte)?.toInt() ?: 0) and 255
            require(slot in 0 until minOf(36, items.size)) { "Unsupported inventory slot $slot" }
            require(items[slot] == null) { "Duplicate inventory slot $slot" }
            items[slot] = decode(entry, version)
        }
        if (kind == InventoryKind.PLAYER) {
            val rawGear = call(root, "get", "equipment")
            require(rawGear == null || type("nbt.CompoundTag").isInstance(rawGear)) { "Malformed equipment compound" }
            val gear = call(root, "getCompoundOrEmpty", "equipment")!!
            equipment.forEach { (slot, name) ->
                val entry = call(gear, "get", name)
                if (entry != null) items[slot] = decode(entry, version)
            }
        }
        return Loaded(items, hash(data))
    }

    private fun decode(tag: Any, version: Int): ItemStack {
        require(type("nbt.CompoundTag").isInstance(tag)) { "Malformed item compound" }
        require((call(tag, "getStringOr", "id", "") as String).isNotBlank()) { "Missing item identifier" }
        val copy = call(tag, "copy")!!
        call(copy, "remove", "Slot")
        call(copy, "putInt", "DataVersion", version)
        return ItemStack.deserializeBytes(bytes(copy))
    }

    fun save(session: InventorySession, items: Array<ItemStack?>) {
        check(Bukkit.isPrimaryThread())
        check(Bukkit.getPlayer(session.target) == null) { "Target joined; reopen the editor." }
        val target = file(session.target)
        val current = Files.readAllBytes(target)
        check(hash(current).contentEquals(session.fingerprint)) { "Player data changed externally; reopen the editor." }
        val root = read(current)
        val list = type("nbt.ListTag").getConstructor().newInstance() as MutableList<Any>
        items.take(if (session.kind == InventoryKind.PLAYER) 36 else 27).forEachIndexed { slot, item ->
            if (item != null && !item.type.isAir) {
                val tag = read(item.serializeAsBytes())
                call(tag, "remove", "DataVersion")
                call(tag, "putByte", "Slot", slot.toByte())
                list.add(tag)
            }
        }
        call(root, "put", if (session.kind == InventoryKind.PLAYER) "Inventory" else "EnderItems", list)
        if (session.kind == InventoryKind.PLAYER) {
            val gear = call(root, "getCompoundOrEmpty", "equipment")!!
            equipment.forEach { (slot, name) ->
                val item = items[slot]
                if (item == null || item.type.isAir) call(gear, "remove", name)
                else {
                    val tag = read(item.serializeAsBytes())
                    call(tag, "remove", "DataVersion")
                    call(gear, "put", name, tag)
                }
            }
            call(root, "put", "equipment", gear)
        }
        val encoded = bytes(root)
        val temporary = Files.createTempFile(target.parent, ".yvtils-inventory-", ".dat")
        try {
            Files.write(temporary, encoded)
            Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } finally {
            Files.deleteIfExists(temporary)
        }
        session.fingerprint = hash(encoded)
    }
}
