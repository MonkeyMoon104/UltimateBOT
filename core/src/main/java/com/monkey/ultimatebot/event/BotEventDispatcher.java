package com.monkey.ultimatebot.event;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.base.BotEvent;
import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.integration.api.BotSnapshotMapper;
import com.monkey.ultimatebot.metrics.BotMetrics;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.jspecify.annotations.Nullable;

public final class BotEventDispatcher {
    private final UltimateBot plugin;
    private final BotMetrics metrics;
    private final ConcurrentHashMap<UUID, AtomicLong> sequences = new ConcurrentHashMap<>();
    private final CopyOnWriteArrayList<Consumer<BotEvent>> observers = new CopyOnWriteArrayList<>();

    public BotEventDispatcher(UltimateBot plugin, BotMetrics metrics) {
        this.plugin = java.util.Objects.requireNonNull(plugin, "plugin");
        this.metrics = java.util.Objects.requireNonNull(metrics, "metrics");
    }

    public long nextSequence(UUID botUUID) {
        return sequences.computeIfAbsent(botUUID, ignored -> new AtomicLong()).incrementAndGet();
    }

    public @Nullable BotSnapshot snapshot(UUID ownerUUID, @Nullable ITrainingBot bot) {
        return BotSnapshotMapper.toSnapshot(ownerUUID, bot);
    }

    public <E extends BotEvent> E publish(E event) {
        plugin.getServer().getPluginManager().callEvent(event);
        metrics.recordEvent(event);
        boolean cancelled =
                event instanceof org.bukkit.event.Cancellable && ((org.bukkit.event.Cancellable) event).isCancelled();
        if (!cancelled) {
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
