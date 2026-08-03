package com.monkey.ultimatebot.api.event.action;

import com.monkey.ultimatebot.api.event.base.BotEvent;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.Nullable;

/** Fired when a bot is about to regain health. */
public final class BotHealEvent extends BotEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final String reason;
    private double amount;
    private boolean cancelled;

    public BotHealEvent(long sequence, BotSnapshot snapshot, @Nullable String reason, double amount) {
        super(sequence, snapshot.ownerUUID(), snapshot.requireBotUUID(), BotEventSource.BUKKIT, snapshot);
        this.reason = reason == null ? "UNKNOWN" : reason;
        this.amount = Math.max(0.0D, amount);
    }

    public String getReason() {
        return reason;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = Math.max(0.0D, amount);
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
