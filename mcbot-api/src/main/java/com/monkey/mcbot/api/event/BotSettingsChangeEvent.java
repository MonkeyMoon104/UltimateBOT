package com.monkey.mcbot.api.event;

import com.monkey.mcbot.api.model.BotSnapshot;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import java.util.Objects;

/** Fired before one runtime setting is changed. Cancelling prevents the update. */
public final class BotSettingsChangeEvent extends BotEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final BotSettingKey setting;
    private final Object oldValue;
    private Object newValue;
    private boolean cancelled;

    public BotSettingsChangeEvent(long sequence, BotSnapshot snapshot, BotEventSource source,
                                  BotSettingKey setting, Object oldValue, Object newValue) {
        super(sequence, snapshot.ownerUUID(), snapshot.botUUID(), source, snapshot);
        this.setting = Objects.requireNonNull(setting, "setting");
        this.oldValue = oldValue;
        this.newValue = newValue;
    }
    public BotSettingKey getSetting() { return setting; }
    public Object getOldValue() { return oldValue; }
    public Object getNewValue() { return newValue; }
    public void setNewValue(Object newValue) { this.newValue = newValue; }
    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
