package com.monkey.mcbot.api.event.combat;

import com.monkey.mcbot.api.model.BotSnapshot;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Objects;
import java.util.UUID;

/**
 * Bukkit event fired when a MinecraftBot kills a player.
 *
 * <p>Reward plugins can listen to this event to grant money, points or items
 * to the owner/team that controls the bot.</p>
 */
public final class BotKillPlayerEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    private final UUID ownerUUID;
    private final UUID botUUID;
    private final Player victim;
    private final BotSnapshot botSnapshot;

    /**
     * Creates a bot kill event.
     *
     * @param ownerUUID owner UUID associated with the bot slot
     * @param botUUID runtime bot entity UUID
     * @param victim player killed by the bot
     * @param botSnapshot current bot snapshot
     */
    public BotKillPlayerEvent(UUID ownerUUID, UUID botUUID, Player victim, BotSnapshot botSnapshot) {
        this.ownerUUID = Objects.requireNonNull(ownerUUID, "ownerUUID");
        this.botUUID = Objects.requireNonNull(botUUID, "botUUID");
        this.victim = Objects.requireNonNull(victim, "victim");
        this.botSnapshot = botSnapshot;
    }

    public UUID getOwnerUUID() {
        return ownerUUID;
    }

    public UUID getBotUUID() {
        return botUUID;
    }

    public Player getVictim() {
        return victim;
    }

    public BotSnapshot getBotSnapshot() {
        return botSnapshot;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
