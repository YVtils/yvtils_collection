/*
 * Part of the YVtils Project.
 * Copyright (c) 2026 Lyvric / YVtils
 * Licensed under the Mozilla Public License 2.0 (MPL-2.0)
 * with additional YVtils License Terms: https://yvtils.net/license
 * Use of the YVtils name, logo, or brand assets is subject to
 * the YVtils Brand Protection Clause.
 */
package yv.tils.regions.logic

import com.sk89q.worldedit.math.BlockVector3
import com.sk89q.worldguard.LocalPlayer
import com.sk89q.worldguard.protection.RegionResultSet
import com.sk89q.worldguard.protection.FlagValueCalculator
import com.sk89q.worldguard.protection.association.RegionAssociable
import com.sk89q.worldguard.protection.flags.Flags
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.UUID
import java.lang.reflect.InvocationHandler
import java.lang.reflect.Proxy

class ClaimPoliciesTest {
    private val owner = UUID.randomUUID()
    private val member = UUID.randomUUID()
    private val visitor = UUID.randomUUID()
    private val base = ProtectedCuboidRegion("yv2_test", BlockVector3.ZERO, BlockVector3.at(10, 10, 10)).apply {
        owners.addPlayer(owner)
        members.addPlayer(member)
    }
    private val ownerPolicy = ClaimPolicies.create(base, ClaimRole.OWNER)
    private val memberPolicy = ClaimPolicies.create(base, ClaimRole.MEMBER)
    private fun subject(uuid: UUID): RegionAssociable = Proxy.newProxyInstance(
        LocalPlayer::class.java.classLoader, arrayOf(LocalPlayer::class.java)
    ) { proxy, method, args ->
        when {
            method.isDefault -> InvocationHandler.invokeDefault(proxy, method, *(args ?: emptyArray()))
            method.name == "getUniqueId" -> uuid
            method.name == "getName" -> uuid.toString()
            method.name == "getGroups" -> emptyArray<String>()
            method.name == "hasGroup" -> false
            else -> error("Unexpected LocalPlayer method: ${method.name}")
        }
    } as LocalPlayer

    private fun set() = RegionResultSet(mutableListOf<ProtectedRegion>(base, ownerPolicy, memberPolicy), null)

    @Test
    fun `all eight role combinations are enforced independently by WorldGuard`() {
        for (flag in ClaimFlags.roles) for (mask in 0..7) {
            val expected = (0..2).map { if (mask and (1 shl it) != 0) StateFlag.State.ALLOW else StateFlag.State.DENY }
            ClaimPolicies.write(ownerPolicy, flag, ClaimRole.OWNER, expected[0])
            ClaimPolicies.write(memberPolicy, flag, ClaimRole.MEMBER, expected[1])
            ClaimPolicies.write(base, flag, ClaimRole.VISITOR, expected[2])
            listOf(owner, member, visitor).forEachIndexed { index, uuid ->
                assertEquals(
                    expected[index],
                    set().queryState(subject(uuid), flag),
                    "${flag.name}, mask=$mask, role=$index"
                )
            }
        }
    }

    @Test
    fun `global policies apply to every role and nonplayer subjects`() {
        for (flag in ClaimFlags.global) for (state in StateFlag.State.entries) {
            ClaimPolicies.write(base, flag, null, state)
            for (uuid in listOf(owner, member, visitor)) assertEquals(state, set().queryState(subject(uuid), flag))
            assertEquals(state, set().queryState(null, flag))
        }
    }

    @Test
    fun `membership changes update policies without retaining old grants`() {
        ClaimPolicies.write(ownerPolicy, Flags.BLOCK_BREAK, ClaimRole.OWNER, StateFlag.State.ALLOW)
        ClaimPolicies.write(memberPolicy, Flags.BLOCK_BREAK, ClaimRole.MEMBER, StateFlag.State.DENY)
        ClaimPolicies.write(base, Flags.BLOCK_BREAK, ClaimRole.VISITOR, StateFlag.State.DENY)
        base.members.removePlayer(member)
        base.owners.addPlayer(member)
        ClaimPolicies.sync(base, ownerPolicy, ClaimRole.OWNER)
        ClaimPolicies.sync(base, memberPolicy, ClaimRole.MEMBER)
        assertEquals(StateFlag.State.ALLOW, set().queryState(subject(member), Flags.BLOCK_BREAK))
        base.owners.removePlayer(member)
        ClaimPolicies.sync(base, ownerPolicy, ClaimRole.OWNER)
        ClaimPolicies.sync(base, memberPolicy, ClaimRole.MEMBER)
        assertEquals(StateFlag.State.DENY, set().queryState(subject(member), Flags.BLOCK_BREAK))
    }

    @Test
    fun `text and numeric flags use independent role groups and global scope`() {
        ClaimPolicies.write(ownerPolicy, Flags.GREET_MESSAGE, ClaimRole.OWNER, "Owner greeting")
        ClaimPolicies.write(memberPolicy, Flags.GREET_MESSAGE, ClaimRole.MEMBER, "Member greeting")
        ClaimPolicies.write(base, Flags.GREET_MESSAGE, ClaimRole.VISITOR, "Visitor greeting")
        assertEquals("Owner greeting", set().queryValue(subject(owner), Flags.GREET_MESSAGE))
        assertEquals("Member greeting", set().queryValue(subject(member), Flags.GREET_MESSAGE))
        assertEquals("Visitor greeting", set().queryValue(subject(visitor), Flags.GREET_MESSAGE))
        ClaimPolicies.write(base, Flags.HEAL_AMOUNT, null, 2)
        for (uuid in listOf(owner, member, visitor)) assertEquals(2, set().queryValue(subject(uuid), Flags.HEAL_AMOUNT))
    }

    @Test
    fun `policy regions preserve native build membership protection`() {
        assertEquals(StateFlag.State.ALLOW, set().queryState(subject(owner), Flags.BUILD))
        assertEquals(StateFlag.State.ALLOW, set().queryState(subject(member), Flags.BUILD))
        // WorldGuard represents failed implicit build membership with a null BUILD value;
        // its protection listener also checks membership rather than expecting explicit DENY.
        val calculator = FlagValueCalculator(mutableListOf(ownerPolicy, memberPolicy, base), null)
        assertEquals(FlagValueCalculator.Result.FAIL, calculator.getMembership(subject(visitor)))
        assertEquals(FlagValueCalculator.Result.SUCCESS, calculator.getMembership(subject(owner)))
        assertEquals(FlagValueCalculator.Result.SUCCESS, calculator.getMembership(subject(member)))
    }
}
