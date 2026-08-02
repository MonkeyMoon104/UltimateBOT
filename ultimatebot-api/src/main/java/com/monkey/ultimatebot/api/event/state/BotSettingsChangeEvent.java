package com.monkey.ultimatebot.api.event.state;

import com.monkey.ultimatebot.api.event.base.BotEvent;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.model.BotSnapshot;
import java.util.Objects;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.Nullable;

/** Fired before one runtime setting is changed. Cancelling prevents the update. */
public final class BotSettingsChangeEvent extends BotEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final BotSettingKey setting;
    private final @Nullable Object oldValue;
    private @Nullable Object newValue;
    private boolean cancelled;

    public BotSettingsChangeEvent(
            long sequence,
            BotSnapshot snapshot,
            BotEventSource source,
            BotSettingKey setting,
            @Nullable Object oldValue,
            @Nullable Object newValue) {
        super(sequence, snapshot.ownerUUID(), snapshot.requireBotUUID(), source, snapshot);
        this.setting = Objects.requireNonNull(setting, "setting");
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public BotSettingKey getSetting() {
        return setting;
    }

    public @Nullable Object getOldValue() {
        return oldValue;
    }

    public @Nullable Object getNewValue() {
        return newValue;
    }

    public void setNewValue(@Nullable Object newValue) {
        this.newValue = newValue;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
