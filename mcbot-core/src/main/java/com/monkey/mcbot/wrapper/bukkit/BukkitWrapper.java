package com.monkey.mcbot.wrapper.bukkit;

import com.monkey.mcbot.wrapper.PlatformWrapper;
import com.monkey.mcbot.wrapper.WrapperCapabilities;
import com.monkey.mcbot.wrapper.WrapperTask;
import com.monkey.mcbot.wrapper.WrapperType;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.util.logging.Level;

public final class BukkitWrapper implements PlatformWrapper {

    private final JavaPlugin plugin;
    private final WrapperCapabilities capabilities;

    public BukkitWrapper(JavaPlugin plugin) {
        this.plugin = plugin;
        this.capabilities = new WrapperCapabilities(false, false, true, false);
    }

    @Override
    public WrapperType type() {
        return WrapperType.BUKKIT;
    }

    @Override
    public WrapperCapabilities capabilities() {
        return capabilities;
    }

    @Override
    public WrapperTask runSync(Runnable task) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        return WrapperTask.bukkit("bukkit-sync", scheduler.runTask(plugin, wrap(task)));
    }

    @Override
    public WrapperTask runSyncLater(Runnable task, long delayTicks) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        return WrapperTask.bukkit("bukkit-sync-later", scheduler.runTaskLater(plugin, wrap(task), Math.max(1L, delayTicks)));
    }

    @Override
    public WrapperTask runEntity(Player player, Runnable task) {
        return runSync(task);
    }

    @Override
    public WrapperTask runEntityLater(Player player, long delayTicks, Runnable task) {
        return runSyncLater(task, delayTicks);
    }

    @Override
    public WrapperTask runAsync(Runnable task) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        return WrapperTask.bukkit("bukkit-async", scheduler.runTaskAsynchronously(plugin, wrap(task)));
    }

    @Override
    public WrapperTask runAsyncLater(Runnable task, long delayTicks) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        return WrapperTask.bukkit("bukkit-async-later", scheduler.runTaskLaterAsynchronously(plugin, wrap(task), Math.max(1L, delayTicks)));
    }

    @Override
    public WrapperTask runAsyncRepeating(Runnable task, long delayTicks, long periodTicks) {
        BukkitScheduler scheduler = Bukkit.getScheduler();
        return WrapperTask.bukkit(
                "bukkit-async-repeating",
                scheduler.runTaskTimerAsynchronously(plugin, wrap(task), Math.max(1L, delayTicks), Math.max(1L, periodTicks))
        );
    }

    private Runnable wrap(Runnable delegate) {
        return () -> {
            try {
                delegate.run();
            } catch (Throwable error) {
                plugin.getLogger().log(Level.WARNING, "[Wrapper/Bukkit] scheduled task failed: " + error.getMessage(), error);
            }
        };
    }
}
