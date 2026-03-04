package com.monkey.mcbot.bot.ai.controllers.brain.helper;

import com.monkey.mcbot.bot.ai.ITrainingBot;
import com.monkey.mcbot.bot.ai.controllers.brain.helper.inter.ICombatDataManager;
import com.monkey.mcbot.bot.ai.controllers.cpvp.BotCPVPController;
import com.monkey.mcbot.bot.ai.controllers.enderpearl.BotEnderpearlController;
import com.monkey.mcbot.bot.ai.controllers.rapvp.BotRAPVPController;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class CombatDataManager implements ICombatDataManager {

    private final Player bot;
    private final BotEnderpearlController enderpearlController;
    private final BotRAPVPController rapvpController;
    private final BotCPVPController cpvpController;

    private float lastKnownHealth;
    private int consecutiveDamageCount = 0;
    private long lastDamageTime = 0;
    private static final long DAMAGE_COMBO_WINDOW = 2000;

    private Vec3 lastTargetPosition;
    private Vec3 targetVelocity = Vec3.ZERO;
    private long lastPositionUpdate = 0;

    public CombatDataManager(Player bot, BotEnderpearlController enderpearlController,
                             BotRAPVPController rapvpController, BotCPVPController cpvpController) {
        this.bot = bot;
        this.enderpearlController = enderpearlController;
        this.rapvpController = rapvpController;
        this.cpvpController = cpvpController;
        this.lastKnownHealth = bot.getHealth();
    }

    @Override
    public void updateCombatData(Player target) {
        float currentHealth = bot.getHealth();
        long currentTime = System.currentTimeMillis();

        if (currentHealth < lastKnownHealth) {
            consecutiveDamageCount++;
            lastDamageTime = currentTime;
            enderpearlController.onDamageReceived();
        } else if (currentTime - lastDamageTime > DAMAGE_COMBO_WINDOW) {
            consecutiveDamageCount = Math.max(0, consecutiveDamageCount - 1);
        }

        lastKnownHealth = currentHealth;

        Vec3 currentTargetPos = target.position();
        if (lastTargetPosition != null && currentTime - lastPositionUpdate > 50) {
            targetVelocity = currentTargetPos.subtract(lastTargetPosition).scale(20.0 / (currentTime - lastPositionUpdate) * 1000);
        }
        lastTargetPosition = currentTargetPos;
        lastPositionUpdate = currentTime;

        if (((ITrainingBot) bot).isCombat()) {
            int botY = bot.blockPosition().getY();
            int targetY = target.blockPosition().getY();
            int yDiff = targetY - botY;

            if (yDiff < 2) {
                rapvpController.tick();
            }
            else {
                cpvpController.tick(target);
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
    public Vec3 getTargetVelocity() {
        return targetVelocity;
    }

    @Override
    public Vec3 getLastTargetPosition() {
        return lastTargetPosition;
    }
}