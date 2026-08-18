package com.monkey.ultimatebot.bot.ai.controllers.brain.helper;

import com.monkey.ultimatebot.bot.ai.ITrainingBot;
import com.monkey.ultimatebot.bot.ai.controllers.brain.helper.inter.ICombatDataManager;
import com.monkey.ultimatebot.bot.ai.controllers.cpvp.BotCPVPController;
import com.monkey.ultimatebot.bot.ai.controllers.enderpearl.BotEnderpearlController;
import com.monkey.ultimatebot.bot.ai.controllers.rapvp.BotRAPVPController;
import com.monkey.ultimatebot.bot.ai.controllers.rapvp.helper.RAPVPState;
import com.monkey.ultimatebot.bot.ai.difficulty.DifficultyLevel;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

@SuppressWarnings("NullAway")
public class CombatDataManager implements ICombatDataManager {

    private final ITrainingBot bot;
    private final BotEnderpearlController enderpearlController;
    private final BotRAPVPController rapvpController;
    private final BotCPVPController cpvpController;

    private float lastKnownHealth;
    private int consecutiveDamageCount = 0;
    private long lastDamageTime = 0;
    private static final long DAMAGE_COMBO_WINDOW = 2000;

    private @Nullable Vector lastTargetPosition;
    private Vector targetVelocity = new Vector();
    private long lastPositionUpdate = 0;

    public CombatDataManager(
            ITrainingBot bot,
            BotEnderpearlController enderpearlController,
            BotRAPVPController rapvpController,
            BotCPVPController cpvpController) {
        this.bot = bot;
        this.enderpearlController = enderpearlController;
        this.rapvpController = rapvpController;
        this.cpvpController = cpvpController;
        this.lastKnownHealth = (float) bot.healthValue();
    }

    @Override
    public void updateCombatData(Player target, boolean allowCombatActions) {
        float currentHealth = (float) bot.healthValue();
        long currentTime = System.currentTimeMillis();

        if (currentHealth < lastKnownHealth) {
            consecutiveDamageCount++;
            lastDamageTime = currentTime;
            enderpearlController.onDamageReceived();
        } else if (currentTime - lastDamageTime > DAMAGE_COMBO_WINDOW) {
            consecutiveDamageCount = Math.max(0, consecutiveDamageCount - 1);
        }

        lastKnownHealth = currentHealth;

        Vector currentTargetPos = target.getLocation().toVector();
        if (lastTargetPosition != null && currentTime - lastPositionUpdate > 50) {
            targetVelocity = currentTargetPos
                    .clone()
                    .subtract(lastTargetPosition)
                    .multiply(20.0 / (currentTime - lastPositionUpdate) * 1000);
        }
        lastTargetPosition = currentTargetPos;
        lastPositionUpdate = currentTime;

        if (bot.isCombat() && allowCombatActions) {
            int botY = bot.getLocation().getBlockY();
            int targetY = target.getLocation().getBlockY();
            int yDiff = targetY - botY;
            DifficultyLevel difficulty = cpvpController.getDifficulty();
            boolean hyperAggressive = difficulty == DifficultyLevel.GOD || difficulty == DifficultyLevel.HARD;
            boolean forceCrystalFollowup = rapvpController.getState() == RAPVPState.WAITING_EXPLOSION
                    || rapvpController.hadRecentAnchorExplosion(hyperAggressive ? 1400L : 1200L);

            if (hyperAggressive) {
                rapvpController.tick();
                cpvpController.tick(target);
                if (forceCrystalFollowup) {
                    cpvpController.tick(target);
                }
            } else if (yDiff < 2) {
                rapvpController.tick();
                if (!rapvpController.isActive()) {
                    cpvpController.tick(target);
                }
                if (forceCrystalFollowup) {
                    cpvpController.tick(target);
                }
            } else {
                cpvpController.tick(target);
                if (!cpvpController.isDoingCrystalAction()) {
                    rapvpController.tick();
                }
                if (forceCrystalFollowup && !cpvpController.isDoingCrystalAction()) {
                    cpvpController.tick(target);
                }
            }
        }
    }

    @Override
    public float getLastKnownHealth() {
        return lastKnownHealth;
    }

    @Override
    public int getConsecutiveDamageCount() {
        return consecutiveDamageCount;
    }

    @Override
    public long getLastDamageTime() {
        return lastDamageTime;
    }

    @Override
    public Vector getTargetVelocity() {
        return targetVelocity;
    }

    @Override
    public @Nullable Vector getLastTargetPosition() {
        return lastTargetPosition;
    }
}
