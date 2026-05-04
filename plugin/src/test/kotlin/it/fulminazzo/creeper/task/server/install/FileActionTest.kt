package it.fulminazzo.creeper.task.server.install

import com.fasterxml.jackson.module.kotlin.readValue
import io.mockk.every
import io.mockk.mockk
import it.fulminazzo.creeper.CreeperPlugin
import it.fulminazzo.creeper.PlayerProfile
import it.fulminazzo.creeper.PlayerResolver
import it.fulminazzo.creeper.extension.spec.MinecraftServerSpec
import it.fulminazzo.creeper.extension.spec.ServerSpec
import it.fulminazzo.creeper.task.server.install.FileAction.Eula
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import java.nio.file.Path
import java.util.*
import kotlin.io.path.exists
import kotlin.io.path.readLines
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class FileActionTest {
    private val specification: ServerSpec<*, *> = mockk()

    private val resolver = mockk<PlayerResolver>()

    @BeforeEach
    fun setup() {
        DIRECTORY.toFile().deleteRecursively()
    }

    @Test
    fun `test that Eula FileAction sets eula to true`() {
        Eula.apply(DIRECTORY, specification, resolver)

        val eulaFile = DIRECTORY.resolve("eula.txt")
        assertTrue(eulaFile.exists(), "Eula file does not exist: $eulaFile")
        assertContains(eulaFile.readLines(), "eula=true")
    }

    @Test
    fun `test that Whitelist FileAction writes whitelist when specification is MinecraftServer and whitelist is not empty`() {
        val specification = mockk<MinecraftServerSpec>()
        every { specification.whitelist } returns setOf("Fulminazzo", "xca_mux")
        every { specification.settings.onlineMode } returns false
        every { resolver.getPlayerProfiles(any(), any()) } answers {
            val usernames = args[0] as Collection<*>
            usernames
                .map { it as String }
                .filter { it == "Fulminazzo" }
                .map { PlayerProfile(UUID.nameUUIDFromBytes(it.toByteArray()), it) }
                .toSet()
        }

        FileAction.Whitelist.apply(DIRECTORY, specification, resolver)

        val whitelistFile = DIRECTORY.resolve("whitelist.json")
        assertTrue(whitelistFile.exists(), "Whitelist file does not exist: $whitelistFile")

        val data = CreeperPlugin.JSON_MAPPER.readValue<Set<PlayerProfile>>(whitelistFile.toFile())
        assertEquals(
            setOf(PlayerProfile(UUID.nameUUIDFromBytes("Fulminazzo".toByteArray()), "Fulminazzo")),
            data
        )
    }

    @Test
    fun `test that Whitelist FileAction does not write whitelist when whitelist is empty`() {
        val specification = mockk<MinecraftServerSpec>()
        every { specification.whitelist } returns setOf("Fulminazzo", "xca_mux")
        every { specification.settings.onlineMode } returns false
        every { resolver.getPlayerProfiles(any(), any()) } returns emptySet()

        FileAction.Whitelist.apply(DIRECTORY, specification, resolver)

        val whitelistFile = DIRECTORY.resolve("whitelist.json")
        assertFalse(whitelistFile.exists(), "Whitelist file should not exist: $whitelistFile")
    }

    @Test
    fun `test that Operators FileAction writes operators when specification is MinecraftServer and operators is not empty`() {
        val specification = mockk<MinecraftServerSpec>()
        every { specification.operators } returns setOf("Fulminazzo", "xca_mux")
        every { specification.settings.onlineMode } returns false
        every { resolver.getPlayerProfiles(any(), any()) } answers {
            val usernames = args[0] as Collection<*>
            usernames
                .map { it as String }
                .filter { it == "Fulminazzo" }
                .map { PlayerProfile(UUID.nameUUIDFromBytes(it.toByteArray()), it) }
                .toSet()
        }

        FileAction.Operators.apply(DIRECTORY, specification, resolver)

        val opsFile = DIRECTORY.resolve("ops.json")
        assertTrue(opsFile.exists(), "Operators file does not exist: $opsFile")

        val data = CreeperPlugin.JSON_MAPPER.readValue<Set<Map<String, String>>>(opsFile.toFile())
        assertEquals(
            setOf(
                mapOf(
                    "uuid" to UUID.nameUUIDFromBytes("Fulminazzo".toByteArray()).toString(),
                    "name" to "Fulminazzo",
                    "level" to "4",
                    "bypassesPlayerLimit" to "false"
                )
            ),
            data
        )
    }

    @Test
    fun `test that Operators FileAction does not write operators when operators is empty`() {
        val specification = mockk<MinecraftServerSpec>()
        every { specification.operators } returns setOf("Fulminazzo", "xca_mux")
        every { specification.settings.onlineMode } returns false
        every { resolver.getPlayerProfiles(any(), any()) } returns emptySet()

        FileAction.Operators.apply(DIRECTORY, specification, resolver)

        val opsFile = DIRECTORY.resolve("ops.json")
        assertFalse(opsFile.exists(), "Operators file should not exist: $opsFile")
    }

    private companion object {
        private var DIRECTORY: Path = Path.of("build/resources/test/task/server/install/file_action_test")

    }

}