package com.monkey.mcbot.api.event;

import com.monkey.mcbot.api.model.BotSnapshot;
import org.bukkit.entity.Entity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/** Fired when a bot is about to receive Bukkit damage. */
public final class BotDamageEvent extends BotEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final Entity damager;
    private final String cause;
    private double damage;
    private boolean cancelled;

    public BotDamageEvent(long sequence, BotSnapshot snapshot, Entity damager, String cause, double damage) {
        super(sequence, snapshot.ownerUUID(), snapshot.botUUID(), BotEventSource.BUKKIT, snapshot);
        this.damager = damager;
        this.cause = cause == null ? "UNKNOWN" : cause;
        this.damage = Math.max(0.0D, damage);
    }
    public Entity getDamager() { return damager; }
    public String getCause() { return cause; }
    public double getDamage() { return damage; }
    public void setDamage(double damage) { this.damage = Math.max(0.0D, damage); }
    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
