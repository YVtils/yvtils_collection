/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 *
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms.
 * License information: https://yvtils.net/license
 *
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */

package yv.tils.regionsv2.configs

import yv.tils.configv2.data.annotations.ConfigDescription
import yv.tils.configv2.data.annotations.NotGuiEditable

/**
 * regions-v2's `config.yml`, persisted as a plain data class via
 * `ObjectMapperFileUtils` and editable in-game through [yv.tils.gui.logic.DataClassConfigGui]
 * (see [yv.tils.regionsv2.configs.ManageGUI]) - add more fields here as the module grows,
 * annotated with [ConfigDescription] (GUI lore text) and, where relevant, [NotGuiEditable].
 */
data class RegionsV2ConfigState(
    @NotGuiEditable
    @ConfigDescription("Documentation URL")
    val documentation: String = "https://docs.yvtils.net/regions-v2/config.yml",

    @ConfigDescription("Whether regions-v2 is enabled")
    var enabled: Boolean = true,
    @ConfigDescription("Maximum claims owned per world")
    var maxClaimsPerWorld: Int = 5,
    @ConfigDescription("Maximum owned claims across all worlds (-1 unlimited)")
    var maxClaimsTotal: Int = 5,
    @ConfigDescription("Minimum horizontal claim area in blocks")
    var minClaimArea: Long = 1,
    @ConfigDescription("Maximum members per claim (-1 unlimited)")
    var maxMembersPerClaim: Int = -1,
    @ConfigDescription("Maximum member claims per player across all worlds (-1 unlimited)")
    var maxMembershipsPerPlayer: Int = -1,
    @ConfigDescription("Show welcome and goodbye action bars")
    var actionBarTransitions: Boolean = true,
    @ConfigDescription("Charge diamonds for claims larger than the free size")
    var currencyEnabled: Boolean = true,
    @ConfigDescription("Free maximum footprint side in chunks (16 blocks each)")
    var freeClaimChunks: Int = 2,
    @ConfigDescription("Diamonds per additional chunk of the longest footprint side (non-negative)")
    var diamondsPerChunk: Int = 1,
    @ConfigDescription("Maximum blocks in a full-height claim")
    var maxClaimVolume: Long = 30000000,
    @ConfigDescription("Maximum horizontal length of each claim side")
    var maxClaimSide: Int = 256,
    @ConfigDescription("Require survival mode to create claims")
    var survivalOnly: Boolean = true,
    @ConfigDescription("Worlds in which claiming is disabled")
    var disabledWorlds: List<String> = emptyList(),
    @NotGuiEditable
    @ConfigDescription("Legacy role-flag list; use the admin flag policy menu")
    var enabledRoleFlags: List<String> = listOf(
        "block-break",
        "block-place",
        "chest-access",
        "use",
        "interact",
        "damage-animals",
        "entry"
    ),
    @NotGuiEditable
    @ConfigDescription("Legacy global-flag list; use the admin flag policy menu")
    var enabledGlobalFlags: List<String> = listOf(
        "pvp",
        "tnt",
        "creeper-explosion",
        "other-explosion",
        "fire-spread",
        "mob-spawning"
    ),
    @NotGuiEditable
    @ConfigDescription("Per-flag policy; edit through the admin flags menu")
    var flagPolicies: Map<String, FlagPolicy> = emptyMap(),
    @NotGuiEditable
    var policyRevision: Int = 0,
    @NotGuiEditable
    var policyChanges: Map<String, List<String>> = emptyMap(),
    @NotGuiEditable
    var appliedWorldRevisions: Map<String, Int> = emptyMap(),
)

data class FlagPolicy(
    var enabled: Boolean = false,
    var roleBased: Boolean = false,
    /** WorldGuard-marshalled values encoded as YAML; empty means unset. */
    var defaults: Map<String, String> = emptyMap(),
)
