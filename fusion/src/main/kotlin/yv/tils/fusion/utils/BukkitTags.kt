/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.utils

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.Tag

object BukkitTags {
    fun block(key: String): Tag<Material>? = NamespacedKey.fromString(key)?.let {
        Bukkit.getTag(Tag.REGISTRY_BLOCKS, it, Material::class.java)
    }
}
