package com.monkey.ultimatebot.bot.ai;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.bot.BotOptions;
import com.monkey.ultimatebot.bot.ai.controllers.brain.BotBrainController;
import com.monkey.ultimatebot.bot.ai.services.BotDeathService;
import com.monkey.ultimatebot.bot.ai.services.BotEquipmentService;
import com.monkey.ultimatebot.bot.ai.services.TotemTrackerService;
import com.monkey.ultimatebot.utils.armor.PlayerOptions;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jspecify.annotations.Nullable;

public class TrainingBotLogic {

    private final ITrainingBot bot;
    private final BotBrainController brainController;
    private final TotemTrackerService totemTracker;
    private final BotDeathService deathHandler;
    private final BotEquipmentService equipmentHandler;
    private final BotOptions botOptions;

    public TrainingBotLogic(
            ITrainingBot bot,
            UltimateBot plugin,
            org.bukkit.entity.Player targetPlayer,
            boolean follow,
            BotOptions botOptions,
            String deadBotMessage,
            String deadBotEventMessage) {

        java.util.Objects.requireNonNull(plugin, "plugin");
        java.util.Objects.requireNonNull(botOptions, "botOptions");
        PlayerOptions playerOptions = plugin.getPlayerOptions();

        this.bot = java.util.Objects.requireNonNull(bot, "bot");
        this.botOptions = botOptions;
        this.brainController = new BotBrainController(bot, plugin, targetPlayer, follow, botOptions);
        this.totemTracker = new TotemTrackerService(bot);
        this.deathHandler = new BotDeathService(bot, plugin, playerOptions, deadBotMessage, deadBotEventMessage);
        this.equipmentHandler = new BotEquipmentService(bot);
    }

    public void onTick() {
        try {
            onTickInternal();
        } finally {
            com.monkey.ultimatebot.bot.BotEquipmentPolicy.enforce(bot, botOptions);
        }
    }

    private void onTickInternal() {
        if (bot.isCombat() && !brainController.getBotAI().usesCustomBrain()) {
            if (brainController.getBotAI().getHealController().isHealing()) {
                brainController.getBotAI().getHealController().updateHealAction();
                totemTracker.onTick();
                return;
            }

            if (brainController.getBotAI().getHealController().shouldHeal()) {
                brainController.getBotAI().getHealController().handleDamageReceived();
                totemTracker.onTick();
                if (brainController.getBotAI().getHealController().isHealing()) {
                    return;
                }
            }
        }

        brainController.onTick();
        totemTracker.onTick();
    }

    public void onDeath(@Nullable LivingEntity killer) {
        deathHandler.handleDeath(killer);
    }

    public boolean onDamaged(float amount, @Nullable EntityDamageEvent event) {
        boolean result = equipmentHandler.handleDamage(amount, event);

        if (event != null && brainController.getBotAI().usesCustomBrain()) {
            brainController.getBotAI().customBrainDamaged(event);
        }

        if (bot.isCombat()
                && brainController.getBotOptions().isHealing()
                && !brainController.getBotAI().usesCustomBrain()) {
            boolean fireOrLavaDamage = isFireOrLavaDamage(event);
            if (!fireOrLavaDamage) {
                brainController.getBotAI().getMovementController().onDamageReceived();
            }
            org.bukkit.entity.Player currentTarget = bot.getTargetPlayer();

            org.bukkit.entity.Entity attacker = null;
            if (event instanceof EntityDamageByEntityEvent) {
                attacker = ((EntityDamageByEntityEvent) event).getDamager();
            }
            if (attacker instanceof org.bukkit.entity.Player
                    && currentTarget != null
                    && ((org.bukkit.entity.Player) attacker).getUniqueId().equals(currentTarget.getUniqueId())) {
                if (!fireOrLavaDamage) {
                    brainController.getBotAI().getEnderpearlController().onDamageReceived();
                }
            }

            if (brainController.getBotAI().getTeleportController().isSuffocating()) {
                brainController.getBotAI().getTeleportController().handleSuffocationDamage();
            }

            if (!brainController.getBotOptions().isHealing()) {
                brainController.getBotAI().getHealController().resetHealState();
            } else if (!brainController.getBotAI().getHealController().isHealing()) {
                brainController.getBotAI().getHealController().handleDamageReceived();
            } else {
                if (!fireOrLavaDamage) {
                    brainController.getBotAI().getMovementController().setUnderFire(true);
                    brainController.getBotAI().getMovementController().emergencyEvade();
                }
            }

            if (currentTarget != null
                    && brainController.getBotAI().getMovementController().isStuckInPlace()) {
                brainController.getBotAI().getMovementController().forceUnstick(currentTarget);
            }
        }

        return result;
    }

    public void onDamaged(EntityDamageEvent event) {
        onDamaged((float) event.getFinalDamage(), event);
    }

    private boolean isFireOrLavaDamage(@Nullable EntityDamageEvent event) {
        // Fake-player NMS often calls actuallyHurt before Bukkit sets lastDamageCause.
        if (event == null) {
            return false;
        }
        switch (event.getCause()) {
            case FIRE:
            case FIRE_TICK:
            case LAVA:
            case HOT_FLOOR:
                return true;
            default:
                return false;
        }
    }

    public BotBrainController getBrainController() {
        return brainController;
    }

    public TotemTrackerService getTotemTracker() {
        return totemTracker;
    }
}
