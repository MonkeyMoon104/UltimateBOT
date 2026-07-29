package com.monkey.mcbot.api.event;

import org.bukkit.event.EventException;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.UUID;

/**
 * Typed subscription facade backed by Bukkit's event system.
 * Events received here are the same instances delivered to {@code @EventHandler} methods.
 */
public final class BotEventBus {

    public <E extends BotEvent> BotEventSubscription subscribeForBot(
            Plugin owner, UUID botUUID, Class<E> eventType, Consumer<? super E> consumer) {
        Objects.requireNonNull(botUUID, "botUUID");
        return subscribe(owner, eventType, event -> {
            if (botUUID.equals(event.getBotUUID())) consumer.accept(event);
        });
    }

    public <E extends BotEvent> BotEventSubscription subscribeForOwner(
            Plugin owner, UUID ownerUUID, Class<E> eventType, Consumer<? super E> consumer) {
        Objects.requireNonNull(ownerUUID, "ownerUUID");
        return subscribe(owner, eventType, event -> {
            if (ownerUUID.equals(event.getOwnerUUID())) consumer.accept(event);
        });
    }

    public <E extends BotEvent> BotEventSubscription subscribe(
            Plugin owner, Class<E> eventType, Consumer<? super E> consumer) {
        return subscribe(owner, eventType, EventPriority.NORMAL, false, consumer);
    }

    public <E extends BotEvent> BotEventSubscription subscribe(
            Plugin owner,
            Class<E> eventType,
            EventPriority priority,
            boolean ignoreCancelled,
            Consumer<? super E> consumer
    ) {
        Objects.requireNonNull(owner, "owner");
        Objects.requireNonNull(eventType, "eventType");
        Objects.requireNonNull(priority, "priority");
        Objects.requireNonNull(consumer, "consumer");

        Listener listener = new Listener() { };
        owner.getServer().getPluginManager().registerEvent(
                eventType,
                listener,
                priority,
                (registered, event) -> {
                    if (!eventType.isInstance(event)) {
                        return;
                    }
                    try {
                        consumer.accept(eventType.cast(event));
                    } catch (RuntimeException exception) {
                        throw new EventException(exception);
                    }
                },
                owner,
                ignoreCancelled
        );

        AtomicBoolean active = new AtomicBoolean(true);
        return new BotEventSubscription() {
            @Override
            public boolean isActive() {
                return active.get();
            }

            @Override
            public void close() {
                if (active.compareAndSet(true, false)) {
                    HandlerList.unregisterAll(listener);
                }
            }
        };
    }
}
