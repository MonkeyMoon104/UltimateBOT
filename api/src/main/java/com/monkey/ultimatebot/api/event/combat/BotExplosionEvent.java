package com.monkey.ultimatebot.api.event.combat;

import com.monkey.ultimatebot.api.event.base.BotEvent;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import java.util.Objects;
import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/** Fired before Bukkit applies a bot-created explosion. */
public final class BotExplosionEvent extends BotEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final BotExplosionType explosionType;
    private final Location location;
    private boolean blockDamage;
    private boolean cancelled;

    public BotExplosionEvent(
            long sequence,
            BotSnapshot snapshot,
            BotExplosionType explosionType,
            Location location,
            boolean blockDamage) {
        super(sequence, snapshot.ownerUUID(), snapshot.requireBotUUID(), BotEventSource.BOT_AI, snapshot);
        this.explosionType = Objects.requireNonNull(explosionType, "explosionType");
        this.location = Objects.requireNonNull(location, "location").clone();
        this.blockDamage = blockDamage;
    }

    public BotExplosionType getExplosionType() {
        return explosionType;
    }

    public Location getLocation() {
        return location.clone();
    }

    public boolean isBlockDamage() {
        return blockDamage;
    }

    public void setBlockDamage(boolean blockDamage) {
        this.blockDamage = blockDamage;
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
