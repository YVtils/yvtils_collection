package yv.tils.waypoint

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import yv.tils.config.files.ObjectMapperFileUtils
import java.nio.file.Path
import java.util.UUID

class SharingPersistenceTest {
    @TempDir
    lateinit var directory: Path
    @Test
    fun `shared recipients and visibility survive save reload`() {
        val point = Waypoint(
            "home", UUID.randomUUID(), UUID.randomUUID(), 1.0, 64.0, 2.0,
            Visibility.SHARED, setOf(UUID.randomUUID(), UUID.randomUUID())
        )
        val path = directory.resolve("points.json").toString()
        ObjectMapperFileUtils.save(path, WaypointState(listOf(point)), overwriteParentDir = true)
        assertEquals(
            WaypointState(listOf(point)),
            ObjectMapperFileUtils.load(path, WaypointState(), overwriteParentDir = true)
        )
    }
}
