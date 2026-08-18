package com.monkey.ultimatebot.bot.ai.controllers.brain.steps;

import com.monkey.ultimatebot.bot.ai.BotAI;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public final class NoTargetTickStep {

    @FunctionalInterface
    public interface FollowAnchorPredicate {
        boolean shouldFollow();
    }

    private final BotAI botAI;
    private final @Nullable Player targetPlayer;
    private final FollowAnchorPredicate followAnchorPredicate;

    public NoTargetTickStep(
            BotAI botAI,
            @Nullable Player targetPlayer,
            FollowAnchorPredicate followAnchorPredicate) {
        this.botAI = botAI;
        this.targetPlayer = targetPlayer;
        this.followAnchorPredicate = followAnchorPredicate;
    }

    public void execute() {
        if (followAnchorPredicate.shouldFollow()) {
            botAI.tick(java.util.Objects.requireNonNull(targetPlayer, "follow target"), false);
            return;
        }
        botAI.tickIdle();
    }
}
