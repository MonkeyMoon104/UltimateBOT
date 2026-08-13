package com.monkey.ultimatebot.bot.ai.controllers.cpvp.helper.crystal;

import java.util.stream.Collectors;

import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyProfileFactory;
import com.monkey.ultimatebot.bot.ai.difficulty.configs.CPVPConfig;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.util.BlockVector;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("NullAway")
public class CrystalManager {

    private CPVPConfig config = DifficultyProfileFactory.buildCPVPConfig(DifficultyLevel.NORMAL);

    public void cleanupCrystalCounts(Map<BlockVector, Integer> crystalCountAtPosition, World world) {
        crystalCountAtPosition.entrySet().removeIf(entry -> {
            BlockVector pos = entry.getKey();
            Material type = world.getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()).getType();
            if (type != Material.OBSIDIAN && type != Material.BEDROCK) {
                return true;
            }
            return entry.getValue() <= 0;
        });
    }

    public void cleanupObsidianCache(Map<BlockVector, Long> obsidianCache) {
        long currentTime = System.currentTimeMillis();
        obsidianCache.entrySet().removeIf(entry -> currentTime - entry.getValue() > config.getObsidianCacheMs());
    }

    public @Nullable EnderCrystal findCrystalAt(BlockVector pos, World world) {
        return world.getNearbyEntities(
                        new org.bukkit.Location(world, pos.getBlockX() + 0.5D, pos.getBlockY() + 0.5D, pos.getBlockZ() + 0.5D),
                        1.5D,
                        1.5D,
                        1.5D,
                        EnderCrystal.class::isInstance)
                .stream()
                .map(EnderCrystal.class::cast)
                .findFirst()
                .orElse(null);
    }

    public List<EnderCrystal> findNearbyCrystals(Player bot, World world, double crystalAttackRange) {
        return world.getNearbyEntities(bot.getLocation(), crystalAttackRange, crystalAttackRange, crystalAttackRange, EnderCrystal.class::isInstance)
                .stream()
                .map(EnderCrystal.class::cast)
                .collect(Collectors.toList());
    }

    public List<BlockVector> getValidCrystalPositions(
            Map<BlockVector, Long> obsidianCache, Player target, Player bot, World world, double maxCrystalDistance) {
        List<BlockVector> validObsidianPositions = new ArrayList<>();
        for (BlockVector obsidianPos : obsidianCache.keySet()) {
            if (isValidCrystalPos(obsidianPos, target, bot, world, maxCrystalDistance)) {
                validObsidianPositions.add(obsidianPos);
            }
        }
        return validObsidianPositions;
    }

    private boolean isValidCrystalPos(BlockVector pos, Player target, Player bot, World world, double maxCrystalDistance) {
        Material type = world.getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ()).getType();
        if (type != Material.OBSIDIAN && type != Material.BEDROCK) return false;
        if (!world.getBlockAt(pos.getBlockX(), pos.getBlockY() + 1, pos.getBlockZ()).isPassable()
                || !world.getBlockAt(pos.getBlockX(), pos.getBlockY() + 2, pos.getBlockZ()).isPassable()) return false;

        org.bukkit.Location center = new org.bukkit.Location(world, pos.getBlockX() + 0.5D, pos.getBlockY() + 1.5D, pos.getBlockZ() + 0.5D);
        double distanceToBot = bot.getLocation().distance(center);
        double distanceToTarget = target.getLocation().distance(center);
        double maxBotDistance = Math.max(maxCrystalDistance + 2.0, 8.0);

        if (distanceToBot > maxBotDistance || distanceToTarget > maxCrystalDistance) return false;

        int crystalY = pos.getBlockY() + 1;
        int botY = bot.getLocation().getBlockY();
        int targetY = target.getLocation().getBlockY();
        return crystalY <= targetY + 2 && botY <= crystalY + 2;
    }

    public void setConfig(CPVPConfig config) {
        this.config = config;
    }
}
