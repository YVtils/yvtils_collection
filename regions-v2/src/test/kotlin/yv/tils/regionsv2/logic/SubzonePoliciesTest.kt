package yv.tils.regionsv2.logic

import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.LocalPlayer
import com.sk89q.worldguard.protection.RegionResultSet
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import yv.tils.regionsv2.data.SubzoneRecord
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy
import java.util.UUID

class SubzonePoliciesTest {
    private val owner = UUID.randomUUID()
    private val member = UUID.randomUUID()
    private val visitor = UUID.randomUUID()
    private val base = ProtectedCuboidRegion("yv2_test", BlockVector3.ZERO, BlockVector3.at(100, 100, 100)).apply {
        owners.addPlayer(owner); members.addPlayer(member)
    }
    private val ownerPolicy = ClaimPolicies.create(base, ClaimRole.OWNER)
    private val memberPolicy = ClaimPolicies.create(base, ClaimRole.MEMBER)
    private val zone = SubzoneRecord("test", "Machine", 1, 2, 3, 10, 12, 13)
    private fun subject(uuid: UUID): LocalPlayer = Proxy.newProxyInstance(
        LocalPlayer::class.java.classLoader,
        arrayOf(LocalPlayer::class.java)
    ) { proxy, method, args ->
        when {
            method.isDefault -> InvocationHandler.invokeDefault(proxy, method, *(args ?: emptyArray()))
            method.name == "getUniqueId" -> uuid
            method.name == "getName" -> uuid.toString()
            method.name == "getGroups" -> emptyArray<String>()
            method.name == "hasGroup" -> false
            else -> error(method.name)
        }
    } as LocalPlayer

    @Test
    fun `subzone preserves all role combinations and changes only selected role`() {
        for (mask in 0..7) {
            val states = (0..2).map { if (mask and (1 shl it) == 0) StateFlag.State.DENY else StateFlag.State.ALLOW }
            ClaimPolicies.write(ownerPolicy, Flags.BLOCK_BREAK, ClaimRole.OWNER, states[0])
            ClaimPolicies.write(memberPolicy, Flags.BLOCK_BREAK, ClaimRole.MEMBER, states[1])
            ClaimPolicies.write(base, Flags.BLOCK_BREAK, ClaimRole.VISITOR, states[2])
            val zones = SubzonePolicies.create(base, ownerPolicy, memberPolicy, zone, ClaimFlags.roles)
            val set = RegionResultSet((listOf(base, ownerPolicy, memberPolicy) + zones).toMutableList(), null)
            listOf(owner, member, visitor).forEachIndexed { index, uuid ->
                assertEquals(states[index], set.queryState(subject(uuid), Flags.BLOCK_BREAK))
            }
            SubzonePolicies.override(zones, Flags.BLOCK_BREAK, ClaimRole.MEMBER, StateFlag.State.ALLOW)
            val changed = RegionResultSet((listOf(base, ownerPolicy, memberPolicy) + zones).toMutableList(), null)
            assertEquals(states[0], changed.queryState(subject(owner), Flags.BLOCK_BREAK))
            assertEquals(StateFlag.State.ALLOW, changed.queryState(subject(member), Flags.BLOCK_BREAK))
            assertEquals(states[2], changed.queryState(subject(visitor), Flags.BLOCK_BREAK))
        }
    }

    @Test
    fun `open mode overrides lower denies for players and nonplayer events including build`() {
        ClaimPolicies.write(base, Flags.TNT, null, StateFlag.State.DENY)
        ClaimPolicies.write(base, Flags.BLOCK_BREAK, ClaimRole.VISITOR, StateFlag.State.DENY)
        ClaimPolicies.write(ownerPolicy, Flags.BLOCK_BREAK, ClaimRole.OWNER, StateFlag.State.DENY)
        val zones = SubzonePolicies.create(
            base, ownerPolicy, memberPolicy, zone.copy(openProtection = true),
            ClaimFlags.roles + ClaimFlags.global
        )
        val set = RegionResultSet((listOf(base, ownerPolicy, memberPolicy) + zones).toMutableList(), null)
        for (uuid in listOf(owner, member, visitor)) {
            assertEquals(StateFlag.State.ALLOW, set.queryState(subject(uuid), Flags.BLOCK_BREAK))
            assertEquals(StateFlag.State.ALLOW, set.queryState(subject(uuid), Flags.BUILD))
            assertEquals(StateFlag.State.ALLOW, set.queryState(subject(uuid), Flags.TNT))
        }
        assertEquals(StateFlag.State.ALLOW, set.queryState(null, Flags.TNT))
        assertEquals(
            StateFlag.State.DENY, RegionResultSet(mutableListOf(base, ownerPolicy, memberPolicy), null)
                .queryState(subject(visitor), Flags.BLOCK_BREAK)
        )
    }

    @Test
    fun `containment checks vertical bounds and invalid ordering`() {
        assertTrue(
            SubzonePolicies.contained(
                SubzonePolicies.min(zone),
                SubzonePolicies.max(zone),
                base.minimumPoint,
                base.maximumPoint
            )
        )
        assertFalse(
            SubzonePolicies.contained(
                BlockVector3.at(0, -1, 0),
                BlockVector3.at(10, 10, 10),
                base.minimumPoint,
                base.maximumPoint
            )
        )
        assertFalse(
            SubzonePolicies.contained(
                BlockVector3.ZERO,
                BlockVector3.at(10, 101, 10),
                base.minimumPoint,
                base.maximumPoint
            )
        )
        assertFalse(
            SubzonePolicies.contained(
                BlockVector3.at(10, 10, 10),
                BlockVector3.ZERO,
                base.minimumPoint,
                base.maximumPoint
            )
        )
    }

    @Test
    fun `rebuilding removes old membership and inherits updated parent values`() {
        base.owners.removePlayer(owner); base.owners.addPlayer(member); base.members.removePlayer(member)
        ClaimPolicies.sync(base, ownerPolicy, ClaimRole.OWNER)
        ClaimPolicies.sync(base, memberPolicy, ClaimRole.MEMBER)
        ClaimPolicies.write(ownerPolicy, Flags.BLOCK_PLACE, ClaimRole.OWNER, StateFlag.State.ALLOW)
        ClaimPolicies.write(base, Flags.BLOCK_PLACE, ClaimRole.VISITOR, StateFlag.State.DENY)
        val zones = SubzonePolicies.create(base, ownerPolicy, memberPolicy, zone, ClaimFlags.roles)
        val set = RegionResultSet((listOf(base, ownerPolicy, memberPolicy) + zones).toMutableList(), null)
        assertEquals(StateFlag.State.ALLOW, set.queryState(subject(member), Flags.BLOCK_PLACE))
        assertEquals(StateFlag.State.DENY, set.queryState(subject(owner), Flags.BLOCK_PLACE))
        assertTrue(zones.all { it.parent == null })
    }

    @Test
    fun `turning open mode off restores inherited state and local overrides`() {
        ClaimPolicies.write(base, Flags.TNT, null, StateFlag.State.DENY)
        val zones = SubzonePolicies.create(
            base,
            ownerPolicy,
            memberPolicy,
            zone.copy(openProtection = false),
            ClaimFlags.global
        )
        SubzonePolicies.override(zones, Flags.TNT, null, StateFlag.State.ALLOW)
        val set = RegionResultSet((listOf(base, ownerPolicy, memberPolicy) + zones).toMutableList(), null)
        assertEquals(StateFlag.State.ALLOW, set.queryState(null, Flags.TNT))
        val inherited = SubzonePolicies.create(base, ownerPolicy, memberPolicy, zone, ClaimFlags.global)
        assertEquals(
            StateFlag.State.DENY, RegionResultSet(
                (listOf(base, ownerPolicy, memberPolicy) + inherited)
                    .toMutableList(), null
            ).queryState(null, Flags.TNT)
        )
    }

    @Test
    fun `geometry is truly 3D and IDs belong only to the parent claim`() {
        val zones = SubzonePolicies.create(base, ownerPolicy, memberPolicy, zone, ClaimFlags.roles)
        assertTrue(zones.all {
            it.minimumPoint == BlockVector3.at(1, 2, 3) && it.maximumPoint == BlockVector3.at(
                10,
                12,
                13
            )
        })
        assertTrue(zones.all { it.id.startsWith("yv2_test__zone_test") })
        assertEquals(3, zones.map { it.id }.toSet().size)
        assertTrue(zones.all { it.priority > memberPolicy.priority })
    }
}
