package com.monkey.ultimatebot.bot.ai.controllers.attack.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.attack.helper.inter.IAttackExecutor;
import com.monkey.ultimatebot.common.guard.GuardMetadata;
import com.monkey.ultimatebot.nms.DamageKind;
import com.monkey.ultimatebot.nms.NMSBridgeManager;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;

public class AttackExecutor implements IAttackExecutor {
    private static final Logger LOGGER = Logger.getLogger(AttackExecutor.class.getName());

    public static final String BOT_FIRE_ASPECT_METADATA = GuardMetadata.FIRE_ASPECT_UNTIL;
    private static final int FIRE_ASPECT_SECONDS = 4;
    private static final long FIRE_ASPECT_METADATA_MS = 5500L;
    private static final String BOT_LAVA_DAMAGE_COOLDOWN_METADATA = "UltimateBotLavaDamageCooldownUntil";
    private static final long LAVA_DAMAGE_COOLDOWN_MS = 3000L;
    private static final float LAVA_DAMAGE = 2.0F;
    private static final long LAVA_DAMAGE_DELAY_TICKS = 10L;

    @Override
    public void performCriticalAttack(ITrainingBot bot, LivingEntity target) {
        performNormalAttack(bot, target);
    }

    @Override
    public void performNormalAttack(ITrainingBot bot, LivingEntity target) {
        bot.attackEntity(target);
        applyFireAspect(bot, target);
        applyLavaDamage(bot, target);
        bot.swingMainHand();
    }

    private void applyFireAspect(ITrainingBot bot, LivingEntity target) {
        if (target == null) {
            return;
        }
        if (bot.getItemInMainHand().getType() != Material.NETHERITE_SWORD) {
            return;
        }

        try {
            Entity bukkitTarget = target;
            if (bukkitTarget != null) {
                bukkitTarget.setMetadata(
                        BOT_FIRE_ASPECT_METADATA,
                        new FixedMetadataValue(
                                bot.getPlugin(), System.currentTimeMillis() + FIRE_ASPECT_METADATA_MS));
            }
            target.setFireTicks(Math.max(target.getFireTicks(), FIRE_ASPECT_SECONDS * 20));
        } catch (RuntimeException fireAspectError) {
            LOGGER.log(Level.FINE, "Could not apply bot fire aspect metadata", fireAspectError);
        }
    }

    private void applyLavaDamage(ITrainingBot bot, LivingEntity target) {
        if (target == null) {
            return;
        }
        if (bot.getItemInMainHand().getType() != Material.NETHERITE_SWORD || bot.distanceTo(target) > 3.7D) {
            return;
        }

        try {
            Entity bukkitTarget = target;
            long now = System.currentTimeMillis();
            if (bukkitTarget != null) {
                for (org.bukkit.metadata.MetadataValue value :
                        bukkitTarget.getMetadata(BOT_LAVA_DAMAGE_COOLDOWN_METADATA)) {
                    if (Objects.equals(value.getOwningPlugin(), bot.getPlugin()) && value.asLong() > now) {
                        return;
                    }
                }
                bukkitTarget.setMetadata(
                        BOT_FIRE_ASPECT_METADATA,
                        new FixedMetadataValue(bot.getPlugin(), now + FIRE_ASPECT_METADATA_MS));
                bukkitTarget.setMetadata(
                        BOT_LAVA_DAMAGE_COOLDOWN_METADATA,
                        new FixedMetadataValue(bot.getPlugin(), now + LAVA_DAMAGE_COOLDOWN_MS));
            }
            scheduleLavaDamage(bot, target);
        } catch (RuntimeException lavaPreparationError) {
            LOGGER.log(Level.FINE, "Could not prepare bot lava damage", lavaPreparationError);
        }
    }

    private void scheduleLavaDamage(ITrainingBot bot, LivingEntity target) {
        ITrainingBot trainingBot = bot;
        if (trainingBot.getPlugin().getWrapperManager() == null) {
            return;
        }

        trainingBot
                .getPlugin()
                .getWrapperManager()
                .active()
                .runSyncLater(
                        () -> {
                            try {
                                if (bot.isRemoved() || target.isDead() || !target.isValid()) {
                                    return;
                                }
                                if (bot.distanceTo(target) > 6.0D) {
                                    return;
                                }

                                Entity bukkitTarget = target;
                                if (bukkitTarget != null) {
                                    bukkitTarget.setMetadata(
                                            BOT_FIRE_ASPECT_METADATA,
                                            new FixedMetadataValue(
                                                    trainingBot.getPlugin(),
                                                    System.currentTimeMillis() + FIRE_ASPECT_METADATA_MS));
                                }
                                NMSBridgeManager.get()
                                        .hurt(
                                                target,
                                                bot.asBukkitPlayer(),
                                                LAVA_DAMAGE,
                                                DamageKind.LAVA);
                            } catch (RuntimeException lavaDamageError) {
                                LOGGER.log(
                                        Level.FINE,
                                        "Could not apply delayed bot lava damage",
                                        lavaDamageError);
                            }
                        },
                        LAVA_DAMAGE_DELAY_TICKS);
    }
}
