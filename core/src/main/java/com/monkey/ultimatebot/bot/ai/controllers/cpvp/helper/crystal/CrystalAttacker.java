package com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.crystal;

import com.monkey.ultimatebot.UltimateBot;
import com.monkey.ultimatebot.api.event.combat.BotExplosionType;
import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.combat.BotExplosionContext;
import com.monkey.ultimatebot.logging.UltimateBotLogging;
import org.bukkit.entity.EnderCrystal;

public class CrystalAttacker {
    private final ITrainingBot bot;

    public CrystalAttacker(ITrainingBot bot) {
        this.bot = bot;
    }

    public void updateBotPosition() {}

    public boolean attackCrystal(EnderCrystal crystal) {
        if (crystal == null || crystal.isDead() || !crystal.isValid()) return false;
        if (!bot.hasLineOfSight(crystal)) {
            return false;
        }

        try {
            return BotExplosionContext.execute(
                    bot,
                    BotExplosionType.END_CRYSTAL,
                    crystal.getLocation(),
                    shouldDamageBlocks(),
                    ignored -> {
                        bot.attackEntity(crystal);
                        bot.swingMainHand();
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
        return bot.getBrainController() != null
                && bot.getBrainController().getBotOptions().canExplosionDamageBlocks();
    }

    public boolean canAttackCrystal(EnderCrystal crystal, double crystalAttackRange) {
        if (crystal == null || crystal.isDead() || !crystal.isValid()) return false;
        double distanceSquared = bot.getLocation().distanceSquared(crystal.getLocation());
        double rangeSquared = crystalAttackRange * crystalAttackRange;
        return distanceSquared <= rangeSquared && bot.hasLineOfSight(crystal);
    }
}
