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

import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ObjectMapperFileUtils
import yv.tils.regionsv2.logic.ClaimFlags
import org.bukkit.Bukkit
import yv.tils.regionsv2.language.LangStrings

class ConfigFile {
    companion object {
        /** The single source of truth. */
        var state: RegionsV2ConfigState = RegionsV2ConfigState()

        /**
         * Flattened `"a.b.c" -> value` view derived from [state], re-synced on every
         * [loadConfig] call - kept around purely so `ConfigFile.config["key"]`-style call
         * sites work without needing the full data class shape.
         */
        val config: MutableMap<String, Any> = mutableMapOf()

        private fun syncDerivedView() {
            config.clear()
            config.putAll(ObjectMapperFileUtils.flatten(state, ConfigFormat.YAML))
        }
    }

    private val filePath = "/regions-v2/config.yml"

    fun loadConfig() {
        state = ObjectMapperFileUtils.load(filePath, RegionsV2ConfigState(), format = ConfigFormat.YAML)
        registerStrings()
        syncDerivedView()
    }

    fun registerStrings() {
        ObjectMapperFileUtils.save(filePath, state, format = ConfigFormat.YAML)
    }

    /** Called by [yv.tils.gui.logic.DataClassConfigGui]'s saver after an in-game edit. */
    fun applyState(newState: RegionsV2ConfigState) {
        check(
            listOf(
                newState.maxClaimsPerWorld,
                newState.maxClaimsTotal,
                newState.maxMembersPerClaim,
                newState.maxMembershipsPerPlayer,
                newState.maxClaimSide, newState.maxSubzonesPerClaim
            ).all { it == -1 || it >= 0 } &&
                    (newState.maxClaimVolume == -1L || newState.maxClaimVolume >= 1) && newState.minClaimArea >= 1 &&
                    newState.freeClaimChunks >= 0 && newState.diamondsPerChunk >= 0 && newState.clusterDistanceBlocks >= 0) {
            LangStrings.INVALID_LIMITS.key
        }
        val previous = state
        val changed = ClaimFlags.changed(previous, newState)
        val revision = if (changed.isEmpty()) previous.policyRevision else previous.policyRevision + 1
        val updated = newState.copy(
            policyRevision = revision,
            policyChanges = if (changed.isEmpty()) previous.policyChanges else previous.policyChanges + (revision.toString() to changed.map { it.name }),
            appliedWorldRevisions = if (changed.isEmpty()) newState.appliedWorldRevisions else previous.appliedWorldRevisions +
                    Bukkit.getWorlds().associate { it.uid.toString() to revision }
        )
        val rollback = ClaimFlags.propagate(previous, updated)
        state = updated
        try {
            registerStrings()
            syncDerivedView()
        } catch (e: Exception) {
            state = previous
            try {
                rollback()
            } catch (restore: Exception) {
                e.addSuppressed(restore)
            }
            syncDerivedView()
            throw IllegalStateException(LangStrings.CONFIG_SAVE_FAILED.key, e)
        }
    }
}
