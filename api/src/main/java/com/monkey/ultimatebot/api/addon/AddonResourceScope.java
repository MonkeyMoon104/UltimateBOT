package com.monkey.ultimatebot.api.addon;

import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

/** Tracks addon-owned resources and releases them in reverse registration order. */
public interface AddonResourceScope extends AutoCloseable {
    <T extends AutoCloseable> T own(T resource);

    Listener registerListener(Listener listener);

    AddonTask runTask(Runnable task);

    AddonTask runTaskLater(Runnable task, long delayTicks);

    AddonTask runTaskTimer(Runnable task, long delayTicks, long periodTicks);

    AddonTask runEntityTask(Player player, Runnable task);

    AddonTask runEntityTaskLater(Player player, Runnable task, long delayTicks);

    AddonTask runAsyncTask(Runnable task);

    AddonTask runAsyncTaskLater(Runnable task, long delayTicks);

    AddonTask runAsyncTaskTimer(Runnable task, long delayTicks, long periodTicks);

    boolean isClosed();

    @Override
    void close();
}
