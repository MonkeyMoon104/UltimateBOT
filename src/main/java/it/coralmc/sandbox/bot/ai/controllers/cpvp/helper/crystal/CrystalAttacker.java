package it.coralmc.sandbox.bot.ai.controllers.cpvp.helper.crystal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class CrystalAttacker {

    private final Player bot;
    private final Level level;
    private Vec3 cachedBotPosition;
    private long lastBotPositionUpdate = 0;
    private static final long POSITION_CACHE_MS = 50;

    public CrystalAttacker(Player bot, Level level) {
        this.bot = bot;
        this.level = level;
    }

    public boolean attackCrystal(EndCrystal crystal, double crystalAttackRange) {
        if (crystal == null || !crystal.isAlive()) return false;

        Vec3 botPos = getCachedBotPosition();
        Vec3 crystalPos = crystal.position();

        double distanceSquared = botPos.distanceToSqr(crystalPos);
        double rangeSquared = crystalAttackRange * crystalAttackRange;

        if (distanceSquared > rangeSquared) return false;

        try {
            bot.attack(crystal);
            bot.swing(InteractionHand.MAIN_HAND);
            return true;
        } catch (Exception e) {
            System.err.println("Errore nell'attacco al crystal: " + e.getMessage());
            return false;
        }
    }

    private Vec3 getCachedBotPosition() {
        long currentTime = System.currentTimeMillis();
        if (cachedBotPosition == null || currentTime - lastBotPositionUpdate > POSITION_CACHE_MS) {
            cachedBotPosition = bot.position();
            lastBotPositionUpdate = currentTime;
        }
        return cachedBotPosition;
    }

    public boolean canAttackCrystal(EndCrystal crystal, double crystalAttackRange) {
        if (crystal == null || !crystal.isAlive()) return false;

        Vec3 botPos = getCachedBotPosition();
        double distanceSquared = botPos.distanceToSqr(crystal.position());
        double rangeSquared = crystalAttackRange * crystalAttackRange;

        return distanceSquared <= rangeSquared;
    }
}