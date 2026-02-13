package com.monkey.mcbot.bot.ai.controllers.cpvp.helper.crystal;

import com.monkey.mcbot.bot.ai.rank.configs.CPVPConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CrystalManager {

    private CPVPConfig config;

    public void cleanupCrystalCounts(Map<BlockPos, Integer> crystalCountAtPosition, Level level) {
        crystalCountAtPosition.entrySet().removeIf(entry -> {
            BlockPos pos = entry.getKey();
            return findCrystalAt(pos.above(), level) == null;
        });
    }

    public void cleanupObsidianCache(Map<BlockPos, Long> obsidianCache) {
        long currentTime = System.currentTimeMillis();
        obsidianCache.entrySet().removeIf(entry ->
                currentTime - entry.getValue() > config.getObsidianCacheMs());
    }

    public EndCrystal findCrystalAt(BlockPos pos, Level level) {
        return level.getEntitiesOfClass(EndCrystal.class,
                        new AABB(pos).inflate(1.5))
                .stream()
                .findFirst()
                .orElse(null);
    }

    public List<EndCrystal> findNearbyCrystals(Player bot, Level level, double crystalAttackRange) {
        return level.getEntitiesOfClass(EndCrystal.class,
                new AABB(bot.blockPosition()).inflate(crystalAttackRange));
    }

    public List<BlockPos> getValidCrystalPositions(Map<BlockPos, Long> obsidianCache,
                                                   Player target, Player bot, Level level,
                                                   double maxCrystalDistance) {
        List<BlockPos> validObsidianPositions = new ArrayList<>();
        for (BlockPos obsidianPos : obsidianCache.keySet()) {
            if (isValidCrystalPos(obsidianPos, target, bot, level, maxCrystalDistance)) {
                validObsidianPositions.add(obsidianPos);
            }
        }
        return validObsidianPositions;
    }

    private boolean isValidCrystalPos(BlockPos pos, Player target, Player bot, Level level, double maxCrystalDistance) {
        BlockState state = level.getBlockState(pos);
        if (!(state.is(Blocks.OBSIDIAN) || state.is(Blocks.BEDROCK))) return false;
        if (!level.getBlockState(pos.above()).isAir() || !level.getBlockState(pos.above(2)).isAir()) return false;

        double distanceToBot = bot.position().distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(pos));
        double distanceToTarget = target.position().distanceTo(net.minecraft.world.phys.Vec3.atCenterOf(pos.above()));

        if (distanceToBot > 10 || distanceToTarget > maxCrystalDistance) return false;

        int crystalY = pos.getY() + 1;
        int botY = bot.blockPosition().getY();
        int targetY = target.blockPosition().getY();

        if (crystalY > targetY) {
            return false;
        }

        if (botY >= crystalY) {
            return false;
        }

        return crystalY <= targetY && botY < crystalY;
    }

    public void setConfig(CPVPConfig config) {
        this.config = config;
    }
}