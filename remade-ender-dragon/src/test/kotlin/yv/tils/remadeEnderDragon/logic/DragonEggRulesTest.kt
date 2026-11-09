/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.remadeEnderDragon.logic

import org.bukkit.Material
import org.bukkit.block.Container
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.inventory.meta.BundleMeta
import org.bukkit.inventory.meta.ItemMeta
import yv.tils.remadeEnderDragon.configs.DragonEggSettings
import yv.tils.remadeEnderDragon.configs.DragonConfig
import java.lang.reflect.Proxy
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DragonEggRulesTest {
    @Test
    fun `defaults retain vanilla egg behavior`() {
        val settings = DragonEggSettings()
        assertTrue(settings.enderChestPuttable)
        assertTrue(settings.chestPuttable)
        assertTrue(settings.voidDisappearing)
        assertTrue(settings.logoutable)
        assertFalse(settings.alwaysDropOnDeath)
        assertFalse(DragonConfig().keepInventoryDuringFight)
    }

    @Test
    fun `ender chest and ordinary storage toggles are independent`() {
        val settings = DragonEggSettings(enderChestPuttable = false)
        assertFalse(DragonEggRules.allowsStorage(settings, enderChest = true))
        assertTrue(DragonEggRules.allowsStorage(settings, enderChest = false))
        settings.enderChestPuttable = true
        settings.chestPuttable = false
        assertTrue(DragonEggRules.allowsStorage(settings, enderChest = true))
        assertFalse(DragonEggRules.allowsStorage(settings, enderChest = false))
    }

    @Test
    fun `editing a detached egg draft does not mutate live settings`() {
        val live = DragonEggSettings()
        val draft = live.copy()
        draft.enderChestPuttable = false
        draft.chestPuttable = false
        draft.voidDisappearing = false
        draft.logoutable = false
        draft.alwaysDropOnDeath = true
        assertTrue(live.enderChestPuttable && live.chestPuttable && live.voidDisappearing && live.logoutable)
        assertFalse(live.alwaysDropOnDeath)
    }

    @Test
    fun `only nonempty egg stacks match`() {
        assertTrue(DragonEggRules.containsEgg(EggTestStack(Material.DRAGON_EGG)))
        assertFalse(DragonEggRules.containsEgg(null))
        assertFalse(DragonEggRules.containsEgg(EggTestStack(Material.AIR)))
        assertFalse(DragonEggRules.containsEgg(EggTestStack(Material.STONE)))
        assertFalse(DragonEggRules.containsEgg(EggTestStack(Material.DRAGON_EGG, count = 0)))
    }

    @Test
    fun `eggs inside nested bundles and shulker boxes match without modifying contents`() {
        val egg = EggTestStack(Material.DRAGON_EGG)
        val bundle = bundle(egg)
        val shulker = shulker(bundle)
        assertTrue(DragonEggRules.containsEgg(bundle))
        assertTrue(DragonEggRules.containsEgg(shulker))
        assertTrue(DragonEggRules.containsEgg(bundle(shulker)))
        assertFalse(DragonEggRules.containsEgg(bundle(EggTestStack(Material.STONE))))
        assertFalse(DragonEggRules.containsEgg(shulker(null)))
    }

    @Test
    fun `normal cursor placement swaps and bundle insertion are rejected only in top inventory`() {
        val actions = listOf(InventoryAction.PLACE_ALL, InventoryAction.PLACE_ONE, InventoryAction.PLACE_SOME,
            InventoryAction.SWAP_WITH_CURSOR, InventoryAction.PLACE_FROM_BUNDLE,
            InventoryAction.PLACE_ALL_INTO_BUNDLE, InventoryAction.PLACE_SOME_INTO_BUNDLE)
        actions.forEach { action ->
            assertTrue(inserts(action, top = true, cursor = true), action.name)
            assertFalse(inserts(action, top = false, cursor = true), action.name)
            assertFalse(inserts(action, top = true, current = true), action.name)
        }
    }

    @Test
    fun `shift click rejects insertion but permits retrieval`() {
        assertTrue(inserts(InventoryAction.MOVE_TO_OTHER_INVENTORY, top = false, current = true))
        assertFalse(inserts(InventoryAction.MOVE_TO_OTHER_INVENTORY, top = true, current = true))
        assertFalse(inserts(InventoryAction.MOVE_TO_OTHER_INVENTORY, top = false))
    }

    @Test
    fun `hotbar and offhand swaps inspect incoming stack rather than clicked stack`() {
        assertTrue(inserts(InventoryAction.HOTBAR_SWAP, top = true, hotbar = true))
        assertFalse(inserts(InventoryAction.HOTBAR_SWAP, top = false, hotbar = true))
        assertFalse(inserts(InventoryAction.HOTBAR_SWAP, top = true, current = true))
    }

    @Test
    fun `pickup drop collection and bundle extraction never insert eggs into storage`() {
        val actions = listOf(InventoryAction.PICKUP_ALL, InventoryAction.PICKUP_ONE, InventoryAction.PICKUP_HALF,
            InventoryAction.PICKUP_SOME, InventoryAction.COLLECT_TO_CURSOR, InventoryAction.DROP_ALL_SLOT,
            InventoryAction.DROP_ONE_SLOT, InventoryAction.DROP_ALL_CURSOR, InventoryAction.DROP_ONE_CURSOR,
            InventoryAction.CLONE_STACK, InventoryAction.NOTHING, InventoryAction.PICKUP_FROM_BUNDLE,
            InventoryAction.PICKUP_ALL_INTO_BUNDLE, InventoryAction.PICKUP_SOME_INTO_BUNDLE)
        actions.forEach { action ->
            assertFalse(inserts(action, top = true, cursor = true, current = true, hotbar = true), action.name)
        }
    }

    private fun inserts(action: InventoryAction, top: Boolean, cursor: Boolean = false,
                        current: Boolean = false, hotbar: Boolean = false) =
        DragonEggRules.insertsEgg(action, top, cursor, current, hotbar)

    private fun bundle(vararg contents: ItemStack): ItemStack {
        val meta = eggStub<BundleMeta> { name, _ ->
            when (name) {
                "getItems" -> contents.toList()
                else -> error("Unexpected bundle call: $name")
            }
        }
        return EggTestStack(Material.BUNDLE, meta)
    }

    private fun shulker(vararg contents: ItemStack?): ItemStack {
        val inventory = eggStub<Inventory> { name, _ ->
            when (name) {
                "getContents" -> contents
                else -> error("Unexpected inventory call: $name")
            }
        }
        val state = eggStub<Container> { name, _ ->
            when (name) {
                "getInventory" -> inventory
                else -> error("Unexpected container call: $name")
            }
        }
        val meta = eggStub<BlockStateMeta> { name, _ ->
            when (name) {
                "hasBlockState" -> true
                "getBlockState" -> state
                else -> error("Unexpected block meta call: $name")
            }
        }
        return EggTestStack(Material.SHULKER_BOX, meta)
    }
}

/** Use the API's protected constructor: normal ItemStacks require a running server. */
internal class EggTestStack(
    private val material: Material,
    private val meta: ItemMeta? = null,
    private var count: Int = 1,
) : ItemStack() {
    override fun getType() = material
    override fun getAmount() = count
    override fun setAmount(amount: Int) { count = amount }
    override fun hasItemMeta() = meta != null
    override fun getItemMeta() = meta
    override fun clone() = EggTestStack(material, meta, count)
    override fun isSimilar(stack: ItemStack?): Boolean =
        stack is EggTestStack && material == stack.material && meta === stack.meta
}

internal inline fun <reified T> eggStub(crossinline call: (String, Array<out Any?>) -> Any?): T =
    Proxy.newProxyInstance(T::class.java.classLoader, arrayOf(T::class.java)) { proxy, method, args ->
        when (method.name) {
            "equals" -> proxy === args?.get(0)
            "hashCode" -> System.identityHashCode(proxy)
            "toString" -> "EggTestStub(${T::class.java.simpleName})"
            else -> call(method.name, args ?: emptyArray())
        }
    } as T
