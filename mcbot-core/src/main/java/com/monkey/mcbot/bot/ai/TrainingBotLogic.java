package com.monkey.mcbot.bot.ai;

import com.monkey.mcbot.MinecraftBot;
import com.monkey.mcbot.bot.BotOptions;
import com.monkey.mcbot.bot.ai.controllers.brain.BotBrainController;
import com.monkey.mcbot.bot.ai.services.BotDeathService;
import com.monkey.mcbot.bot.ai.services.BotEquipmentService;
import com.monkey.mcbot.bot.ai.services.TotemTrackerService;
import com.monkey.mcbot.utils.armor.PlayerOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.event.entity.EntityDamageEvent;

public class TrainingBotLogic {

    private final ITrainingBot bot;
    private final BotBrainController brainController;
    private final TotemTrackerService totemTracker;
    private final BotDeathService deathHandler;
    private final BotEquipmentService equipmentHandler;

    public TrainingBotLogic(
            ITrainingBot bot,
            MinecraftBot plugin,
            org.bukkit.entity.Player targetPlayer,
            boolean follow,
            BotOptions botOptions,
            String deadBotMessage,
            String deadBotEventMessage) {

        java.util.Objects.requireNonNull(plugin, "plugin");
        java.util.Objects.requireNonNull(botOptions, "botOptions");
        PlayerOptions playerOptions = plugin.getPlayerOptions();

        this.bot = java.util.Objects.requireNonNull(bot, "bot");
        this.brainController = new BotBrainController(bot, plugin, targetPlayer, follow, botOptions);
        this.totemTracker = new TotemTrackerService(bot);
        this.deathHandler = new BotDeathService(bot, plugin, playerOptions, deadBotMessage, deadBotEventMessage);
        this.equipmentHandler = new BotEquipmentService(bot);
    }

    public void onTick() {
        if (bot.isCombat()) {
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

    public void onDeath(DamageSource cause) {
        deathHandler.handleDeath(cause);
    }

    public boolean onActuallyHurt(ServerLevel level, DamageSource source, float amount, EntityDamageEvent event) {

        boolean result = equipmentHandler.handleDamage(level, source, amount, event);

        if (bot.isCombat() && brainController.getBotOptions().isHealing()) {
            boolean fireOrLavaDamage = isFireOrLavaDamage(source);
            if (!fireOrLavaDamage) {
                brainController.getBotAI().getMovementController().onDamageReceived();
            }
            org.bukkit.entity.Player currentTarget = bot.getTargetPlayer();

            net.minecraft.world.entity.Entity attacker = source.getEntity();
            if (attacker instanceof Player nmsPlayer
                    && currentTarget != null
                    && nmsPlayer.getUUID().equals(currentTarget.getUniqueId())) {
                if (!fireOrLavaDamage) {
                    if (source.isCritical()) {
                        brainController.getBotAI().getEnderpearlController().onDamageReceived();
                    }
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
                brainController
                        .getBotAI()
                        .getMovementController()
                        .forceUnstick(((CraftPlayer) currentTarget).getHandle());
            }
        }

        return result;
    }

    private boolean isFireOrLavaDamage(DamageSource source) {
        String msgId = source.getMsgId();
        return "inFire".equals(msgId) || "onFire".equals(msgId) || "lava".equals(msgId);
    }

    public BotBrainController getBrainController() {
        return brainController;
    }

    public TotemTrackerService getTotemTracker() {
        return totemTracker;
    }
}
