package com.monkey.ultimatebot.bot.ai.difficulty;

import com.monkey.ultimatebot.bot.ai.difficulty.configs.CPVPConfig;
import com.monkey.ultimatebot.bot.ai.difficulty.configs.RAPVPConfig;

public class DifficultyProfileFactory {

    public static RAPVPConfig buildRAPVPConfig(DifficultyLevel difficulty) {
        switch (difficulty) {
            case EASY:
                return new RAPVPConfig.Builder()
                        .maxDistance(4)
                        .predictionTicks(4)
                        .minMovement(0.01)
                        .minSafeDistance(1.2)
                        .anchorSearchCooldownMillis(700L)
                        .build();
            case NORMAL:
                return new RAPVPConfig.Builder()
                        .maxDistance(7)
                        .predictionTicks(6)
                        .minMovement(0.01)
                        .minSafeDistance(2.2)
                        .anchorSearchCooldownMillis(430L)
                        .build();
            case MEDIUM:
                return new RAPVPConfig.Builder()
                        .maxDistance(10)
                        .predictionTicks(10)
                        .minMovement(0.05)
                        .minSafeDistance(2.8)
                        .anchorSearchCooldownMillis(220L)
                        .build();
            case HARD:
                return new RAPVPConfig.Builder()
                        .maxDistance(12)
                        .predictionTicks(12)
                        .minMovement(0.01)
                        .minSafeDistance(3.0)
                        .anchorSearchCooldownMillis(120L)
                        .build();
            case GOD:
                return new RAPVPConfig.Builder()
                        .maxDistance(14)
                        .predictionTicks(12)
                        .minMovement(0.01)
                        .minSafeDistance(2.2)
                        .anchorSearchCooldownMillis(40L)
                        .build();
            default:
                return new RAPVPConfig.Builder().build();
        }
    }

    public static CPVPConfig buildCPVPConfig(DifficultyLevel difficulty) {
        switch (difficulty) {
            case EASY:
                return new CPVPConfig.Builder()
                        .maxCrystalDistance(4.3)
                        .minCrystalDistance(1.8)
                        .crystalAttackRange(4.2)
                        .optimalDamageRange(3.2)
                        .obsidianPlaceCooldownTicks(9)
                        .crystalPlaceCooldownTicks(9)
                        .attackCooldownTicks(7)
                        .obsidianPreparationTime(5)
                        .crystalPreparationTime(5)
                        .attackPreparationTime(4)
                        .positionCooldownMs(3600L)
                        .fullScanIntervalMs(2100L)
                        .positionCacheMs(3000L)
                        .maxCrystalsPerPosition(2)
                        .maxPositionsToCheck(3)
                        .minCrystalScore(7.0)
                        .minAttackScore(0.95)
                        .build();
            case NORMAL:
                return new CPVPConfig.Builder()
                        .maxCrystalDistance(6.3)
                        .minCrystalDistance(2.2)
                        .crystalAttackRange(5.8)
                        .optimalDamageRange(4.3)
                        .obsidianPlaceCooldownTicks(7)
                        .crystalPlaceCooldownTicks(7)
                        .attackCooldownTicks(5)
                        .obsidianPreparationTime(3)
                        .crystalPreparationTime(3)
                        .attackPreparationTime(2)
                        .positionCooldownMs(2000L)
                        .fullScanIntervalMs(900L)
                        .positionCacheMs(1400L)
                        .maxCrystalsPerPosition(3)
                        .maxPositionsToCheck(4)
                        .minCrystalScore(5.6)
                        .minAttackScore(0.55)
                        .build();
            case MEDIUM:
                return new CPVPConfig.Builder()
                        .maxCrystalDistance(7.3)
                        .minCrystalDistance(2.25)
                        .crystalAttackRange(6.5)
                        .optimalDamageRange(4.4)
                        .obsidianPlaceCooldownTicks(5)
                        .crystalPlaceCooldownTicks(5)
                        .attackCooldownTicks(4)
                        .obsidianPreparationTime(2)
                        .crystalPreparationTime(2)
                        .attackPreparationTime(2)
                        .positionCooldownMs(1300L)
                        .fullScanIntervalMs(520L)
                        .positionCacheMs(850L)
                        .maxCrystalsPerPosition(4)
                        .maxPositionsToCheck(5)
                        .minCrystalScore(4.4)
                        .minAttackScore(0.38)
                        .build();
            case HARD:
                return new CPVPConfig.Builder()
                        .maxCrystalDistance(8.5)
                        .minCrystalDistance(2.2)
                        .crystalAttackRange(7.0)
                        .optimalDamageRange(4.6)
                        .obsidianPlaceCooldownTicks(4)
                        .crystalPlaceCooldownTicks(4)
                        .attackCooldownTicks(3)
                        .obsidianPreparationTime(2)
                        .crystalPreparationTime(2)
                        .attackPreparationTime(1)
                        .positionCooldownMs(1200L)
                        .fullScanIntervalMs(350L)
                        .positionCacheMs(700L)
                        .maxCrystalsPerPosition(4)
                        .maxPositionsToCheck(4)
                        .minCrystalScore(4.4)
                        .minAttackScore(0.35)
                        .build();
            case GOD:
                return new CPVPConfig.Builder()
                        .maxCrystalDistance(11.5)
                        .minCrystalDistance(1.8)
                        .crystalAttackRange(8.5)
                        .optimalDamageRange(4.8)
                        .obsidianPlaceCooldownTicks(0)
                        .crystalPlaceCooldownTicks(0)
                        .attackCooldownTicks(0)
                        .obsidianPreparationTime(0)
                        .crystalPreparationTime(0)
                        .attackPreparationTime(0)
                        .positionCooldownMs(350L)
                        .fullScanIntervalMs(150L)
                        .positionCacheMs(350L)
                        .maxCrystalsPerPosition(10)
                        .maxPositionsToCheck(8)
                        .minCrystalScore(1.6)
                        .minAttackScore(0.05)
                        .build();
            default:
                return new CPVPConfig.Builder().build();
        }
    }
}
