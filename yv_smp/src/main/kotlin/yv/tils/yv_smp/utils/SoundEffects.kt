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

package yv.tils.yv_smp.utils

import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player

class SoundEffects {
    companion object {
        fun dramatic(player: Player, sound: Sound, pitch: Float = 1.0f) {
            player.playSound(player.location, sound, 1.0f, pitch)
            player.world.spawnParticle(Particle.EXPLOSION, player.location, 3, 0.5, 0.5, 0.5, 0.0)
        }
    }
}