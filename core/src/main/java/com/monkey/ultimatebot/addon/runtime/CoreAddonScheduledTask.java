package com.monkey.ultimatebot.addon.runtime;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.addon.AddonTask;
import com.monkey.ultimatebot.wrapper.PlatformWrapper;
import com.monkey.ultimatebot.wrapper.WrapperTask;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

final class CoreAddonScheduledTask implements AddonTask {
    private final UltimateBot plugin;
    private final Logger logger;
    private final CoreAddonResourceScope.Execution execution;
    private final @Nullable Player player;
    private final Runnable task;
    private final long periodTicks;
    private final Consumer<AutoCloseable> release;
    private @Nullable WrapperTask activeTask;
    private boolean cancelled;
    private boolean done;

    CoreAddonScheduledTask(
            UltimateBot plugin,
            Logger logger,
            CoreAddonResourceScope.Execution execution,
            @Nullable Player player,
            Runnable task,
            long periodTicks,
            Consumer<AutoCloseable> release) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
        this.logger = Objects.requireNonNull(logger, "logger");
        this.execution = Objects.requireNonNull(execution, "execution");
        this.player = player;
        this.task = Objects.requireNonNull(task, "task");
        this.periodTicks = periodTicks;
        this.release = Objects.requireNonNull(release, "release");
    }

    synchronized void start(long delayTicks) {
        if (cancelled || done) {
            return;
        }
        PlatformWrapper scheduler = plugin.getWrapperManager().active();
        Runnable invocation = this::invoke;
                switch (execution) {
            case GLOBAL:
                activeTask = delayTicks == 0L ? scheduler.runSync(invocation) : scheduler.runSyncLater(invocation, delayTicks);
                break;
            case ENTITY:
                activeTask = delayTicks == 0L
                        ? scheduler.runEntity(Objects.requireNonNull(player, "player"), invocation)
                        : scheduler.runEntityLater(Objects.requireNonNull(player, "player"), delayTicks, invocation);
                break;
            case ASYNC:
                activeTask = delayTicks == 0L ? scheduler.runAsync(invocation) : scheduler.runAsyncLater(invocation, delayTicks);
                break;
        }
    }

    private void invoke() {
        synchronized (this) {
            activeTask = null;
            if (cancelled || done) {
                return;
            }
        }
        try {
            task.run();
        } catch (RuntimeException | LinkageError error) {
            logger.log(Level.WARNING, "Addon scheduled task failed", error);
            cancel();
            return;
        }
        boolean completed = false;
        synchronized (this) {
            if (cancelled) {
                return;
            }
            if (periodTicks == 0L) {
                done = true;
                completed = true;
            } else {
                start(periodTicks);
            }
        }
        if (completed) {
            release.accept(this);
        }
    }

    @Override
    public void cancel() {
        WrapperTask scheduled;
        synchronized (this) {
            if (cancelled || done) {
                return;
            }
            cancelled = true;
            done = true;
            scheduled = activeTask;
            activeTask = null;
        }
        if (scheduled != null) {
            scheduled.cancel();
        }
        release.accept(this);
    }

    @Override
    public synchronized boolean isCancelled() {
        return cancelled;
    }

    @Override
    public synchronized boolean isDone() {
        return done;
    }
}
