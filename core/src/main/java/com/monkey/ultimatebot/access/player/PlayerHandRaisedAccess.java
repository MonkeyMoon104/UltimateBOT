package com.monkey.ultimatebot.access.player;

import java.util.Objects;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;

public final class PlayerHandRaisedAccess {

    private static final boolean HAS_HAND_RAISED = hasNoArg(LivingEntity.class, "isHandRaised");

    private PlayerHandRaisedAccess() {}

    public static boolean isRaised(LivingEntity entity) {
        Objects.requireNonNull(entity, "entity");
        if (HAS_HAND_RAISED) {
            return entity.isHandRaised();
        }
        return entity instanceof HumanEntity && ((HumanEntity) entity).isBlocking();
    }

    private static boolean hasNoArg(Class<?> type, String name) {
        try {
            type.getMethod(name);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }
}
