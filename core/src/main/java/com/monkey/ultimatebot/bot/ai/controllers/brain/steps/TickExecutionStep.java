package com.monkey.ultimatebot.bot.ai.controllers.brain.steps;

import com.monkey.ultimatebot.bot.ai.BotAI;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public final class TickExecutionStep {

    @FunctionalInterface
    public interface CombatAllowance {
        boolean shouldUseCombatOnPlayerTarget();
    }

    private final BotAI botAI;
    private final boolean follow;
    private final boolean combat;
    private final boolean watchOnlyMode;
    private final CombatAllowance combatAllowance;
    private final NoTargetTickStep noTargetTickStep;
    private final WatchOnlyTickStep watchOnlyTickStep;

    public TickExecutionStep(
            BotAI botAI,
            boolean follow,
            boolean combat,
            boolean watchOnlyMode,
            CombatAllowance combatAllowance,
            NoTargetTickStep noTargetTickStep,
            WatchOnlyTickStep watchOnlyTickStep) {
        this.botAI = botAI;
        this.follow = follow;
        this.combat = combat;
        this.watchOnlyMode = watchOnlyMode;
        this.combatAllowance = combatAllowance;
        this.noTargetTickStep = noTargetTickStep;
        this.watchOnlyTickStep = watchOnlyTickStep;
    }

    public void execute(@Nullable LivingEntity selectedTarget) {
        if (selectedTarget == null) {
            noTargetTickStep.execute();
            return;
        }

        boolean playerTarget = selectedTarget instanceof Player;
        boolean allowCombat = playerTarget ? combatAllowance.shouldUseCombatOnPlayerTarget() : combat;

        if (playerTarget && watchOnlyMode) {
            watchOnlyTickStep.execute(selectedTarget);
            return;
        }

        if (follow || allowCombat) {
            botAI.tick(selectedTarget, allowCombat);
            return;
        }

        botAI.tickIdle();
    }
}
