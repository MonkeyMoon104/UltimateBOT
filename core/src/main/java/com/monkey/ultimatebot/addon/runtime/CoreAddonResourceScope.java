package com.monkey.ultimatebot.addon.runtime;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.addon.AddonResourceScope;
import com.monkey.ultimatebot.api.addon.AddonTask;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.jspecify.annotations.Nullable;

final class CoreAddonResourceScope implements AddonResourceScope {
    private final UltimateBot plugin;
    private final Logger logger;
    private final Deque<AutoCloseable> resources = new ArrayDeque<>();
    private final AtomicBoolean closed = new AtomicBoolean();

    CoreAddonResourceScope(UltimateBot plugin, Logger logger) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.logger = Objects.requireNonNull(logger, "logger");
    }

    @Override
    public synchronized <T extends AutoCloseable> T own(T resource) {
        T checked = Objects.requireNonNull(resource, "resource");
        ensureOpen();
        resources.push(checked);
        return checked;
    }

    @Override
    public Listener registerListener(Listener listener) {
        Listener checked = Objects.requireNonNull(listener, "listener");
        ensureOpen();
        plugin.getServer().getPluginManager().registerEvents(checked, plugin);
        own(() -> HandlerList.unregisterAll(checked));
        return checked;
    }

    @Override
    public AddonTask runTask(Runnable task) {
        return schedule(Execution.GLOBAL, null, task, 0L, 0L);
    }

    @Override
    public AddonTask runTaskLater(Runnable task, long delayTicks) {
        return schedule(Execution.GLOBAL, null, task, delayTicks, 0L);
    }

    @Override
    public AddonTask runTaskTimer(Runnable task, long delayTicks, long periodTicks) {
        return schedule(Execution.GLOBAL, null, task, delayTicks, requirePositive(periodTicks, "periodTicks"));
    }

    @Override
    public AddonTask runEntityTask(Player player, Runnable task) {
        return schedule(Execution.ENTITY, Objects.requireNonNull(player, "player"), task, 0L, 0L);
    }

    @Override
    public AddonTask runEntityTaskLater(Player player, Runnable task, long delayTicks) {
        return schedule(Execution.ENTITY, Objects.requireNonNull(player, "player"), task, delayTicks, 0L);
    }

    @Override
    public AddonTask runAsyncTask(Runnable task) {
        return schedule(Execution.ASYNC, null, task, 0L, 0L);
    }

    @Override
    public AddonTask runAsyncTaskLater(Runnable task, long delayTicks) {
        return schedule(Execution.ASYNC, null, task, delayTicks, 0L);
    }

    @Override
    public AddonTask runAsyncTaskTimer(Runnable task, long delayTicks, long periodTicks) {
        return schedule(Execution.ASYNC, null, task, delayTicks, requirePositive(periodTicks, "periodTicks"));
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public synchronized void close() {
        if (!closed.compareAndSet(false, true)) {
            return;
        }
        while (!resources.isEmpty()) {
            try {
                resources.pop().close();
            } catch (Exception error) {
                logger.log(Level.WARNING, "Unable to release an addon resource", error);
            }
        }
    }

    private AddonTask schedule(
            Execution execution, @Nullable Player player, Runnable task, long delayTicks, long periodTicks) {
        requireNonNegative(delayTicks, "delayTicks");
        CoreAddonScheduledTask scheduled = new CoreAddonScheduledTask(
                plugin, logger, execution, player, Objects.requireNonNull(task, "task"), periodTicks, this::release);
        own(scheduled);
        scheduled.start(delayTicks);
        return scheduled;
    }

    private void ensureOpen() {
        if (closed.get()) {
            throw new IllegalStateException("addon resource scope is closed");
        }
    }

    private synchronized void release(AutoCloseable resource) {
        resources.removeFirstOccurrence(resource);
    }

    private static long requirePositive(long value, String name) {
        if (value <= 0L) {
            throw new IllegalArgumentException(name + " must be positive");
        }
        return value;
    }

    private static void requireNonNegative(long value, String name) {
        if (value < 0L) {
            throw new IllegalArgumentException(name + " cannot be negative");
        }
    }

    enum Execution {
        GLOBAL,
        ENTITY,
        ASYNC
    }
}
