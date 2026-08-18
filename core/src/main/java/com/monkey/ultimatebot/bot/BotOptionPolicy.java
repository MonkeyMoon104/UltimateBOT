package com.monkey.ultimatebot.bot;

import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import com.monkey.ultimatebot.utils.armor.ArmorTier;
import org.jspecify.annotations.Nullable;

final class BotOptionPolicy {

    private BotOptionPolicy() {}

    static int clampTotemCount(int candidate, int minTotemCount, int maxTotemCount) {
        int value = candidate;
        if (value < minTotemCount) {
            value = minTotemCount;
        }
        if (value > maxTotemCount) {
            value = maxTotemCount;
        }
        if (value == -1 && minTotemCount > -1) {
            value = minTotemCount;
        }
        return value;
    }

    static DifficultyLevel clampDifficulty(
            DifficultyLevel candidate, DifficultyLevel minDifficulty, DifficultyLevel maxDifficulty) {
        if (candidate.compareTo(minDifficulty) < 0) {
            return minDifficulty;
        }
        if (candidate.compareTo(maxDifficulty) > 0) {
            return maxDifficulty;
        }
        return candidate;
    }

    static ArmorRange normalizeArmorRange(@Nullable ArmorTier minArmorTier, @Nullable ArmorTier maxArmorTier) {
        ArmorTier platformMax = ArmorTier.maxAvailable();
        ArmorTier resolvedMax = maxArmorTier == null ? platformMax : maxArmorTier;
        if (resolvedMax.compareTo(platformMax) > 0) {
            resolvedMax = platformMax;
        }
        ArmorTier resolvedMin = minArmorTier == null ? ArmorTier.LEATHER : minArmorTier;
        if (resolvedMin.compareTo(resolvedMax) > 0) {
            resolvedMin = ArmorTier.LEATHER;
        }
        BotOptionValidators.validateArmorRange(resolvedMin, resolvedMax);
        return new ArmorRange(resolvedMin, resolvedMax);
    }

    static ArmorTier clampArmorTier(@Nullable ArmorTier armorTier, ArmorTier minArmorTier, ArmorTier maxArmorTier) {
        ArmorTier platformMax = ArmorTier.maxAvailable();
        ArmorTier resolvedMax = maxArmorTier.compareTo(platformMax) > 0 ? platformMax : maxArmorTier;
        if (armorTier == null) {
            return minArmorTier.compareTo(resolvedMax) > 0 ? resolvedMax : minArmorTier;
        }
        if (armorTier.compareTo(minArmorTier) < 0) {
            return minArmorTier;
        }
        if (armorTier.compareTo(resolvedMax) > 0) {
            return resolvedMax;
        }
        return armorTier;
    }

    static final class ArmorRange {
        private final ArmorTier min;
        private final ArmorTier max;

        ArmorRange(ArmorTier min, ArmorTier max) {
            this.min = min;
            this.max = max;
        }

        ArmorTier min() {
            return min;
        }

        ArmorTier max() {
            return max;
        }
    }
}
