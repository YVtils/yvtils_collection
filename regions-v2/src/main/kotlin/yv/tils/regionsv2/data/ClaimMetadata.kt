package yv.tils.regionsv2.data

import yv.tils.configv2.files.ObjectMapperFileUtils
import java.util.UUID

data class ClaimRecord(
    val uuid: String = "",
    val world: String = "",
    val name: String = "",
    val created: Long = 0,
    val owners: List<String> = emptyList(),
    val members: List<String> = emptyList(),
    val welcome: String = "",
    val goodbye: String = "",
    /** Null grandfathers the existing footprint at the current configured price. */
    val currencyCredit: Long? = null,
    val subzones: List<SubzoneRecord> = emptyList(),
)

data class SubzoneRecord(
    val uuid: String = "",
    val name: String = "",
    val minX: Int = 0, val minY: Int = 0, val minZ: Int = 0,
    val maxX: Int = 0, val maxY: Int = 0, val maxZ: Int = 0,
    val openProtection: Boolean = false,
    /** Scope -> flag name -> WorldGuard-marshalled YAML. Missing values inherit the claim. */
    val overrides: Map<String, Map<String, String>> = emptyMap(),
)

data class ClaimMetadataState(val claims: Map<String, ClaimRecord> = emptyMap())

/** UUID identity/display metadata only; WorldGuard remains authoritative for protection. */
object ClaimMetadata {
    private const val PATH = "/regions-v2/claims.json"
    var state = ClaimMetadataState()
        private set

    fun load() {
        state = ObjectMapperFileUtils.load(PATH, ClaimMetadataState())
    }

    fun save(records: Map<String, ClaimRecord>) {
        val next = ClaimMetadataState(records)
        ObjectMapperFileUtils.save(PATH, next)
        state = next
    }

    fun put(record: ClaimRecord) = save(state.claims + (record.uuid to record))
    fun remove(uuid: UUID) = save(state.claims - uuid.toString())
}
