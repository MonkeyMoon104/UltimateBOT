package com.monkey.ultimatebot.bot.ai.controllers.movement.helper;

import com.monkey.ultimatebot.bot.ai.controllers.movement.helper.interf.ICombatStateManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;
import org.jspecify.annotations.Nullable;

public class CombatStateManager implements ICombatStateManager {
    private @Nullable Vector lastTargetPosition;
    private Vector targetVelocity = new Vector();
    private boolean isUnderFire = false;
    private long lastDamageTime = 0;
    private int consecutiveHits = 0;
    private @Nullable Vector lastSafePosition;

    @Override
    public void updateCombatData(LivingEntity target) {
        Vector currentTargetPos = target.getLocation().toVector();

        if (lastTargetPosition != null) {
            targetVelocity = currentTargetPos.clone().subtract(lastTargetPosition);
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
    public Vector getTargetVelocity() {
        return targetVelocity;
    }

    public void setLastSafePosition(@Nullable Vector position) {
        this.lastSafePosition = position;
    }

    public @Nullable Vector getLastSafePosition() {
        return lastSafePosition;
    }
}
