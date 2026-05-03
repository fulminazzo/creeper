package it.fulminazzo.creeper.tester;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.java.JavaPluginLoader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.jul.JDK14LoggerAdapter;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Main class of the plugin.
 */
public final class CreeperTester extends JavaPlugin implements TesterApplication {
    private @Nullable Logger logger;

    /**
     * Instantiates a new Creeper tester.
     *
     * @param loader      the loader
     * @param description the description
     * @param dataFolder  the data folder
     * @param file        the file
     */
    public CreeperTester(final @NotNull JavaPluginLoader loader,
                         final @NotNull PluginDescriptionFile description,
                         final @NotNull File dataFolder,
                         final @NotNull File file) {
        super(loader, description, dataFolder, file);
    }

    @Override
    public void onEnable() {
        java.util.logging.Logger logger = getLogger();
        try {
            Constructor<?> constructor = JDK14LoggerAdapter.class.getDeclaredConstructor(java.util.logging.Logger.class);
            constructor.setAccessible(true);
            this.logger = (Logger) constructor.newInstance(logger);
        } catch (InvocationTargetException | IllegalAccessException | InstantiationException |
                 NoSuchMethodException e) {
            logger.log(Level.WARNING, "Error while creating logger", e);
            logger.warning("Shutting down plugin to prevent further errors");
            getServer().getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public boolean onCommand(final @NotNull CommandSender sender,
                             final @NotNull Command command,
                             final @NotNull String label,
                             final @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase(getCommandName())) {
            new TestCommand(this, sender::sendMessage).execute();
            return true;
        } else return super.onCommand(sender, command, label, args);
    }

    @Override
    public List<String> onTabComplete(final @NotNull CommandSender sender,
                                      final @NotNull Command command,
                                      final @NotNull String alias,
                                      final @NotNull String[] args) {
        if (command.getName().equalsIgnoreCase(getCommandName())) return Collections.emptyList();
        else return super.onTabComplete(sender, command, alias, args);
    }

    private @NotNull String getCommandName() {
        return getName().toLowerCase();
    }

    @Override
    public @NotNull Logger logger() {
        return Objects.requireNonNull(logger, "Logger not initialized");
    }

    @Override
    public @NotNull File dataDirectory() {
        return getDataFolder();
    }

}
