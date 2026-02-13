package com.monkey.mcbot.bot.ai.controllers.movement.helper;

import com.monkey.mcbot.bot.ai.controllers.movement.helper.interf.ICombatStateManager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class CombatStateManager implements ICombatStateManager {
    private Vec3 lastTargetPosition;
    private Vec3 targetVelocity = Vec3.ZERO;
    private boolean isUnderFire = false;
    private long lastDamageTime = 0;
    private int consecutiveHits = 0;
    private Vec3 lastSafePosition = null;

    @Override
    public void updateCombatData(Player target) {
        Vec3 currentTargetPos = target.position();

        if (lastTargetPosition != null) {
            targetVelocity = currentTargetPos.subtract(lastTargetPosition);
        }

        long currentTime = System.currentTimeMillis();
        if (currentTime - lastDamageTime > 3000) {
            isUnderFire = false;
            consecutiveHits = 0;
        }

        lastTargetPosition = currentTargetPos;
    }

    @Override
    public void setUnderFire(boolean underFire) {
        this.isUnderFire = underFire;
        this.lastDamageTime = System.currentTimeMillis();
        if (underFire) {
            this.consecutiveHits++;
        }
    }

    @Override
    public void onDamageReceived() {
        setUnderFire(true);
    }

    @Override
    public boolean isUnderFire() {
        return isUnderFire;
    }

    @Override
    public boolean hasRecentDamage() {
        return System.currentTimeMillis() - lastDamageTime < 3000;
    }

    @Override
    public int getConsecutiveHits() {
        return consecutiveHits;
    }

    @Override
    public void resetCombatState() {
        isUnderFire = false;
        consecutiveHits = 0;
    }

    @Override
    public Vec3 getTargetVelocity() {
        return targetVelocity;
    }

    public void setLastSafePosition(Vec3 position) {
        this.lastSafePosition = position;
    }

    public Vec3 getLastSafePosition() {
        return lastSafePosition;
    }
}