/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 */
package yv.tils.yv_smp.commands

import dev.jorel.commandapi.arguments.Argument
import dev.jorel.commandapi.arguments.ArgumentSuggestions
import dev.jorel.commandapi.kotlindsl.*
import org.bukkit.NamespacedKey
import org.bukkit.Particle
import org.bukkit.Registry
import org.bukkit.command.CommandSender
import yv.tils.config.language.LanguageHandler
import yv.tils.yv_smp.language.LangStrings
import yv.tils.yv_smp.logic.ambient.AmbientInterval
import yv.tils.yv_smp.logic.ambient.AmbientSounds
import yv.tils.yv_smp.logic.ambient.AmbientSource
import yv.tils.yv_smp.permissions.Permissions
import java.util.logging.Level
import yv.tils.utils.modules.Core

object AmbientCommand {
    fun register(root: Argument<*>) = root.apply {
        withPermission(Permissions.COMMAND_AMBIENT.permission.name)
        anyExecutor { sender, _ -> message(sender, LangStrings.AMBIENT_HELP) }
        literalArgument("create") {
            stringArgument("name") {
                namespacedKeyArgument("sound") {
                    replaceSuggestions(ArgumentSuggestions.strings {
                        Registry.SOUND_EVENT.map { Registry.SOUND_EVENT.getKeyOrThrow(it).toString() }.toTypedArray()
                    })
                    doubleArgument("radius", min = 0.1, max = 512.0) {
                        booleanArgument("looping") {
                            stringArgument("interval") {
                                replaceSuggestions(ArgumentSuggestions.strings("40t", "10s", "5m", "1h"))
                                playerExecutor { player, args ->
                                    action(player) {
                                        val name = args["name"] as String
                                        require(name.matches(Regex("[a-zA-Z0-9_-]{1,64}"))) { "name" }
                                        require(AmbientSounds.get(name) == null) { "duplicate" }
                                        val location = player.location
                                        val source = AmbientSource(
                                            name, player.world.name, location.x, location.y, location.z,
                                            (args["sound"] as NamespacedKey).toString(), args["radius"] as Double,
                                            args["looping"] as Boolean, interval(args["interval"] as String),
                                        )
                                        AmbientSounds.put(source)
                                        AmbientSounds.play(source)
                                        message(player, LangStrings.AMBIENT_SAVED, "name" to name)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        literalArgument("list") {
            anyExecutor { sender, _ ->
                message(sender, LangStrings.AMBIENT_LIST, "sources" to AmbientSounds.names().joinToString(", "))
            }
        }
        literalArgument("info") {
            stringArgument("name") {
                replaceSuggestions(ArgumentSuggestions.strings { AmbientSounds.names() })
                anyExecutor { sender, args ->
                    action(sender) {
                        val source = source(args["name"] as String)
                        message(
                            sender, LangStrings.AMBIENT_INFO,
                            "name" to source.name, "world" to source.world,
                            "x" to source.x, "y" to source.y, "z" to source.z,
                            "sound" to source.sound, "radius" to source.radius,
                            "looping" to source.looping, "interval" to source.intervalTicks,
                            "particle" to (source.particle ?: "none")
                        )
                    }
                }
            }
        }
        for (operation in listOf("remove", "play", "move")) {
            literalArgument(operation) {
                stringArgument("name") {
                    replaceSuggestions(ArgumentSuggestions.strings { AmbientSounds.names() })
                    if (operation == "move") {
                        playerExecutor { player, args ->
                            action(player) {
                                val source = source(args["name"] as String)
                                val location = player.location
                                AmbientSounds.put(
                                    source.copy(
                                        world = player.world.name,
                                        x = location.x,
                                        y = location.y,
                                        z = location.z
                                    )
                                )
                                message(player, LangStrings.AMBIENT_SAVED, "name" to source.name)
                            }
                        }
                    } else {
                        anyExecutor { sender, args ->
                            action(sender) {
                                val source = source(args["name"] as String)
                                if (operation == "remove") AmbientSounds.remove(source.name) else AmbientSounds.play(
                                    source
                                )
                                message(
                                    sender,
                                    if (operation == "remove") LangStrings.AMBIENT_REMOVED else LangStrings.AMBIENT_PLAYED,
                                    "name" to source.name
                                )
                            }
                        }
                    }
                }
            }
        }
        literalArgument("set") {
            stringArgument("name") {
                replaceSuggestions(ArgumentSuggestions.strings { AmbientSounds.names() })
                literalArgument("sound") {
                    namespacedKeyArgument("sound") {
                        replaceSuggestions(ArgumentSuggestions.strings {
                            Registry.SOUND_EVENT.map { Registry.SOUND_EVENT.getKeyOrThrow(it).toString() }.toTypedArray()
                        })
                        anyExecutor { sender, args ->
                            edit(sender, args["name"] as String) {
                                it.copy(sound = (args["sound"] as NamespacedKey).toString())
                            }
                        }
                    }
                }
                literalArgument("radius") {
                    doubleArgument("radius", min = 0.1, max = 512.0) {
                        anyExecutor { sender, args ->
                            edit(sender, args["name"] as String) {
                                it.copy(radius = args["radius"] as Double)
                            }
                        }
                    }
                }
                literalArgument("looping") {
                    booleanArgument("looping") {
                        anyExecutor { sender, args ->
                            edit(sender, args["name"] as String) {
                                it.copy(looping = args["looping"] as Boolean)
                            }
                        }
                    }
                }
                literalArgument("interval") {
                    stringArgument("interval") {
                        replaceSuggestions(ArgumentSuggestions.strings("40t", "10s", "5m", "1h"))
                        anyExecutor { sender, args ->
                            edit(sender, args["name"] as String) {
                                it.copy(intervalTicks = interval(args["interval"] as String))
                            }
                        }
                    }
                }
                literalArgument("particle") {
                    namespacedKeyArgument("particle") {
                        replaceSuggestions(ArgumentSuggestions.strings {
                            arrayOf(
                                "none",
                                *Particle.entries.map { it.key.toString() }.toTypedArray()
                            )
                        })
                        anyExecutor { sender, args ->
                            edit(sender, args["name"] as String) { source ->
                                val input = (args["particle"] as NamespacedKey).toString()
                                source.copy(
                                    particle = if (input == "minecraft:none") null else
                                        (AmbientSounds.particle(input)
                                            ?: throw IllegalArgumentException("particle")).key.toString()
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun source(name: String) = AmbientSounds.get(name) ?: throw IllegalArgumentException("missing")

    private fun interval(input: String): Long = AmbientInterval.parse(input)?.takeIf { it <= Long.MAX_VALUE / 2 }
        ?: throw IllegalArgumentException("interval")

    private fun edit(sender: CommandSender, name: String, transform: (AmbientSource) -> AmbientSource) =
        action(sender) {
            AmbientSounds.put(transform(source(name)))
            message(sender, LangStrings.AMBIENT_SAVED, "name" to name)
        }

    private fun action(sender: CommandSender, block: () -> Unit) {
        try {
            block()
        } catch (exception: IllegalArgumentException) {
            val key = when (exception.message) {
                "name" -> LangStrings.AMBIENT_INVALID_NAME
                "duplicate" -> LangStrings.AMBIENT_DUPLICATE
                "missing" -> LangStrings.AMBIENT_MISSING
                "particle" -> LangStrings.AMBIENT_INVALID_PARTICLE
                "interval" -> LangStrings.AMBIENT_INVALID_INTERVAL
                else -> {
                    failure(sender, exception)
                    return
                }
            }
            message(sender, key)
        } catch (exception: Exception) {
            failure(sender, exception)
        }
    }

    private fun failure(sender: CommandSender, exception: Exception) {
        Core.instance.logger.log(
            Level.SEVERE, "Ambient source operation failed", exception
        )
        message(sender, LangStrings.AMBIENT_SAVE_FAILED)
    }

    private fun message(sender: CommandSender, key: LangStrings, vararg values: Pair<String, Any>) {
        sender.sendMessage(LanguageHandler.getMessage(key, sender, values.toMap()))
    }
}
