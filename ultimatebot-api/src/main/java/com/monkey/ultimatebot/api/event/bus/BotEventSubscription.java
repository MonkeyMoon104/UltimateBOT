package com.monkey.ultimatebot.api.event.bus;

/** A removable EventBus listener registration. */
public interface BotEventSubscription extends AutoCloseable {
    boolean isActive();

    @Override
    void close();
}
