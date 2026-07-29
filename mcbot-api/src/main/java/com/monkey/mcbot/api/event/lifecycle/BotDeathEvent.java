package com.monkey.mcbot.api.event.lifecycle;

import com.monkey.mcbot.api.event.base.BotEvent;
import com.monkey.mcbot.api.event.base.BotEventSource;

import com.monkey.mcbot.api.model.BotSnapshot;
import org.bukkit.entity.Entity;
import org.bukkit.event.HandlerList;

/** Fired when a bot dies, before its entity and registry entry are discarded. */
public final class BotDeathEvent extends BotEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final String damageType;
    private final Entity killer;

    public BotDeathEvent(long sequence, BotSnapshot snapshot, String damageType, Entity killer) {
        super(sequence, snapshot.ownerUUID(), snapshot.botUUID(), BotEventSource.BUKKIT, snapshot);
        this.damageType = damageType == null ? "unknown" : damageType;
        this.killer = killer;
    }
    public String getDamageType() { return damageType; }
    public Entity getKiller() { return killer; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
