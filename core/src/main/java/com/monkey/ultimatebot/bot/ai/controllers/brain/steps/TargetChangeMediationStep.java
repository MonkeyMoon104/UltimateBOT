package com.monkey.ultimatebot.bot.ai.controllers.brain.steps;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.state.BotTargetChangeEvent;
import com.monkey.ultimatebot.api.model.runtime.BotSnapshot;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import java.util.UUID;
import org.bukkit.entity.LivingEntity;
import org.jspecify.annotations.Nullable;

public final class TargetChangeMediationStep {

    private final UltimateBot plugin;
    private final ITrainingBot bot;

    public TargetChangeMediationStep(UltimateBot plugin, ITrainingBot bot) {
        this.plugin = plugin;
        this.bot = bot;
    }

    public @Nullable LivingEntity mediate(
            @Nullable LivingEntity previousTarget, @Nullable LivingEntity selectedTarget) {
        UUID ownerUUID = plugin.getBotRegistry().getOwnerUUIDByBotUUID(bot.getUniqueId());
        BotSnapshot snapshot = ownerUUID == null ? null : plugin.getBotEventDispatcher().snapshot(ownerUUID, bot);
        if (snapshot == null) {
            return selectedTarget;
        }

        BotTargetChangeEvent event = plugin.getBotEventDispatcher()
                .publish(new BotTargetChangeEvent(
                        plugin.getBotEventDispatcher().nextSequence(bot.getUniqueId()),
                        snapshot,
                        previousTarget,
                        selectedTarget));
        return event.isCancelled() ? previousTarget : event.getNewTarget();
    }
}
