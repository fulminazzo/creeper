package it.fulminazzo.creeper.tester.bukkit;

import it.fulminazzo.creeper.tester.TestWorker;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Closeable;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Bukkit implementation of {@link TestWorker}.
 * <br>
 * Executes a task every {@link #DELAY} milliseconds <b>synchronously</b>.
 */
final class BukkitTestWorker implements TestWorker, Closeable {
    private static final int DELAY = 100;

    private final @NotNull Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    private final @NotNull BukkitTask mainTask;

    private @Nullable BukkitTask current;
    private long lastFinished;

    /**
     * Instantiates a new Bukkit test worker.
     *
     * @param plugin the plugin to run the tasks with
     */
    public BukkitTestWorker(final @NotNull JavaPlugin plugin) {
        this.mainTask = plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                plugin,
                () -> {
                    if (current != null && !current.isCancelled()) return;
                    if (tasks.isEmpty() || System.currentTimeMillis() - lastFinished < DELAY) return;
                    Runnable task = tasks.poll();
                    if (task == null) return;
                    current = plugin.getServer().getScheduler().runTask(
                            plugin,
                            () -> {
                                task.run();
                                lastFinished = System.currentTimeMillis();
                                current = null;
                            }
                    );
                },
                0,
                DELAY * 20 / 1000
        );
    }

    @Override
    public void schedule(final @NotNull Runnable runnable) {
        tasks.add(runnable);
    }

    @Override
    public void close() {
        if (current != null) current.cancel();
        mainTask.cancel();
    }

}
