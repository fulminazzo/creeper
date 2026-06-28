package it.fulminazzo.creeper.tester.bukkit;

import be.seeseemelk.mockbukkit.MockBukkit;
import it.fulminazzo.creeper.tester.TestRunner;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.mockito.Mockito;
import org.slf4j.jul.JDK14LoggerAdapter;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CreeperTesterIntegrationTest {
    private static final @NotNull String TEST_BUILD_DIRECTORY = new File("").getAbsoluteFile().getParentFile().toPath()
            .resolve("integration-test").resolve("build").toString();

    private CreeperTester plugin;

    @BeforeEach
    void setup() throws IOException {
        MockBukkit.mock();
        plugin = MockBukkit.load(CreeperTester.class);

        File configFile = plugin.configuration();
        configFile.getParentFile().mkdirs();
        configFile.delete();
        configFile.createNewFile();
        try (FileWriter writer = new FileWriter(configFile)) {
            Yaml yaml = new Yaml();
            Map<String, Object> data = new HashMap<>();
            data.put("build-directory-path", TEST_BUILD_DIRECTORY);
            yaml.dump(data, writer);
        }
    }

    @AfterEach
    void tearDown() {
        plugin.getDataFolder().mkdirs();
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
    void testThatOnCommandWorks() {
        File directory = plugin.dataDirectory();

        CommandSender sender = mock(CommandSender.class);
        Command command = mock(Command.class);
        when(command.getName()).thenReturn("runcreepertests");

        assertTrue(plugin.onCommand(sender, command, command.getName(), new String[0]));

        File resultsFile = new File(directory, TestRunner.TEST_RESULTS_FILENAME);
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
    void testThatOnTabCompleteWorks() {
        CommandSender sender = mock(CommandSender.class);
        Command command = mock(Command.class);
        when(command.getName()).thenReturn("runcreepertests");

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
