package yv.tils.vanish

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import yv.tils.config.files.ObjectMapperFileUtils
import yv.tils.vanish.configs.SavedSettings
import yv.tils.vanish.configs.VanishSettings
import java.nio.file.Path
import java.util.UUID

class SettingsPersistenceTest {
    @TempDir
    lateinit var directory: Path
    @Test
    fun `personal preferences retain UUID identity and disabled functions after restart`() {
        val uuid = UUID.randomUUID()
        val settings =
            SavedSettings(mapOf(uuid to VanishSettings(layer = 7, announce = false, silentContainers = false)))
        val path = directory.resolve("players.json").toString()
        ObjectMapperFileUtils.save(path, settings, overwriteParentDir = true)
        assertEquals(settings, ObjectMapperFileUtils.load(path, SavedSettings(), overwriteParentDir = true))
    }
}
