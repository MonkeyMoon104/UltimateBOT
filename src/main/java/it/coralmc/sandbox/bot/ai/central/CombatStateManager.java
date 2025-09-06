package it.coralmc.sandbox.bot.ai.central;

import it.coralmc.sandbox.bot.ai.central.inter.ICombatStateManager;
import it.coralmc.sandbox.bot.ai.controllers.cpvp.BotCPVPController;
import it.coralmc.sandbox.bot.ai.controllers.inventory.BotInventoryController;
import it.coralmc.sandbox.bot.ai.controllers.rapvp.BotRAPVPController;
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
    private static final long ANCHOR_ATTEMPT_COOLDOWN = 8000;
    private long lastDamageTime = 0;
    private int consecutiveDamageCount = 0;

    public CombatStateManager(Player bot, BotInventoryController inventoryController,
                              BotCPVPController cpvpController, BotRAPVPController rapvpController) {
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

        double yDiff = bot.position().y - target.position().y;

        if (healthPercent < 0.25f) {
            newState = CombatState.RETREATING;
        } else if (consecutiveDamageCount >= 2 && currentTime - lastDamageTime < 1500) {
            newState = CombatState.DEFENSIVE;
        }
        else if (Math.abs(yDiff) <= 3.0 && shouldAttemptAnchor(target, currentTime)) {
            newState = CombatState.ANCHOR_SETUP;
        }
        else if (yDiff < -1.0 && cpvpController.canPlaceCrystal()) {
            newState = CombatState.CRYSTAL_SETUP;
        }
        else if (shouldReposition(target, distance)) {
            newState = CombatState.REPOSITIONING;
        } else if (distance > 4.0 && distance < 12.0 && healthPercent > 0.4f) {
            newState = CombatState.CRYSTAL_SETUP;
        } else if (distance <= 4.0 && healthPercent > 0.3f) {
            newState = CombatState.AGGRESSIVE;
        } else {
            newState = CombatState.AGGRESSIVE;
        }

        if (newState != currentState) {
            currentState = newState;
            lastStateChange = currentTime;
            onStateChange(target);
        }
    }

    @Override
    public CombatState getCurrentState() {
        return currentState;
    }

    @Override
    public void onStateChange(Player target) {
        switch (currentState) {
            case DEFENSIVE -> {
            }
            case ANCHOR_SETUP -> {
                rapvpController.enable(target);
                lastAnchorAttempt = System.currentTimeMillis();
            }
            case RETREATING -> {
            }
            case REPOSITIONING -> {
            }
        }
    }

    @Override
    public boolean shouldAttemptAnchor(Player target, long currentTime) {
        if (currentTime - lastAnchorAttempt < ANCHOR_ATTEMPT_COOLDOWN) return false;
        if (!inventoryController.hasItem(Items.RESPAWN_ANCHOR)) return false;
        if (!inventoryController.hasItem(Items.GLOWSTONE)) return false;

        double distance = bot.distanceTo(target);

        return distance > 1.0 && distance < 8.0 && target.onGround();
    }

    @Override
    public boolean shouldReposition(Player target, double distance) {
        net.minecraft.world.phys.Vec3 botPos = bot.position();
        net.minecraft.world.phys.Vec3 targetPos = target.position();
        double yDiff = botPos.y - targetPos.y;

        return (yDiff < -2 && distance > 2.0) ||
                (distance < 1.5 && consecutiveDamageCount > 0) ||
                (distance > 15.0);
    }

    public void updateDamageData(int consecutiveDamageCount, long lastDamageTime) {
        this.consecutiveDamageCount = consecutiveDamageCount;
        this.lastDamageTime = lastDamageTime;
    }
}