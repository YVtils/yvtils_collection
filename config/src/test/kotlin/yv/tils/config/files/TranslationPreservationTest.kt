package yv.tils.config.files

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText
import yv.tils.config.language.BuildLanguage
import yv.tils.config.language.FileTypes

class TranslationPreservationTest {
    @TempDir lateinit var directory: Path

    @Test fun `registered defaults fill missing keys while preserving customized translations`() {
        val previous = ConfigurateFileUtils.baseDirectory
        try {
            ConfigurateFileUtils.baseDirectory = directory.toFile()
            val languages = directory.resolve("languages").toFile()
            languages.mkdirs()
            val english = languages.toPath().resolve("en.yml")
            english.writeText("releaseTest:\n  existing: My custom translation\n")
            BuildLanguage.registerString(BuildLanguage.RegisteredString(FileTypes.EN, "releaseTest.existing", "Default"))
            BuildLanguage.registerString(BuildLanguage.RegisteredString(FileTypes.EN, "releaseTest.added", "New translation"))
            BuildLanguage.buildFiles()
            val node = ConfigurateFileUtils.load("/languages/en.yml", ConfigFormat.YAML).node
            assertEquals("My custom translation", node.node("releaseTest", "existing").string)
            assertEquals("New translation", node.node("releaseTest", "added").string)
        } finally {
            ConfigurateFileUtils.baseDirectory = previous
        }
    }
}
