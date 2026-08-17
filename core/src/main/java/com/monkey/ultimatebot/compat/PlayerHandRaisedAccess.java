package com.monkey.ultimatebot.compat;

import java.util.Objects;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.LivingEntity;

/**
 * {@link LivingEntity#isHandRaised()} across Bukkit versions.
 *
 * <p>The method exists from 1.11. Spigot 1.9–1.10 share offhand/shield but lack this API, so this
 * is dual-path in core — not a new NMS package. Fallback is {@link HumanEntity#isBlocking()}.
 */
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
