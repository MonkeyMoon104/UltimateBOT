package com.monkey.ultimatebot.bot;

import com.monkey.ultimatebot.api.model.identity.BotSkin;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import com.monkey.ultimatebot.common.model.bot.BotTargetMode;
import org.jspecify.annotations.Nullable;

final class BotOptionDefaults {

    private BotOptionDefaults() {}

    static BotType botType(@Nullable BotType botType) {
        return botType == null ? BotType.SINGLE : botType;
    }

    static BotSkin botSkin(@Nullable BotSkin botSkin) {
        return botSkin == null ? BotSkin.owner() : botSkin;
    }

    static DifficultyLevel difficulty(@Nullable DifficultyLevel difficulty) {
        return difficulty == null ? DifficultyLevel.EASY : difficulty;
    }

    static BotTargetMode targetMode(@Nullable BotTargetMode targetMode) {
        return targetMode == null ? BotTargetMode.PLAYERS : targetMode;
    }

    static BotCreationSource creationSource(@Nullable BotCreationSource creationSource) {
        return creationSource == null ? BotCreationSource.CORE : creationSource;
    }

    static double positiveOrDefault(double candidate, double fallback) {
        return candidate <= 0.0D ? fallback : candidate;
    }

    static @Nullable String customKillMessage(@Nullable String customKillMessage) {
        return customKillMessage == null || customKillMessage.trim().isEmpty() ? null : customKillMessage;
    }
}
