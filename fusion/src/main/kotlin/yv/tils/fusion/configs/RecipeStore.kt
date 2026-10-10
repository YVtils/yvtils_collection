/* Part of the YVtils Project. Copyright (c) 2026 Lyvric / YVtils.
 * MPL-2.0 with additional terms: https://yvtils.net/license */
package yv.tils.fusion.configs

import yv.tils.fusion.FusionRecipe
import yv.tils.fusion.RecipeBook

import org.spongepowered.configurate.BasicConfigurationNode
import org.spongepowered.configurate.ConfigurationNode
import org.spongepowered.configurate.ConfigurationOptions
import org.spongepowered.configurate.objectmapping.ObjectMapper
import org.spongepowered.configurate.kotlin.dataClassFieldDiscoverer
import org.spongepowered.configurate.util.NamingSchemes
import org.spongepowered.configurate.serialize.TypeSerializerCollection
import io.leangen.geantyref.GenericTypeReflector
import yv.tils.configv2.files.ConfigFormat
import yv.tils.configv2.files.ConfigurateFileUtils
import yv.tils.configv2.files.ObjectMapperFileUtils
import yv.tils.fusion.api.FusionRegistry
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

object RecipeStore {
    private const val DIRECTORY = "/fusion/recipes"
    private const val COMBINED_JSON = "/fusion/recipes.json"
    private const val COMBINED_YAML = "/fusion/recipes.yml"
    var book = RecipeBook()
        private set

    private fun directory(): Path = ConfigurateFileUtils.create(
        "$DIRECTORY/location.json", emptyMap(), ConfigFormat.JSON
    ).file.toPath().parent

    fun reload() {
        val directory = directory()
        if (!Files.exists(directory)) {
            val json = ConfigurateFileUtils.create(COMBINED_JSON, emptyMap(), ConfigFormat.JSON).file
            val yaml = ConfigurateFileUtils.create(COMBINED_YAML, emptyMap(), ConfigFormat.YAML).file
            val loaded = when {
                json.exists() -> readCombined(COMBINED_JSON, ConfigFormat.JSON)
                yaml.exists() -> readCombined(COMBINED_YAML, ConfigFormat.YAML)
                else -> RecipeBook()
            }
            val candidate = loaded.copy(recipes = loaded.recipes.map(DefaultRecipes::upgradeGlass))
            validate(candidate)
            migrate(directory, candidate)
            book = candidate
            return
        }
        require(Files.isDirectory(directory)) { "Fusion recipes path must be a directory" }
        val files = Files.list(directory).use { paths ->
            paths.filter { it.fileName.toString().endsWith(".json", ignoreCase = true) }.sorted().toList()
        }
        require(files.size <= 500) { "Maximum 500 recipes" }
        val loaded = files.map(::readRecipe)
        val candidate = RecipeBook(recipes = loaded.map(DefaultRecipes::upgradeGlass))
        validate(candidate)
        candidate.recipes.zip(loaded).forEach { (updated, original) ->
            if (updated != original) persist(directory.resolve("${updated.id}.json"), updated)
        }
        book = candidate
    }

    private fun <T : Any> map(node: ConfigurationNode, type: Class<T>): T {
        val mapper = ObjectMapper.factoryBuilder().addDiscoverer(dataClassFieldDiscoverer())
            .defaultNamingScheme(NamingSchemes.PASSTHROUGH).build()
        val options = ConfigurationOptions.defaults().serializers(
            TypeSerializerCollection.defaults().childBuilder().register(
                { candidate -> runCatching { GenericTypeReflector.erase(candidate).kotlin.isData }.getOrDefault(false) },
                mapper.asTypeSerializer()
            ).build()
        )
        return mapper.get(type).load(BasicConfigurationNode.root(options).from(node))
    }

    private fun readCombined(path: String, format: ConfigFormat): RecipeBook {
        val node = ConfigurateFileUtils.load(path, format).node
        require(node.isMap && !node.node("schemaVersion").virtual() && node.node("recipes").isList) {
            "Combined recipe file must contain schemaVersion and a recipes list"
        }
        val candidate = map(node, RecipeBook::class.java)
        require(candidate.schemaVersion in 1..2) { "Unsupported combined recipe schema" }
        return candidate.copy(
            schemaVersion = 2, recipes = candidate.recipes +
                    if (candidate.schemaVersion == 1 && candidate.recipes.none { it.id == "nourishing-flask" })
                        listOf(DefaultRecipes.nourishingFlask()) else emptyList()
        )
    }

    private fun readRecipe(path: Path): FusionRecipe {
        val node = ConfigurateFileUtils.load(path.toString(), ConfigFormat.JSON, overwriteParentDir = true).node
        require(node.isMap && node.node("schemaVersion").int == 1 && !node.node("id").virtual()) {
            "${path.fileName}: expected individual recipe schemaVersion 1 and id"
        }
        val recipe = map(node, FusionRecipe::class.java)
        require(path.fileName.toString() == "${recipe.id}.json") { "Recipe filename must match its id: ${path.fileName}" }
        recipe.validate()
        return recipe
    }

    private fun validate(candidate: RecipeBook) {
        require(candidate.recipes.size <= 500) { "Maximum 500 recipes" }
        require(candidate.recipes.map { it.id }.distinct().size == candidate.recipes.size) { "Duplicate recipe IDs" }
        candidate.recipes.forEach(FusionRecipe::validate)
        require(candidate.recipes.none { it.id in FusionRegistry.runtimeIds() }) { "Recipe ID conflicts with an extension" }
    }

    fun save(draft: FusionRecipe, original: FusionRecipe?) {
        require(book.recipes.firstOrNull { it.id == draft.id } == original) { "Recipe changed; reopen the editor" }
        val candidate = book.copy(recipes = book.recipes.filterNot { it.id == draft.id } + draft)
        validate(candidate)
        val path = directory().resolve("${draft.id}.json")
        require((if (Files.exists(path)) readRecipe(path) else null) == original) {
            "Recipe file changed on disk; reload and reopen the editor"
        }
        persist(path, draft)
        book = candidate
    }

    /** Build the complete directory privately, then make it authoritative in one atomic move. */
    private fun migrate(directory: Path, candidate: RecipeBook) {
        Files.createDirectories(directory.parent)
        val staging = Files.createTempDirectory(directory.parent, ".recipes-migration-")
        try {
            candidate.recipes.forEach { persist(staging.resolve("${it.id}.json"), it) }
            Files.move(staging, directory, StandardCopyOption.ATOMIC_MOVE)
        } finally {
            if (Files.exists(staging)) {
                Files.list(staging).use { it.forEach(Files::deleteIfExists) }
                Files.deleteIfExists(staging)
            }
        }
    }

    private fun persist(path: Path, recipe: FusionRecipe) {
        val temporary = Files.createTempFile(path.parent, ".recipe-", ".tmp")
        try {
            ObjectMapperFileUtils.save(temporary.toString(), recipe, ConfigFormat.JSON, overwriteParentDir = true)
            val file = ConfigurateFileUtils.load(temporary.toString(), ConfigFormat.JSON, overwriteParentDir = true)
            file.node.node("schemaVersion").set(1)
            ConfigurateFileUtils.save(file)
            Files.move(temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING)
        } finally {
            Files.deleteIfExists(temporary)
        }
    }
}
