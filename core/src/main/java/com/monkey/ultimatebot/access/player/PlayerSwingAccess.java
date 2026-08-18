package com.monkey.ultimatebot.access.player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import org.bukkit.World;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.jspecify.annotations.Nullable;

public final class PlayerSwingAccess {

    private static final boolean HAS_SWING_MAIN = hasNoArg(LivingEntity.class, "swingMainHand");
    private static final boolean HAS_SWING_OFF = hasNoArg(LivingEntity.class, "swingOffHand");

    private static final Object LOCK = new Object();
    private static volatile @Nullable NmsSwing nms;

    private PlayerSwingAccess() {}

    public static void swingMainHand(Player player) {
        Objects.requireNonNull(player, "player");
        if (HAS_SWING_MAIN) {
            player.swingMainHand();
            return;
        }
        nms(player).swing(player, false);
    }

    public static void swingOffHand(Player player) {
        Objects.requireNonNull(player, "player");
        if (HAS_SWING_OFF) {
            player.swingOffHand();
            return;
        }
        nms(player).swing(player, true);
    }

    private static NmsSwing nms(Player player) {
        NmsSwing cached = nms;
        if (cached != null) {
            return cached;
        }
        synchronized (LOCK) {
            cached = nms;
            if (cached != null) {
                return cached;
            }
            cached = NmsSwing.resolve(player);
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

    private static final class NmsSwing {
        private final @Nullable Method getHandle;
        private final @Nullable Method swingHand;
        private final @Nullable Object mainHand;
        private final @Nullable Object offHand;
        private final @Nullable Constructor<?> animationPacket;
        private final @Nullable Field playerConnection;
        private final @Nullable Method sendPacket;

        private NmsSwing(
                @Nullable Method getHandle,
                @Nullable Method swingHand,
                @Nullable Object mainHand,
                @Nullable Object offHand,
                @Nullable Constructor<?> animationPacket,
                @Nullable Field playerConnection,
                @Nullable Method sendPacket) {
            this.getHandle = getHandle;
            this.swingHand = swingHand;
            this.mainHand = mainHand;
            this.offHand = offHand;
            this.animationPacket = animationPacket;
            this.playerConnection = playerConnection;
            this.sendPacket = sendPacket;
        }

        static NmsSwing resolve(Player sample) {
            try {
                Method getHandle = sample.getClass().getMethod("getHandle");
                Object handle = getHandle.invoke(sample);
                if (handle == null) {
                    return missing();
                }
                Method swingHand = findSwingHand(handle.getClass());
                Object main = null;
                Object off = null;
                if (swingHand != null) {
                    Class<?> handType = swingHand.getParameterTypes()[0];
                    main = enumConstant(handType, "MAIN_HAND", "MAINHAND");
                    off = enumConstant(handType, "OFF_HAND", "OFFHAND");
                }
                Class<?> packetClass = findAnimationPacketClass(handle.getClass());
                Constructor<?> ctor = packetClass == null ? null : findAnimationCtor(packetClass, handle.getClass());
                Field connection = findField(handle.getClass(), "playerConnection", "connection");
                Method send = connection == null ? null : findSendPacket(connection.getType());
                return new NmsSwing(getHandle, swingHand, main, off, ctor, connection, send);
            } catch (ReflectiveOperationException ignored) {
                return missing();
            }
        }

        private static NmsSwing missing() {
            return new NmsSwing(null, null, null, null, null, null, null);
        }

        void swing(Player player, boolean offHand) {
            swingNms(player, offHand);
            broadcastAnimation(player, offHand ? 3 : 0);
        }

        void swingNms(Player player, boolean offHand) {
            if (getHandle == null || swingHand == null) {
                return;
            }
            try {
                Object handle = getHandle.invoke(player);
                if (handle == null) {
                    return;
                }
                Object hand = offHand ? firstNonNull(this.offHand, this.mainHand) : this.mainHand;
                if (hand == null) {
                    return;
                }
                if (swingHand.getParameterCount() == 2) {
                    swingHand.invoke(handle, hand, Boolean.TRUE);
                } else {
                    swingHand.invoke(handle, hand);
                }
            } catch (ReflectiveOperationException ignored) {

            }
        }

        void broadcastAnimation(Player player, int animation) {
            Method resolveHandle = getHandle;
            if (animationPacket == null || playerConnection == null || sendPacket == null || resolveHandle == null) {
                return;
            }
            World world = player.getWorld();
            if (world == null) {
                return;
            }
            try {
                Object handle = resolveHandle.invoke(player);
                if (handle == null) {
                    return;
                }
                Object packet = animationPacket.newInstance(handle, Integer.valueOf(animation));
                for (Player viewer : world.getPlayers()) {
                    if (viewer.getEntityId() == player.getEntityId()) {
                        continue;
                    }
                    Object viewerHandle = resolveHandle.invoke(viewer);
                    if (viewerHandle == null) {
                        continue;
                    }
                    Object connection = playerConnection.get(viewerHandle);
                    if (connection != null) {
                        sendPacket.invoke(connection, packet);
                    }
                }
            } catch (ReflectiveOperationException ignored) {

            }
        }

        private static @Nullable Method findSwingHand(Class<?> handleClass) {
            Method named = null;
            Method obfuscated = null;
            Method twoArg = null;
            for (Method method : handleClass.getMethods()) {
                Class<?>[] params = method.getParameterTypes();
                if (params.length == 0 || !isHandEnum(params[0])) {
                    continue;
                }
                if (params.length == 1) {
                    if ("swingHand".equals(method.getName())) {
                        named = method;
                    } else if ("a".equals(method.getName())) {
                        obfuscated = method;
                    }
                } else if (params.length == 2 && params[1] == boolean.class && "swingHand".equals(method.getName())) {
                    twoArg = method;
                }
            }
            if (named != null) {
                return named;
            }
            if (obfuscated != null) {
                return obfuscated;
            }
            return twoArg;
        }

        private static boolean isHandEnum(Class<?> type) {
            if (!type.isEnum()) {
                return false;
            }
            String simple = type.getSimpleName();
            return "EnumHand".equals(simple) || "InteractionHand".equals(simple);
        }

        private static @Nullable Class<?> findAnimationPacketClass(Class<?> handleClass) {
            Class<?> current = handleClass;
            while (current != null) {
                Package pkg = current.getPackage();
                if (pkg != null && pkg.getName().startsWith("net.minecraft.server.v")) {
                    try {
                        return Class.forName(pkg.getName() + ".PacketPlayOutAnimation");
                    } catch (ClassNotFoundException ignored) {
                        return null;
                    }
                }
                current = current.getSuperclass();
            }
            return null;
        }

        private static @Nullable Constructor<?> findAnimationCtor(Class<?> packetClass, Class<?> handleClass) {
            for (Constructor<?> ctor : packetClass.getConstructors()) {
                Class<?>[] params = ctor.getParameterTypes();
                if (params.length == 2 && params[1] == int.class && params[0].isAssignableFrom(handleClass)) {
                    return ctor;
                }
            }
            return null;
        }

        private static @Nullable Field findField(Class<?> type, String... names) {
            Class<?> current = type;
            while (current != null) {
                for (String name : names) {
                    try {
                        Field field = current.getDeclaredField(name);
                        field.setAccessible(true);
                        return field;
                    } catch (NoSuchFieldException ignored) {

                    }
                }
                current = current.getSuperclass();
            }
            return null;
        }

        private static @Nullable Method findSendPacket(Class<?> connectionType) {
            for (Method method : connectionType.getMethods()) {
                String name = method.getName();
                if (method.getParameterCount() == 1 && ("sendPacket".equals(name) || "send".equals(name))) {
                    return method;
                }
            }
            return null;
        }

        @SuppressWarnings({"rawtypes", "unchecked"})
        private static @Nullable Object enumConstant(Class<?> type, String... names) {
            if (!type.isEnum()) {
                return null;
            }
            Class<? extends Enum> enumType = type.asSubclass(Enum.class);
            for (String name : names) {
                try {
                    return Enum.valueOf(enumType, name);
                } catch (IllegalArgumentException ignored) {

                }
            }
            return null;
        }

        private static @Nullable Object firstNonNull(@Nullable Object first, @Nullable Object second) {
            return first != null ? first : second;
        }
    }
}
