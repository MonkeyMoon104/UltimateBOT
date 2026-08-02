package com.monkey.ultimatebot.api.event.combat;

import com.monkey.ultimatebot.api.event.base.BotEvent;
import com.monkey.ultimatebot.api.event.base.BotEventSource;
import com.monkey.ultimatebot.api.model.BotSnapshot;
import java.util.Objects;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;

/** Fired immediately before a bot executes a combat attack. */
public final class BotAttackEvent extends BotEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final LivingEntity target;
    private final BotAttackType attackType;
    private boolean cancelled;

    public BotAttackEvent(long sequence, BotSnapshot snapshot, LivingEntity target, BotAttackType attackType) {
        super(sequence, snapshot.ownerUUID(), snapshot.requireBotUUID(), BotEventSource.BOT_AI, snapshot);
        this.target = Objects.requireNonNull(target, "target");
        this.attackType = Objects.requireNonNull(attackType, "attackType");
    }

    public LivingEntity getTarget() {
        return target;
    }

    public BotAttackType getAttackType() {
        return attackType;
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
