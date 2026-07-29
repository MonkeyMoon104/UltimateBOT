package com.monkey.mcbot.api.event.state;

import com.monkey.mcbot.api.event.base.BotEvent;
import com.monkey.mcbot.api.event.base.BotEventSource;
import com.monkey.mcbot.api.model.BotSnapshot;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.jspecify.annotations.Nullable;

/** Fired before the AI changes its active target. */
public final class BotTargetChangeEvent extends BotEvent implements Cancellable {
    private static final HandlerList HANDLERS = new HandlerList();
    private final @Nullable LivingEntity previousTarget;
    private @Nullable LivingEntity newTarget;
    private boolean cancelled;

    public BotTargetChangeEvent(
            long sequence,
            BotSnapshot snapshot,
            @Nullable LivingEntity previousTarget,
            @Nullable LivingEntity newTarget) {
        super(sequence, snapshot.ownerUUID(), snapshot.requireBotUUID(), BotEventSource.BOT_AI, snapshot);
        this.previousTarget = previousTarget;
        this.newTarget = newTarget;
    }

    public @Nullable LivingEntity getPreviousTarget() {
        return previousTarget;
    }

    public @Nullable LivingEntity getNewTarget() {
        return newTarget;
    }

    public void setNewTarget(@Nullable LivingEntity newTarget) {
        this.newTarget = newTarget;
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
