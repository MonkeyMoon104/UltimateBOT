package com.monkey.mcbot.api.event;

import org.bukkit.Location;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import java.util.Objects;
import java.util.UUID;

/** Fired before a bot entity is created. Cancelling leaves any existing bot untouched. */
public final class BotSpawnPrepareEvent extends BotEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private Location spawnLocation;
    private boolean cancelled;

    public BotSpawnPrepareEvent(long sequence, UUID ownerUUID, UUID botUUID,
                                BotEventSource source, Location spawnLocation) {
        super(sequence, ownerUUID, botUUID, source, null);
        this.spawnLocation = Objects.requireNonNull(spawnLocation, "spawnLocation").clone();
    }
    public Location getSpawnLocation() { return spawnLocation.clone(); }
    public void setSpawnLocation(Location spawnLocation) {
        this.spawnLocation = Objects.requireNonNull(spawnLocation, "spawnLocation").clone();
    }
    @Override public boolean isCancelled() { return cancelled; }
    @Override public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
