package com.monkey.mcbot.sdk.event;

/** A reconnecting remote event stream subscription. */
public interface BotEventSubscription extends AutoCloseable {
    boolean isActive();
    long getLastEventId();
    @Override void close();
}
