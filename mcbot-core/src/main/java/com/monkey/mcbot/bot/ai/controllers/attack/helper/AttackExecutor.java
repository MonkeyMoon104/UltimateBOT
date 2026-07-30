package com.monkey.mcbot.bot.ai.controllers.attack.helper;

import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.bot.ai.controllers.attack.helper.inter.IAttackExecutor;
import com.monkey.mcbot.common.guard.GuardMetadata;
import com.monkey.mcbot.nms.NMSBridgeManager;
import java.util.Objects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import org.bukkit.entity.Entity;
import org.bukkit.metadata.FixedMetadataValue;

public class AttackExecutor implements IAttackExecutor {
    private static final System.Logger LOGGER = System.getLogger(AttackExecutor.class.getName());

    public static final String BOT_FIRE_ASPECT_METADATA = GuardMetadata.FIRE_ASPECT_UNTIL;
    private static final int FIRE_ASPECT_SECONDS = 4;
    private static final long FIRE_ASPECT_METADATA_MS = 5500L;
    private static final String BOT_LAVA_DAMAGE_COOLDOWN_METADATA = "MinecraftBotLavaDamageCooldownUntil";
    private static final long LAVA_DAMAGE_COOLDOWN_MS = 3000L;
    private static final float LAVA_DAMAGE = 2.0F;
    private static final long LAVA_DAMAGE_DELAY_TICKS = 10L;

    private float cachedBaseDamage = -1;
    private Player lastDamageCalculatedBot = null;
    private net.minecraft.server.level.ServerLevel cachedServerLevel = null;
    private Player lastServerLevelBot = null;

    @Override
    public void performCriticalAttack(Player bot, LivingEntity target) {
        try {
            float baseDamage = getCachedBaseDamage(bot);
            float criticalDamage = baseDamage * 1.5f;

            net.minecraft.server.level.ServerLevel serverLevel = getCachedServerLevel(bot);

            NMSBridgeManager.get()
                    .hurtEntity(target, serverLevel, bot.damageSources().playerAttack(bot), criticalDamage);
            applyFireAspect(bot, target);
            applyLavaDamage(bot, target);

        } catch (Exception e) {
            performNormalAttack(bot, target);
        }
    }

    @Override
    public void performNormalAttack(Player bot, LivingEntity target) {
        bot.attack(target);
        applyFireAspect(bot, target);
        applyLavaDamage(bot, target);
        bot.swing(InteractionHand.MAIN_HAND);
    }

    private void applyFireAspect(Player bot, LivingEntity target) {
        if (!(bot instanceof ITrainingBot trainingBot) || target == null || target.fireImmune()) {
            return;
        }
        if (!Items.NETHERITE_SWORD.equals(bot.getMainHandItem().getItem())) {
            return;
        }

        try {
            Entity bukkitTarget = target.getBukkitEntity();
            if (bukkitTarget != null) {
                bukkitTarget.setMetadata(
                        BOT_FIRE_ASPECT_METADATA,
                        new FixedMetadataValue(
                                trainingBot.getPlugin(), System.currentTimeMillis() + FIRE_ASPECT_METADATA_MS));
            }
            target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), FIRE_ASPECT_SECONDS * 20));
        } catch (RuntimeException fireAspectError) {
            LOGGER.log(System.Logger.Level.DEBUG, "Could not apply bot fire aspect metadata", fireAspectError);
        }
    }

    private void applyLavaDamage(Player bot, LivingEntity target) {
        if (!(bot instanceof ITrainingBot trainingBot) || target == null || target.fireImmune()) {
            return;
        }
        if (!Items.NETHERITE_SWORD.equals(bot.getMainHandItem().getItem()) || bot.distanceTo(target) > 3.7D) {
            return;
        }

        try {
            Entity bukkitTarget = target.getBukkitEntity();
            long now = System.currentTimeMillis();
            if (bukkitTarget != null) {
                for (org.bukkit.metadata.MetadataValue value :
                        bukkitTarget.getMetadata(BOT_LAVA_DAMAGE_COOLDOWN_METADATA)) {
                    if (Objects.equals(value.getOwningPlugin(), trainingBot.getPlugin()) && value.asLong() > now) {
                        return;
                    }
                }
                bukkitTarget.setMetadata(
                        BOT_FIRE_ASPECT_METADATA,
                        new FixedMetadataValue(trainingBot.getPlugin(), now + FIRE_ASPECT_METADATA_MS));
                bukkitTarget.setMetadata(
                        BOT_LAVA_DAMAGE_COOLDOWN_METADATA,
                        new FixedMetadataValue(trainingBot.getPlugin(), now + LAVA_DAMAGE_COOLDOWN_MS));
            }
            scheduleLavaDamage(trainingBot, bot, target);
        } catch (RuntimeException lavaPreparationError) {
            LOGGER.log(System.Logger.Level.DEBUG, "Could not prepare bot lava damage", lavaPreparationError);
        }
    }

    private void scheduleLavaDamage(ITrainingBot trainingBot, Player bot, LivingEntity target) {
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
                                if (bot.isRemoved() || target.isRemoved() || !target.isAlive() || target.fireImmune()) {
                                    return;
                                }
                                if (bot.distanceTo(target) > 6.0D
                                        || !(target.level()
                                                instanceof net.minecraft.server.level.ServerLevel targetLevel)) {
                                    return;
                                }

                                Entity bukkitTarget = target.getBukkitEntity();
                                if (bukkitTarget != null) {
                                    bukkitTarget.setMetadata(
                                            BOT_FIRE_ASPECT_METADATA,
                                            new FixedMetadataValue(
                                                    trainingBot.getPlugin(),
                                                    System.currentTimeMillis() + FIRE_ASPECT_METADATA_MS));
                                }
                                NMSBridgeManager.get()
                                        .hurtEntity(
                                                target,
                                                targetLevel,
                                                bot.damageSources().lava(),
                                                LAVA_DAMAGE);
                            } catch (RuntimeException lavaDamageError) {
                                LOGGER.log(
                                        System.Logger.Level.DEBUG,
                                        "Could not apply delayed bot lava damage",
                                        lavaDamageError);
                            }
                        },
                        LAVA_DAMAGE_DELAY_TICKS);
    }

    private float getCachedBaseDamage(Player bot) {
        if (!Objects.equals(lastDamageCalculatedBot, bot) || cachedBaseDamage < 0) {
            cachedBaseDamage =
                    (float) bot.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            lastDamageCalculatedBot = bot;
        }
        return cachedBaseDamage;
    }

    private net.minecraft.server.level.ServerLevel getCachedServerLevel(Player bot) {
        if (!Objects.equals(lastServerLevelBot, bot) || cachedServerLevel == null) {
            cachedServerLevel = (net.minecraft.server.level.ServerLevel) bot.level();
            lastServerLevelBot = bot;
        }
        return cachedServerLevel;
    }

    public void invalidateCache() {
        cachedBaseDamage = -1;
        lastDamageCalculatedBot = null;
        cachedServerLevel = null;
        lastServerLevelBot = null;
    }
}
