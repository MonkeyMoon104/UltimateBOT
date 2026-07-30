package com.monkey.mcbot.wrapper;

import org.bukkit.entity.Player;

public interface PlatformWrapper {

    WrapperType type();

    WrapperCapabilities capabilities();

    WrapperTask runSync(Runnable task);

    WrapperTask runSyncLater(Runnable task, long delayTicks);

    WrapperTask runEntity(Player player, Runnable task);

    WrapperTask runEntityLater(Player player, long delayTicks, Runnable task);

    WrapperTask runAsync(Runnable task);

    WrapperTask runAsyncLater(Runnable task, long delayTicks);

    WrapperTask runAsyncRepeating(Runnable task, long delayTicks, long periodTicks);
}
