package com.monkey.mcbot.api.event.combat;

import com.monkey.mcbot.api.event.base.BotEvent;
import com.monkey.mcbot.api.event.base.BotEventSource;
import com.monkey.mcbot.api.model.BotSnapshot;
import java.util.Objects;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.HandlerList;

/** Fired when a bot kills any living entity, including players and mobs. */
public final class BotKillEntityEvent extends BotEvent {
    private static final HandlerList HANDLERS = new HandlerList();
    private final LivingEntity victim;

    public BotKillEntityEvent(long sequence, BotSnapshot snapshot, LivingEntity victim) {
        super(sequence, snapshot.ownerUUID(), snapshot.requireBotUUID(), BotEventSource.BUKKIT, snapshot);
        this.victim = Objects.requireNonNull(victim, "victim");
    }

    public LivingEntity getVictim() {
        return victim;
    }

    public boolean isPlayerVictim() {
        return victim instanceof org.bukkit.entity.Player;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }
}
