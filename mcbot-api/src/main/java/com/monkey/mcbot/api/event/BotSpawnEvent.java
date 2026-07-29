package com.monkey.mcbot.api.event;

import com.monkey.mcbot.api.model.BotSnapshot;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.HandlerList;
import java.util.Objects;

/** Fired after a bot is spawned and registered. */
public final class BotSpawnEvent extends BotEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final HumanEntity bot;

    public BotSpawnEvent(long sequence, BotSnapshot snapshot, BotEventSource source, HumanEntity bot) {
        super(sequence, snapshot.ownerUUID(), snapshot.botUUID(), source, snapshot);
        this.bot = Objects.requireNonNull(bot, "bot");
    }
    public HumanEntity getBot() { return bot; }
    @Override public HandlerList getHandlers() { return HANDLERS; }
    public static HandlerList getHandlerList() { return HANDLERS; }
}
