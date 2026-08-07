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

package yv.tils.configv2.data.annotations

import org.bukkit.Material

/**
 * GUI-facing metadata for a `data class` field, read reflectively by `gui`'s
 * `DataClassConfigGui` (and ignored entirely by [yv.tils.configv2.files.ObjectMapperFileUtils],
 * which only cares about the field's name/type/value).
 *
 * This is what makes a plain data class usable both as [yv.tils.configv2.files.ObjectMapperFileUtils]
 * persistence *and* as an in-game editable config, without needing a separate
 * `ConfigEntry`-style list kept in sync by hand: the description/icon live directly on the
 * property declaration, right next to its type and default value.
 *
 * Every annotation here targets [AnnotationTarget.VALUE_PARAMETER] (primary constructor
 * parameters) - for a `data class` property declared in the primary constructor, Kotlin
 * resolves an unqualified annotation to the constructor parameter by default when that's an
 * applicable target, so no `@param:` use-site target is needed in practice.
 */

/**
 * Human-readable description shown in the config GUI's item lore.
 *
 * Deliberately a distinct annotation from Configurate's own
 * [org.spongepowered.configurate.objectmapping.meta.Comment] (which additionally requires a
 * `Processor`/`CommentedConfigurationNode` to actually emit YAML comments, and carries
 * options - `override`, localization - unrelated to the GUI) - the two could be unified later
 * if `config.yml` output ever needs real inline comments too.
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigDescription(val value: String)

/**
 * A fixed GUI icon for a field, regardless of its current value.
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class ConfigIcon(val material: Material)

/**
 * A GUI icon for a `Boolean` field that swaps depending on its current (or default, if
 * unset) value - the annotation-based equivalent of [yv.tils.configv2.data.ConfigEntry]'s
 * `dynamicInvItem` lambda, covering every real use of it in this project (all of which just
 * swap between two fixed materials based on a boolean).
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class BooleanIcon(val whenTrue: Material, val whenFalse: Material)

/**
 * Marks a field as not editable through the config GUI (e.g. a `documentation` URL field) -
 * it's still persisted normally, just skipped when the GUI enumerates editable fields.
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class NotGuiEditable

/**
 * Marks a `List<String>` field as holding [Material] names (e.g. multiMine's `blocks` list) -
 * the GUI validates new entries against [Material.valueOf] and uses the named material as
 * each entry's icon. Without this, a `List<String>` field is treated as a plain string list
 * (no validation, generic icon).
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class MaterialNameList

/**
 * A human-readable string representation of a field's default value, shown as a separate
 * "default: X" lore line in the config GUI (alongside the field's current, possibly-edited
 * value).
 *
 * This exists only for GUI display - it's a redundant, hand-maintained copy of whatever
 * value the property's own Kotlin default already is (Kotlin reflection can't read a
 * constructor parameter's actual default *value*, only whether it has one), so keep it in
 * sync with the real default by hand. Fields without this annotation simply don't show a
 * "default" lore line.
 */
@Target(AnnotationTarget.VALUE_PARAMETER, AnnotationTarget.PROPERTY)
@Retention(AnnotationRetention.RUNTIME)
annotation class DefaultValue(val value: String)
