package it.fulminazzo.creeper.tester;

import be.seeseemelk.mockbukkit.MockBukkit;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.slf4j.jul.JDK14LoggerAdapter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreeperTesterIntegrationTest {
    private CreeperTester plugin;

    @BeforeAll
    static void setupAll() {
        MockBukkit.mock();
    }

    @BeforeEach
    void setup() {
        plugin = MockBukkit.load(CreeperTester.class);
    }

    @AfterAll
    static void tearDownAll() {
        MockBukkit.unmock();
    }

    @Test
    void testOnEnableExceptionDisablesPlugin() {
        try (MockedConstruction<JDK14LoggerAdapter> ignored = Mockito.mockConstruction(
                JDK14LoggerAdapter.class,
                (mock, context) -> {
                    throw new RuntimeException("Test exception");
                })) {
            plugin.onEnable();

            assertFalse(plugin.isEnabled(), "Plugin should be disabled after a logger creation failure");

            assertFalse(
                    Bukkit.getServer().getPluginManager().isPluginEnabled(plugin),
                    "Plugin Manager should report the plugin as disabled"
            );
        }
    }

    @Test
    void testThatOnCommandWorks() throws IOException {
        File directory = plugin.dataDirectory();
        Files.deleteIfExists(directory.toPath());

        CommandSender sender = mock(CommandSender.class);
        Command command = mock(Command.class);
        when(command.getName()).thenReturn("creepertester");

        assertTrue(plugin.onCommand(sender, command, command.getName(), new String[0]));

        File resultsFile = new File(directory, TestsRunner.TEST_RESULTS_FILENAME);
        assertTrue(resultsFile.exists(), "Results file should have been created");
    }

    @Test
    void testThatOnCommandDoesNotExecuteIfDoesNotMatch() {
        CommandSender sender = mock(CommandSender.class);
        Command command = mock(Command.class);
        when(command.getName()).thenReturn("somethingelse");

        assertFalse(plugin.onCommand(sender, command, command.getName(), new String[0]));
    }

    @Test
    void testThatOnTabCompleteWorks() throws IOException {
        File directory = plugin.dataDirectory();
        Files.deleteIfExists(directory.toPath());

        CommandSender sender = mock(CommandSender.class);
        Command command = mock(Command.class);
        when(command.getName()).thenReturn("creepertester");

        List<String> completions = plugin.onTabComplete(sender, command, command.getName(), new String[0]);
        assertNotNull(completions, "Tab completions should not be null");
        assertTrue(completions.isEmpty(), "Tab completions should be empty");
    }

    @Test
    void testThatOnTabCompleteDoesNotExecuteIfDoesNotMatch() {
        CommandSender sender = mock(CommandSender.class);
        Command command = mock(Command.class);
        when(command.getName()).thenReturn("somethingelse");

        List<String> completions = plugin.onTabComplete(sender, command, command.getName(), new String[0]);
        assertNull(completions, "Tab completions should be null");
    }

}
