package com.monkey.mcbot.bot.ai.controllers.brain.helper;

import com.monkey.mcbot.bot.ai.controllers.brain.helper.inter.ICombatStateManager;
import com.monkey.mcbot.bot.ai.controllers.cpvp.BotCPVPController;
import com.monkey.mcbot.bot.ai.controllers.inventory.BotInventoryController;
import com.monkey.mcbot.bot.ai.controllers.rapvp.BotRAPVPController;
import com.monkey.mcbot.bot.ai.controllers.rapvp.helper.RAPVPState;
import com.monkey.mcbot.bot.ai.rank.BotRank;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;

public class CombatStateManager implements ICombatStateManager {

    private final Player bot;
    private final BotInventoryController inventoryController;
    private final BotCPVPController cpvpController;
    private final BotRAPVPController rapvpController;

    private CombatState currentState = CombatState.AGGRESSIVE;
    private long lastStateChange = 0;
    private static final long MIN_STATE_DURATION = 500;
    private long lastAnchorAttempt = 0;
    private long lastDamageTime = 0;
    private int consecutiveDamageCount = 0;
    private long lastComboPressureTime = 0L;

    public CombatStateManager(
            Player bot,
            BotInventoryController inventoryController,
            BotCPVPController cpvpController,
            BotRAPVPController rapvpController) {
        this.bot = bot;
        this.inventoryController = inventoryController;
        this.cpvpController = cpvpController;
        this.rapvpController = rapvpController;
    }

    @Override
    public void updateCombatState(Player target) {
        long currentTime = System.currentTimeMillis();
        double distance = bot.distanceTo(target);
        float healthPercent = bot.getHealth() / bot.getMaxHealth();

        long minDuration = (currentTime - lastDamageTime < 1000) ? 250 : MIN_STATE_DURATION;
        if (currentTime - lastStateChange < minDuration) return;

        CombatState newState = currentState;

        int botY = bot.blockPosition().getY();
        int targetY = target.blockPosition().getY();
        int yDiff = targetY - botY;
        BotRank rank = cpvpController.getRank();
        boolean hyperAggressive = rank == BotRank.GOD || rank == BotRank.HARD;
        boolean comboWindow = rapvpController.getState() == RAPVPState.WAITING_EXPLOSION
                || rapvpController.hadRecentAnchorExplosion(hyperAggressive ? 1500L : 1200L);
        if (comboWindow) {
            lastComboPressureTime = currentTime;
        }
        boolean keepComboMomentum = currentTime - lastComboPressureTime < (hyperAggressive ? 900L : 650L);
        boolean underDamagePressure =
                consecutiveDamageCount >= 2 && currentTime - lastDamageTime < (hyperAggressive ? 1800L : 2200L);

        if (keepComboMomentum && healthPercent > (hyperAggressive ? 0.10f : 0.18f)) {
            if (yDiff >= 2 && cpvpController.canPlaceCrystal()) {
                newState = CombatState.CRYSTAL_SETUP;
            } else if (shouldAttemptAnchor(target, currentTime)) {
                newState = CombatState.ANCHOR_SETUP;
            } else {
                newState = CombatState.AGGRESSIVE;
            }
        } else if (underDamagePressure
                && healthPercent > (hyperAggressive ? 0.10f : 0.20f)
                && yDiff < 2
                && shouldAttemptAnchor(target, currentTime)) {
            newState = CombatState.ANCHOR_SETUP;
        } else if (!hyperAggressive && healthPercent < 0.25f) {
            newState = CombatState.RETREATING;
        } else if (hyperAggressive && healthPercent < 0.14f) {
            newState = CombatState.DEFENSIVE;
        } else if (!hyperAggressive && consecutiveDamageCount >= 2 && currentTime - lastDamageTime < 1500) {
            newState = CombatState.DEFENSIVE;
        } else if (shouldAttemptCombat(distance)) {
            if (yDiff < 2 && shouldAttemptAnchor(target, currentTime)) {
                newState = CombatState.ANCHOR_SETUP;
            } else if (yDiff >= 2 && cpvpController.canPlaceCrystal()) {
                newState = CombatState.CRYSTAL_SETUP;
            } else if (shouldReposition(target, distance)) {
                newState = CombatState.REPOSITIONING;
            } else {
                newState = CombatState.AGGRESSIVE;
            }
        } else if (shouldReposition(target, distance)) {
            newState = CombatState.REPOSITIONING;
        } else {
            newState = CombatState.AGGRESSIVE;
        }

        if (newState != currentState) {
            currentState = newState;
            lastStateChange = currentTime;
            onStateChange(target);
        }
    }

    private boolean shouldAttemptCombat(double distance) {
        BotRank rank = cpvpController.getRank();
        boolean hyperAggressive = rank == BotRank.GOD || rank == BotRank.HARD;
        double maxDistance = hyperAggressive ? 16.0 : 12.0;
        float minHealth = hyperAggressive ? 0.12f : 0.3f;
        return distance > 0.7 && distance < maxDistance && bot.getHealth() / bot.getMaxHealth() > minHealth;
    }

    @Override
    public CombatState getCurrentState() {
        return currentState;
    }

    @Override
    public void onStateChange(Player target) {
        switch (currentState) {
            case AGGRESSIVE, CRYSTAL_SETUP, DEFENSIVE, REPOSITIONING, RETREATING -> {}
            case ANCHOR_SETUP -> {
                rapvpController.enable(target);
                lastAnchorAttempt = System.currentTimeMillis();
            }
        }
    }

    @Override
    public boolean shouldAttemptAnchor(Player target, long currentTime) {
        if (currentTime - lastAnchorAttempt < getAnchorAttemptCooldown()) return false;
        if (!inventoryController.hasItem(Items.RESPAWN_ANCHOR)) return false;
        if (!inventoryController.hasItem(Items.GLOWSTONE)) return false;

        BotRank rank = cpvpController.getRank();
        boolean hyperAggressive = rank == BotRank.GOD || rank == BotRank.HARD;
        double distance = bot.distanceTo(target);
        double maxDistance = hyperAggressive ? 11.0 : 8.0;
        return distance > 1.0 && distance < maxDistance && (target.onGround() || hyperAggressive);
    }

    @Override
    public boolean shouldReposition(Player target, double distance) {
        BotRank rank = cpvpController.getRank();
        boolean hyperAggressive = rank == BotRank.GOD || rank == BotRank.HARD;
        net.minecraft.world.phys.Vec3 botPos = bot.position();
        net.minecraft.world.phys.Vec3 targetPos = target.position();
        double yDiff = botPos.y - targetPos.y;
        if (!target.onGround() && targetPos.y > botPos.y + 0.5D) {
            return distance > 14.0D;
        }

        return (yDiff < -2 && distance > (hyperAggressive ? 1.5 : 2.0))
                || (distance < (hyperAggressive ? 1.2 : 1.5) && consecutiveDamageCount > (hyperAggressive ? 2 : 0))
                || (distance > 15.0);
    }

    private long getAnchorAttemptCooldown() {
        BotRank rank = cpvpController.getRank();
        if (rank == null) {
            return 8000L;
        }

        return switch (rank) {
            case EASY -> 8000L;
            case NORMAL -> 5200L;
            case MEDIUM -> 3200L;
            case HARD -> 1800L;
            case GOD -> 800L;
        };
    }

    public void updateDamageData(int consecutiveDamageCount, long lastDamageTime) {
        this.consecutiveDamageCount = consecutiveDamageCount;
        this.lastDamageTime = lastDamageTime;
    }
}
