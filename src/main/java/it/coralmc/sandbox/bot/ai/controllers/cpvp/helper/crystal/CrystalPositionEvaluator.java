package it.coralmc.sandbox.bot.ai.controllers.cpvp.helper.crystal;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.Set;

public class CrystalPositionEvaluator {

    private final Player bot;

    public CrystalPositionEvaluator(Player bot) {
        this.bot = bot;
    }

    public double calculateCrystalScore(BlockPos crystalPos, Player target,
                                        Map<BlockPos, Integer> crystalCountAtPosition,
                                        double optimalDamageRange,
                                        double minCrystalDistance) {

        int crystalY = crystalPos.getY() + 1;
        int botY = bot.blockPosition().getY();
        int targetY = target.blockPosition().getY();

        if (crystalY > targetY) {
            return 0.0;
        }

        if (botY >= crystalY) {
            return 0.0;
        }

        if (crystalY <= targetY && botY < crystalY) {
            Vec3 targetPos = target.position();
            Vec3 crystalCenter = Vec3.atCenterOf(crystalPos.above());
            double distanceToTarget = crystalCenter.distanceTo(targetPos);

            double score = Math.max(0, 10.0 - distanceToTarget);

            int yDiff = targetY - crystalY;
            if (yDiff > 0) {
                score += yDiff * 2.0;
            }

            return score;
        }

        return 0.0;
    }

    public double evaluateCrystalForAttack(EndCrystal crystal, Player target,
                                           Set<EndCrystal> myPlacedCrystals,
                                           double crystalAttackRange,
                                           double minCrystalDistance,
                                           double optimalDamageRange) {

        Vec3 crystalPos = crystal.position();
        int crystalY = (int) crystalPos.y;
        int botY = bot.blockPosition().getY();
        int targetY = target.blockPosition().getY();

        if (crystalY > targetY) {
            return 0.0;
        }

        if (botY >= crystalY) {
            return 0.0;
        }

        if (crystalY <= targetY && botY < crystalY) {
            double distanceToTarget = target.position().distanceTo(crystalPos);
            double distanceToBot = crystalPos.distanceTo(bot.position());

            double score = 1.0;

            if (distanceToTarget <= 4.0) {
                score += 2.0;
            }

            if (myPlacedCrystals.contains(crystal)) {
                score += 1.0;
            }

            return score;
        }

        return 0.0;
    }
}