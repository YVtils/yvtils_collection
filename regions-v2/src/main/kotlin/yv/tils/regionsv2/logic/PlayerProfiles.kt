package yv.tils.regionsv2.logic

import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import yv.tils.regionsv2.language.RegionText
import yv.tils.regionsv2.language.LangStrings.*
import yv.tils.regionsv2.language.message
import yv.tils.utils.modules.Core
import java.util.UUID
import java.util.concurrent.TimeUnit

object PlayerProfiles {
    fun resolve(sender: CommandSender, input: String, callback: (UUID) -> Unit) {
        runCatching { UUID.fromString(input) }.getOrNull()?.let { callback(it); return }
        Bukkit.getOfflinePlayerIfCached(input)?.let { callback(it.uniqueId); return }
        check(input.matches(Regex("[A-Za-z0-9_]{1,16}"))) { PROFILE_FAILED.key }
        RegionText.send(sender, RESOLVING.message())
        Bukkit.createProfile(input).update().orTimeout(15, TimeUnit.SECONDS).whenComplete { profile, error ->
            if (!Core.instance.isEnabled) return@whenComplete
            Bukkit.getScheduler().runTask(Core.instance, Runnable {
                if (sender is Player && !sender.isOnline) return@Runnable
                RegionText.action(sender) {
                    check(error == null && profile?.id != null) { PROFILE_FAILED.key }
                    callback(profile.id!!)
                }
            })
        }
    }
}
