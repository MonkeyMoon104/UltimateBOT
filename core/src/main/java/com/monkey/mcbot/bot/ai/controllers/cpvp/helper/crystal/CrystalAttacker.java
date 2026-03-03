package com.monkey.mcbot.bot.ai.controllers.cpvp.helper.crystal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CrystalAttacker {
    private final Player bot;
    private final Level level;
    private Vec3 cachedBotPosition;
    private boolean positionUpdatedThisTick = false;

    public CrystalAttacker(Player bot, Level level) {
        this.bot = bot;
        this.level = level;
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
            bot.attack(crystal);
            bot.swing(InteractionHand.MAIN_HAND);
            return true;
        } catch (Exception e) {
            System.err.println("Errore nell'attacco al crystal: " + e.getMessage());
            return false;
        }
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