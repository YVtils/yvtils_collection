# InvUI and high Survival anvil costs

## Investigation

The collection uses InvUI 2.1.1 / 2.3.2 / 2.5.1 through its versioned GUI libraries.
InvUI exposes `AnvilWindow.setEnchantmentCost(int)`. Its `AnvilWindowImpl` delegates
directly to `CustomAnvilMenu.setEnchantmentCost`, which assigns `dataSlots[0]`.
That is the ordinary client-visible anvil cost property; it is not a separate
numeric-label API. `setResultAlwaysValid` changes result-slot presentation and
`setTextFieldAlwaysEnabled` enables rename input; neither controls cost formatting.

Relevant upstream sources, also inspected in the resolved InvUI 2.1.1 source JAR:

- [AnvilWindow](https://github.com/NichtStudioCode/InvUI/blob/main/invui/src/main/java/xyz/xenondevs/invui/window/AnvilWindow.java)
- [AnvilWindowImpl](https://github.com/NichtStudioCode/InvUI/blob/main/invui/src/main/java/xyz/xenondevs/invui/window/AnvilWindowImpl.java)
- [CustomAnvilMenu](https://github.com/NichtStudioCode/InvUI/blob/main/invui/src/main/java/xyz/xenondevs/invui/internal/menu/CustomAnvilMenu.java)

The vanilla client decides how that property is rendered. The familiar rule is
`cost >= 40 && !player.getAbilities().instabuild`, which renders the translated
“Too Expensive!” label rather than the number. InvUI still opens that vanilla
screen and sends the same property. It cannot by itself replace that label while
keeping the actual Survival cost and Survival client abilities.

## Decision

Keep the native anvil and the existing `AntiTooExpensive` implementation:
raise Paper's server-side maximum and report the real level cost through localized
`essentials.anvil.cost` feedback when `showAnvilCost` is enabled. Do not fake a cost
below 40, spoof Creative abilities, or substitute an InvUI rename-input window for
Minecraft's repair/enchantment calculation.

A separate custom GUI could show the amount in its title/lore, but would need a
complete inventory/XP/rename/enchantment transaction implementation. It would not
make the vanilla cost label display the true number. A client modification can
change rendering; that is outside the server-only module contract.

This is a source/API investigation, not a claim of a live client compatibility
test. Verify native cost/payment behavior on each supported Paper/client version.
