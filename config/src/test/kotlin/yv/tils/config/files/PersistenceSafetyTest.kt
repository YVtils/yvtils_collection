package yv.tils.config.files

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.readText
import kotlin.io.path.writeText

class PersistenceSafetyTest {
    @TempDir
    lateinit var directory: Path

    data class Settings(val enabled: Boolean = true)
    data class Record(val uuid: String, val count: Int)

    @Test fun `missing list wrapper cannot silently erase an existing file`() {
        val path = directory.resolve("wrong.json")
        path.writeText("""{"unrelated":42}""")
        assertThrows(IllegalArgumentException::class.java) {
            ObjectMapperFileUtils.loadList<Record>(path.toString(), overwriteParentDir = true)
        }
        assertEquals("""{"unrelated":42}""", path.readText())
    }

    @Test
    fun `missing object returns defaults but malformed existing object is preserved`() {
        val path = directory.resolve("settings.json")
        assertEquals(Settings(), ObjectMapperFileUtils.load(path.toString(), Settings(), overwriteParentDir = true))
        path.writeText("{broken")
        assertThrows(Exception::class.java) {
            val state = ObjectMapperFileUtils.load(path.toString(), Settings(), overwriteParentDir = true)
            ObjectMapperFileUtils.save(path.toString(), state, overwriteParentDir = true)
        }
        assertEquals("{broken", path.readText())
    }

    @Test
    fun `invalid record rejects entire save without discarding valid records`() {
        val path = directory.resolve("saves.json")
        val original = """{"saves":[{"uuid":"a","count":1},{"uuid":"b","count":"invalid"}]}"""
        path.writeText(original)
        assertThrows(Exception::class.java) {
            val state = ObjectMapperFileUtils.loadList<Record>(path.toString(), overwriteParentDir = true)
            ObjectMapperFileUtils.saveList(path.toString(), state, overwriteParentDir = true)
        }
        assertEquals(original, path.readText())
    }

    @Test
    fun `empty lists survive save and reload`() {
        val path = directory.resolve("empty.json").toString()
        ObjectMapperFileUtils.saveList<Record>(path, emptyList(), overwriteParentDir = true)
        assertEquals(emptyList<Record>(), ObjectMapperFileUtils.loadList<Record>(path, overwriteParentDir = true))
        assertTrue(Path.of(path).readText().contains("[]"))
    }

    @Test
    fun `failed serialization leaves previously committed file intact`() {
        val path = directory.resolve("state.json").toString()
        ObjectMapperFileUtils.save(path, Settings(false), overwriteParentDir = true)
        val original = Path.of(path).readText()
        assertThrows(Exception::class.java) {
            ConfigurateFileUtils.save(
                ConfigurateFileUtils.create(
                    path,
                    mapOf("unsupported" to Any()), ConfigFormat.JSON, overwriteParentDir = true
                )
            )
        }
        assertEquals(original, Path.of(path).readText())
    }
}
