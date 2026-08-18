package com.monkey.ultimatebot.access.player;

import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public final class PlayerAttackAccess {

    private static final boolean HAS_BUKKIT_ATTACK = hasAttack(Player.class);
    private static final Object LOCK = new Object();
    private static volatile @Nullable NmsAttack nms;

    private PlayerAttackAccess() {}

    public static void attack(Player player, Entity target) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(target, "target");
        if (HAS_BUKKIT_ATTACK) {
            player.attack(target);
            return;
        }
        try {
            player.getClass().getMethod("attack", Entity.class).invoke(player, target);
            return;
        } catch (ReflectiveOperationException ignored) {

        }
        nms(player).attack(player, target);
    }

    private static NmsAttack nms(Player player) {
        NmsAttack cached = nms;
        if (cached != null) {
            return cached;
        }
        synchronized (LOCK) {
            cached = nms;
            if (cached != null) {
                return cached;
            }
            cached = NmsAttack.resolve(player);
            nms = cached;
            return cached;
        }
    }

    private static boolean hasAttack(Class<?> type) {
        try {
            type.getMethod("attack", Entity.class);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    private static final class NmsAttack {
        private final @Nullable Method getHandle;
        private final @Nullable Method attack;

        private NmsAttack(@Nullable Method getHandle, @Nullable Method attack) {
            this.getHandle = getHandle;
            this.attack = attack;
        }

        static NmsAttack resolve(Player sample) {
            try {
                Method getHandle = sample.getClass().getMethod("getHandle");
                Object handle = getHandle.invoke(sample);
                if (handle == null) {
                    return new NmsAttack(null, null);
                }
                return new NmsAttack(getHandle, findAttack(handle.getClass()));
            } catch (ReflectiveOperationException ignored) {
                return new NmsAttack(null, null);
            }
        }

        void attack(Player player, Entity target) {
            if (getHandle == null || attack == null) {
                return;
            }
            try {
                Object handle = getHandle.invoke(player);
                Object nmsTarget = target.getClass().getMethod("getHandle").invoke(target);
                if (handle == null || nmsTarget == null) {
                    return;
                }
                attack.invoke(handle, nmsTarget);
            } catch (ReflectiveOperationException ignored) {

            }
        }

        private static @Nullable Method findAttack(Class<?> handleClass) {
            for (Method method : handleClass.getMethods()) {
                if (!"attack".equals(method.getName()) || method.getParameterCount() != 1) {
                    continue;
                }
                Class<?> param = method.getParameterTypes()[0];
                if (param.getName().endsWith(".Entity") || "Entity".equals(param.getSimpleName())) {
                    return method;
                }
            }
            return null;
        }
    }
}
