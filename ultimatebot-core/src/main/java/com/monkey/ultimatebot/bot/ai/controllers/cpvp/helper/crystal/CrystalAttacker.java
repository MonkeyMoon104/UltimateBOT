package com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.crystal;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.combat.BotExplosionType;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.combat.BotExplosionContext;
import com.monkey.ultimatebot.logging.UltimateBotLogging;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class CrystalAttacker {
    private final Player bot;
    private Vec3 cachedBotPosition;
    private boolean positionUpdatedThisTick = false;

    public CrystalAttacker(Player bot) {
        this.bot = bot;
    }

    public void updateBotPosition() {
        cachedBotPosition = bot.position();
        positionUpdatedThisTick = true;
    }

    public boolean attackCrystal(EndCrystal crystal) {
        if (crystal == null || !crystal.isAlive()) return false;

        if (!positionUpdatedThisTick) {
            cachedBotPosition = bot.position();
        }
        positionUpdatedThisTick = false;

        if (!bot.hasLineOfSight(crystal)) {
            return false;
        }

        try {
            ITrainingBot trainingBot = bot instanceof ITrainingBot value ? value : null;
            return BotExplosionContext.execute(
                    trainingBot,
                    BotExplosionType.END_CRYSTAL,
                    crystal.getBukkitEntity().getLocation(),
                    shouldDamageBlocks(),
                    ignored -> {
                        bot.attack(crystal);
                        bot.swing(InteractionHand.MAIN_HAND);
                        return true;
                    },
                    false);
        } catch (Exception e) {
            UltimateBotLogging.warn(
                    UltimateBot.getInstance().getLogger(), "Combat", "Crystal attack failed -> " + e.getMessage());
            return false;
        }
    }

    private boolean shouldDamageBlocks() {
        if (!(bot instanceof ITrainingBot trainingBot) || trainingBot.getBrainController() == null) {
            return false;
        }
        return trainingBot.getBrainController().getBotOptions().canExplosionDamageBlocks();
    }

    public boolean canAttackCrystal(EndCrystal crystal, double crystalAttackRange) {
        if (crystal == null || !crystal.isAlive()) return false;

        Vec3 botPos = getCachedBotPosition();
        double distanceSquared = botPos.distanceToSqr(crystal.position());
        double rangeSquared = crystalAttackRange * crystalAttackRange;

        return distanceSquared <= rangeSquared && bot.hasLineOfSight(crystal);
    }

    private Vec3 getCachedBotPosition() {
        if (cachedBotPosition == null) {
            cachedBotPosition = bot.position();
        }
        return cachedBotPosition;
    }
}
