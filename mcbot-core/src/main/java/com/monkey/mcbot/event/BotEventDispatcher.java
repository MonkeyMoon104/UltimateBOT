package com.monkey.mcbot.event;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.api.event.base.BotEvent;
import com.monkey.mcbot.api.model.BotSnapshot;
import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.integration.api.BotSnapshotMapper;
import com.monkey.mcbot.metrics.BotMetrics;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

/** Single core dispatch point for Bukkit events and remote event observers. */
public final class BotEventDispatcher {
    private final MinecraftBot plugin;
    private final BotMetrics metrics;
    private final ConcurrentHashMap<UUID, AtomicLong> sequences = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<Consumer<BotEvent>> observers = new CopyOnWriteArrayList<>();

    public BotEventDispatcher(MinecraftBot plugin, BotMetrics metrics) {
        this.plugin = java.util.Objects.requireNonNull(plugin, "plugin");
        this.metrics = java.util.Objects.requireNonNull(metrics, "metrics");
    }

    public long nextSequence(UUID botUUID) {
        return sequences.computeIfAbsent(botUUID, ignored -> new AtomicLong()).incrementAndGet();
    }

    public BotSnapshot snapshot(UUID ownerUUID, ITrainingBot bot) {
        return BotSnapshotMapper.toSnapshot(ownerUUID, bot);
    }

    public <E extends BotEvent> E publish(E event) {
        plugin.getServer().getPluginManager().callEvent(event);
        metrics.recordEvent(event);
        if (!(event instanceof org.bukkit.event.Cancellable cancellable) || !cancellable.isCancelled()) {
            for (Consumer<BotEvent> observer : observers) {
                try {
                    observer.accept(event);
                } catch (RuntimeException exception) {
                    metrics.recordObserverFailure();
                    plugin.getLogger().warning("Bot event observer failed: " + exception.getMessage());
                }
            }
        }
        return event;
    }

    public AutoCloseable observe(Consumer<BotEvent> observer) {
        observers.add(observer);
        return () -> observers.remove(observer);
    }

    public void forget(UUID botUUID) {
        if (botUUID != null) {
            sequences.remove(botUUID);
        }
    }

    public void clear() {
        sequences.clear();
        observers.clear();
    }
}
