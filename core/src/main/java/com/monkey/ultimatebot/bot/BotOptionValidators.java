package com.monkey.ultimatebot.bot;

import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import com.monkey.ultimatebot.utils.armor.ArmorTier;

final class BotOptionValidators {

    private BotOptionValidators() {}

    static void validateTotemRange(int minTotemCount, int maxTotemCount) {
        if (minTotemCount < -1) {
            throw new IllegalArgumentException("min totem count cannot be less than -1");
        }
        if (maxTotemCount < 0) {
            throw new IllegalArgumentException("max totem count cannot be less than 0");
        }
        if (minTotemCount > maxTotemCount) {
            throw new IllegalArgumentException("min totem count cannot be greater than max totem count");
        }
    }

    static void validateDifficultyRange(DifficultyLevel minDifficulty, DifficultyLevel maxDifficulty) {
        if (minDifficulty == null || maxDifficulty == null) {
            throw new IllegalArgumentException("difficulty bounds cannot be null");
        }
        if (minDifficulty.compareTo(maxDifficulty) > 0) {
            throw new IllegalArgumentException("min difficulty cannot be greater than max difficulty");
        }
    }

    static void validateArmorRange(ArmorTier minArmorTier, ArmorTier maxArmorTier) {
        if (minArmorTier == null || maxArmorTier == null) {
            throw new IllegalArgumentException("armor bounds cannot be null");
        }
        if (minArmorTier.compareTo(maxArmorTier) > 0) {
            throw new IllegalArgumentException("min armor cannot be greater than max armor");
        }
    }

    static boolean parseBlastBinary(int value, String fieldName) {
        if (value == 0) {
            return false;
        }
        if (value == 1) {
            return true;
        }
        throw new IllegalArgumentException(fieldName + " blast value must be 0 or 1");
    }
}
