package com.monkey.ultimatebot.sdk.model.request;

import com.monkey.ultimatebot.common.model.DifficultyTier;
import java.util.Objects;

/** Runtime request for a bot difficulty. */
public record DifficultyRequest(DifficultyTier difficulty) {
    public DifficultyRequest {
        Objects.requireNonNull(difficulty, "difficulty");
    }
}
