package com.monkey.ultimatebot.access.player;

import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public final class PlayerAttackCooldownAccess {

    private static final boolean HAS_BUKKIT = hasNoArg(HumanEntity.class, "getAttackCooldown");
    private static final Object LOCK = new Object();
    private static volatile @Nullable NmsCooldown nms;

    private PlayerAttackCooldownAccess() {}

    public static float get(Player player) {
        return get(player, 0.5F);
    }

    public static float get(Player player, float advance) {
        Objects.requireNonNull(player, "player");
        if (HAS_BUKKIT) {
            return player.getAttackCooldown();
        }
        return nms(player).get(player, advance);
    }

    private static NmsCooldown nms(Player player) {
        NmsCooldown cached = nms;
        if (cached != null) {
            return cached;
        }
        synchronized (LOCK) {
            cached = nms;
            if (cached != null) {
                return cached;
            }
            cached = NmsCooldown.resolve(player);
            nms = cached;
            return cached;
        }
    }

    private static boolean hasNoArg(Class<?> type, String name) {
        try {
            type.getMethod(name);
            return true;
        } catch (NoSuchMethodException ignored) {
            return false;
        }
    }

    private static final class NmsCooldown {
        private final @Nullable Method getHandle;
        private final @Nullable Method strength;

        private NmsCooldown(@Nullable Method getHandle, @Nullable Method strength) {
            this.getHandle = getHandle;
            this.strength = strength;
        }

        static NmsCooldown resolve(Player sample) {
            try {
                Method getHandle = sample.getClass().getMethod("getHandle");
                Object handle = getHandle.invoke(sample);
                if (handle == null) {
                    return new NmsCooldown(null, null);
                }
                return new NmsCooldown(getHandle, findStrength(handle.getClass()));
            } catch (ReflectiveOperationException ignored) {
                return new NmsCooldown(null, null);
            }
        }

        float get(Player player, float advance) {
            if (getHandle == null || strength == null) {
                return 1.0F;
            }
            try {
                Object handle = getHandle.invoke(player);
                if (handle == null) {
                    return 1.0F;
                }
                Object value = strength.invoke(handle, Float.valueOf(advance));
                if (value instanceof Float) {
                    return ((Float) value).floatValue();
                }
            } catch (ReflectiveOperationException ignored) {

            }
            return 1.0F;
        }

        private static @Nullable Method findStrength(Class<?> handleClass) {
            Method mapped = namedFloat(handleClass, "getAttackCooldown");
            if (mapped != null) {
                return mapped;
            }
            Method mojang = namedFloat(handleClass, "getAttackStrengthScale");
            if (mojang != null) {
                return mojang;
            }
            Class<?> current = handleClass;
            while (current != null) {
                String simple = current.getSimpleName();
                if ("EntityHuman".equals(simple) || "Player".equals(simple) || "ServerPlayer".equals(simple)) {
                    try {
                        Method obfuscated = current.getDeclaredMethod("s", float.class);
                        if (obfuscated.getReturnType() == float.class) {
                            obfuscated.setAccessible(true);
                            return obfuscated;
                        }
                    } catch (NoSuchMethodException ignored) {

                    }
                }
                current = current.getSuperclass();
            }
            return null;
        }

        private static @Nullable Method namedFloat(Class<?> handleClass, String name) {
            try {
                Method method = handleClass.getMethod(name, float.class);
                if (method.getReturnType() == float.class) {
                    return method;
                }
            } catch (NoSuchMethodException ignored) {

            }
            return null;
        }
    }
}
